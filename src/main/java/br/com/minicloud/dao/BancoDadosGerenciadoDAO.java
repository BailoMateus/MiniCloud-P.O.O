package br.com.minicloud.dao;

import br.com.minicloud.dominio.BancoDadosGerenciado;
import br.com.minicloud.dao.ConexaoBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BancoDadosGerenciadoDAO {

    // 1. CREATE (Inserir um novo BancoDadosGerenciado)
    public BancoDadosGerenciado inserirBancoDados(BancoDadosGerenciado bancoDados) {
        Connection conn = null;
        PreparedStatement stmtRecurso = null;
        PreparedStatement stmtBanco = null;
        ResultSet rs = null;

        String sqlRecurso = "INSERT INTO recurso (nome, custoBaseHora) VALUES (?, ?) RETURNING id";
        String sqlBanco = "INSERT INTO bancodadosgerenciado (recurso_id, armazenamentoGb, replicacaoAtiva, custoAdicionalGb) VALUES (?, ?, ?, ?)";

        try {
            conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
            conn.setAutoCommit(false);

            // --- PASSO 1: INSERIR RECURSO PAI ---
            stmtRecurso = conn.prepareStatement(sqlRecurso, Statement.RETURN_GENERATED_KEYS);
            stmtRecurso.setString(1, bancoDados.getNome());
            stmtRecurso.setDouble(2, bancoDados.getCustoBaseHora());
            stmtRecurso.executeUpdate();

            rs = stmtRecurso.getGeneratedKeys();
            if (rs.next()) {
                int idGerado = rs.getInt(1);
                bancoDados.setId(idGerado);

                // --- PASSO 2: INSERIR BANCO DE DADOS ESPECÍFICO ---
                stmtBanco = conn.prepareStatement(sqlBanco);
                stmtBanco.setInt(1, idGerado);
                stmtBanco.setInt(2, bancoDados.getArmazenamentoGb());
                stmtBanco.setBoolean(3, bancoDados.isReplicacaoAtiva());
                stmtBanco.setDouble(4, bancoDados.getCustoPorGb());

                stmtBanco.executeUpdate();

                conn.commit();
                return bancoDados;
            } else {
                conn.rollback();
                throw new SQLException("Falha ao obter o ID gerado do Recurso Pai.");
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
            }
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmtRecurso != null) stmtRecurso.close();
                if (stmtBanco != null) stmtBanco.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }

    // 2. READ (Buscar BancoDadosGerenciado por ID)
    public BancoDadosGerenciado buscarPorId(int id) {
        String sql = "SELECT r.id, r.nome, r.custoBaseHora, r.ativo, r.horasUsoMes, " +
                "b.armazenamentoGb, b.replicacaoAtiva, b.custoAdicionalGb " +
                "FROM recurso r JOIN bancodadosgerenciado b ON r.id = b.recurso_id " +
                "WHERE r.id = ?";

        try (Connection conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BancoDadosGerenciado bd = new BancoDadosGerenciado(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getDouble("custoBaseHora"),
                            rs.getInt("armazenamentoGb"),
                            rs.getBoolean("replicacaoAtiva"),
                            rs.getDouble("custoAdicionalGb")
                    );
                    bd.setAtivo(rs.getBoolean("ativo"));
                    bd.setHorasUsoMes(rs.getInt("horasUsoMes"));
                    return bd;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar BancoDadosGerenciado: " + e.getMessage());
        }
        return null;
    }

    // 3. READ (Listar todos os BancoDadosGerenciados)
    public List<BancoDadosGerenciado> listarTodos() {
        List<BancoDadosGerenciado> bancos = new ArrayList<>();
        String sql = "SELECT r.id, r.nome, r.custoBaseHora, r.ativo, r.horasUsoMes, " +
                "b.armazenamentoGb, b.replicacaoAtiva, b.custoAdicionalGb " +
                "FROM recurso r JOIN bancodadosgerenciado b ON r.id = b.recurso_id " +
                "ORDER BY r.nome";

        try (Connection conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BancoDadosGerenciado bd = new BancoDadosGerenciado(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("custoBaseHora"),
                        rs.getInt("armazenamentoGb"),
                        rs.getBoolean("replicacaoAtiva"),
                        rs.getDouble("custoAdicionalGb")
                );
                bd.setAtivo(rs.getBoolean("ativo"));
                bd.setHorasUsoMes(rs.getInt("horasUsoMes"));
                bancos.add(bd);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar BancoDadosGerenciado: " + e.getMessage());
        }
        return bancos;
    }

    // 4. UPDATE (Atualizar um BancoDadosGerenciado)
    public boolean atualizarBancoDados(BancoDadosGerenciado bancoDados) {
        Connection conn = null;
        PreparedStatement stmtRecurso = null;
        PreparedStatement stmtBanco = null;

        String sqlRecurso = "UPDATE recurso SET nome = ?, custoBaseHora = ?, ativo = ?, horasUsoMes = ? WHERE id = ?";
        String sqlBanco = "UPDATE bancodadosgerenciado SET armazenamentoGb = ?, replicacaoAtiva = ?, custoAdicionalGb = ? WHERE recurso_id = ?";

        try {
            conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
            conn.setAutoCommit(false);

            // --- PASSO 1: ATUALIZAR RECURSO PAI ---
            stmtRecurso = conn.prepareStatement(sqlRecurso);
            stmtRecurso.setString(1, bancoDados.getNome());
            stmtRecurso.setDouble(2, bancoDados.getCustoBaseHora());
            stmtRecurso.setBoolean(3, bancoDados.isAtivo());
            stmtRecurso.setInt(4, bancoDados.getHorasUsoMes());
            stmtRecurso.setInt(5, bancoDados.getId());
            int updatedRecurso = stmtRecurso.executeUpdate();

            // --- PASSO 2: ATUALIZAR BANCO DE DADOS ESPECÍFICO ---
            stmtBanco = conn.prepareStatement(sqlBanco);
            stmtBanco.setInt(1, bancoDados.getArmazenamentoGb());
            stmtBanco.setBoolean(2, bancoDados.isReplicacaoAtiva());
            stmtBanco.setDouble(3, bancoDados.getCustoPorGb());
            stmtBanco.setInt(4, bancoDados.getId());
            int updatedBanco = stmtBanco.executeUpdate();

            if (updatedRecurso > 0 && updatedBanco > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (stmtRecurso != null) stmtRecurso.close();
                if (stmtBanco != null) stmtBanco.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }

    // 5. DELETE (Excluir um BancoDadosGerenciado - Lógica idêntica ao EC2)
    public boolean excluirBancoDados(int id) {
        Connection conn = null;
        PreparedStatement stmtBanco = null;
        PreparedStatement stmtRecurso = null;

        String sqlBanco = "DELETE FROM bancodadosgerenciado WHERE recurso_id = ?";
        String sqlRecurso = "DELETE FROM recurso WHERE id = ?";

        try {
            conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
            conn.setAutoCommit(false);

            stmtBanco = conn.prepareStatement(sqlBanco);
            stmtBanco.setInt(1, id);
            stmtBanco.executeUpdate();

            stmtRecurso = conn.prepareStatement(sqlRecurso);
            stmtRecurso.setInt(1, id);
            int deletedRecurso = stmtRecurso.executeUpdate();

            if (deletedRecurso > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (stmtBanco != null) stmtBanco.close();
                if (stmtRecurso != null) stmtRecurso.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }
}