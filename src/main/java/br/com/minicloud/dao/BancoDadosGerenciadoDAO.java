package br.com.minicloud.dao;

import br.com.minicloud.dominio.BancoDadosGerenciado;
import br.com.minicloud.dao.ConexaoBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BancoDadosGerenciadoDAO {

    // 1. CREATE (Inserir um novo BancoDadosGerenciado)
    public BancoDadosGerenciado inserirBancoDados(BancoDadosGerenciado banco) {
        Connection conn = null;
        PreparedStatement stmtRecurso = null;
        PreparedStatement stmtBanco = null;
        ResultSet rs = null;

        // SQL 1: Inserir na tabela pai (Recurso)
        String sqlRecurso = "INSERT INTO recurso (nome, custoBaseHora) VALUES (?, ?) RETURNING id";

        // SQL 2: Inserir na tabela específica (BancoDadosGerenciado)
        String sqlBanco = "INSERT INTO bancodadosgerenciado (recurso_id, armazenamentoGb, replicacaoAtiva, custoPorGb) VALUES (?, ?, ?, ?)";

        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false); // Inicia Transação

            // --- PASSO 1: INSERIR RECURSO PAI ---
            stmtRecurso = conn.prepareStatement(sqlRecurso, Statement.RETURN_GENERATED_KEYS);
            stmtRecurso.setString(1, banco.getNome());
            stmtRecurso.setDouble(2, banco.getCustoBaseHora());

            stmtRecurso.executeUpdate();

            // Obter o ID gerado
            rs = stmtRecurso.getGeneratedKeys();
            if (rs.next()) {
                int idGerado = rs.getInt(1);
                banco.setId(idGerado); // Define o ID no objeto Java

                // --- PASSO 2: INSERIR BANCO ESPECÍFICO ---
                stmtBanco = conn.prepareStatement(sqlBanco);
                stmtBanco.setInt(1, idGerado); // Usa o ID como FK
                stmtBanco.setInt(2, banco.getArmazenamentoGb());
                stmtBanco.setBoolean(3, banco.isReplicacaoAtiva());
                stmtBanco.setDouble(4, banco.getCustoPorGb());

                stmtBanco.executeUpdate();

                conn.commit(); // Finaliza Transação
                System.out.println("BancoDadosGerenciado inserido com sucesso. ID: " + idGerado);
                return banco;
            } else {
                conn.rollback();
                throw new SQLException("Falha ao obter o ID gerado do Recurso Pai.");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao inserir BancoDadosGerenciado: " + e.getMessage());
            try {
                if (conn != null) conn.rollback(); // Desfaz em caso de erro
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
                "bdg.armazenamentoGb, bdg.replicacaoAtiva, bdg.custoPorGb " +
                "FROM recurso r JOIN bancodadosgerenciado bdg ON r.id = bdg.recurso_id " +
                "WHERE r.id = ?";

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BancoDadosGerenciado banco = new BancoDadosGerenciado(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getDouble("custoBaseHora"),
                            rs.getInt("armazenamentoGb"),
                            rs.getBoolean("replicacaoAtiva"),
                            rs.getDouble("custoPorGb")
                    );

                    // Configura atributos herdados de RecursoCloud
                    banco.setAtivo(rs.getBoolean("ativo"));
                    banco.setHorasUsoMes(rs.getInt("horasUsoMes"));
                    return banco;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar BancoDadosGerenciado por ID: " + e.getMessage());
        }
        return null;
    }

    // 3. READ (Listar todos os BancoDadosGerenciado)
    public List<BancoDadosGerenciado> listarTodos() {
        List<BancoDadosGerenciado> bancos = new ArrayList<>();
        String sql = "SELECT r.id, r.nome, r.custoBaseHora, r.ativo, r.horasUsoMes, " +
                "bdg.armazenamentoGb, bdg.replicacaoAtiva, bdg.custoPorGb " +
                "FROM recurso r JOIN bancodadosgerenciado bdg ON r.id = bdg.recurso_id " +
                "ORDER BY r.nome";

        try (Connection conn = ConexaoBD.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BancoDadosGerenciado banco = new BancoDadosGerenciado(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("custoBaseHora"),
                        rs.getInt("armazenamentoGb"),
                        rs.getBoolean("replicacaoAtiva"),
                        rs.getDouble("custoPorGb")
                );
                // Configura atributos herdados
                banco.setAtivo(rs.getBoolean("ativo"));
                banco.setHorasUsoMes(rs.getInt("horasUsoMes"));
                bancos.add(banco);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar BancoDadosGerenciado: " + e.getMessage());
        }
        return bancos;
    }

    // 4. UPDATE (Atualizar um BancoDadosGerenciado)
    public boolean atualizarBancoDados(BancoDadosGerenciado banco) {
        Connection conn = null;
        PreparedStatement stmtRecurso = null;
        PreparedStatement stmtBanco = null;

        // SQL 1: Atualizar a tabela pai (Recurso)
        String sqlRecurso = "UPDATE recurso SET nome = ?, custoBaseHora = ?, ativo = ?, horasUsoMes = ? WHERE id = ?";

        // SQL 2: Atualizar a tabela específica (BancoDadosGerenciado)
        String sqlBanco = "UPDATE bancodadosgerenciado SET armazenamentoGb = ?, replicacaoAtiva = ?, custoPorGb = ? WHERE recurso_id = ?";

        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false); // Inicia Transação

            // --- PASSO 1: ATUALIZAR RECURSO PAI ---
            stmtRecurso = conn.prepareStatement(sqlRecurso);
            stmtRecurso.setString(1, banco.getNome());
            stmtRecurso.setDouble(2, banco.getCustoBaseHora());
            stmtRecurso.setBoolean(3, banco.isAtivo());
            stmtRecurso.setInt(4, banco.getHorasUsoMes());
            stmtRecurso.setInt(5, banco.getId());

            int updatedRecurso = stmtRecurso.executeUpdate();

            // --- PASSO 2: ATUALIZAR BANCO ESPECÍFICO ---
            stmtBanco = conn.prepareStatement(sqlBanco);
            stmtBanco.setInt(1, banco.getArmazenamentoGb());
            stmtBanco.setBoolean(2, banco.isReplicacaoAtiva());
            stmtBanco.setDouble(3, banco.getCustoPorGb());
            stmtBanco.setInt(4, banco.getId());

            int updatedBanco = stmtBanco.executeUpdate();

            if (updatedRecurso > 0 && updatedBanco > 0) {
                conn.commit();
                System.out.println("BancoDadosGerenciado ID " + banco.getId() + " atualizado com sucesso.");
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar BancoDadosGerenciado: " + e.getMessage());
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

    // 5. DELETE (Excluir um BancoDadosGerenciado)
    public boolean excluirBancoDados(int id) {
        Connection conn = null;
        PreparedStatement stmtBanco = null;
        PreparedStatement stmtRecurso = null;

        // SQL 1: Excluir da tabela específica (BancoDadosGerenciado)
        String sqlBanco = "DELETE FROM bancodadosgerenciado WHERE recurso_id = ?";

        // SQL 2: Excluir da tabela pai (Recurso)
        String sqlRecurso = "DELETE FROM recurso WHERE id = ?";

        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false); // Inicia Transação

            // --- PASSO 1: EXCLUIR BANCO ESPECÍFICO ---
            stmtBanco = conn.prepareStatement(sqlBanco);
            stmtBanco.setInt(1, id);
            stmtBanco.executeUpdate();

            // --- PASSO 2: EXCLUIR RECURSO PAI ---
            stmtRecurso = conn.prepareStatement(sqlRecurso);
            stmtRecurso.setInt(1, id);
            int deletedRecurso = stmtRecurso.executeUpdate();

            if (deletedRecurso > 0) {
                conn.commit();
                System.out.println("BancoDadosGerenciado ID " + id + " excluído com sucesso.");
                return true;
            } else {
                conn.rollback();
                System.err.println("Falha ao excluir Recurso pai ID " + id + ".");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Erro ao excluir BancoDadosGerenciado: " + e.getMessage());
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