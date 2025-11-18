package br.com.minicloud.dao;

import br.com.minicloud.dominio.InstanciaComputacao;
import br.com.minicloud.dao.ConexaoBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstanciaComputacaoDAO {

    // 1. CREATE (Inserir uma nova InstanciaComputacao)
    public InstanciaComputacao inserirInstancia(InstanciaComputacao instancia) {
        Connection conn = null;
        PreparedStatement stmtRecurso = null;
        PreparedStatement stmtInstancia = null;
        ResultSet rs = null;

        String sqlRecurso = "INSERT INTO recurso (nome, custoBaseHora) VALUES (?, ?) RETURNING id";
        String sqlInstancia = "INSERT INTO instanciacomputacao (recurso_id, vcpus, memoriaGb) VALUES (?, ?, ?)";

        try {
            conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
            conn.setAutoCommit(false);

            // --- PASSO 1: INSERIR RECURSO PAI ---
            stmtRecurso = conn.prepareStatement(sqlRecurso, Statement.RETURN_GENERATED_KEYS);
            stmtRecurso.setString(1, instancia.getNome());
            stmtRecurso.setDouble(2, instancia.getCustoBaseHora());

            stmtRecurso.executeUpdate();

            rs = stmtRecurso.getGeneratedKeys();
            if (rs.next()) {
                int idGerado = rs.getInt(1);
                instancia.setId(idGerado);

                // --- PASSO 2: INSERIR INSTÂNCIA ESPECÍFICA ---
                stmtInstancia = conn.prepareStatement(sqlInstancia);
                stmtInstancia.setInt(1, idGerado);
                stmtInstancia.setInt(2, instancia.getVcpus());
                stmtInstancia.setInt(3, instancia.getMemoriaGb());

                stmtInstancia.executeUpdate();

                conn.commit();
                System.out.println("InstanciaComputacao inserida com sucesso. ID: " + idGerado);
                return instancia;
            } else {
                conn.rollback();
                throw new SQLException("Falha ao obter o ID gerado do Recurso Pai.");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao inserir InstanciaComputacao: " + e.getMessage());
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
                if (stmtInstancia != null) stmtInstancia.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }

    // 2. READ (Buscar InstanciaComputacao por ID)
    public InstanciaComputacao buscarPorId(int id) {
        String sql = "SELECT r.id, r.nome, r.custoBaseHora, r.ativo, r.horasUsoMes, " +
                "ic.vcpus, ic.memoriaGb " +
                "FROM recurso r JOIN instanciacomputacao ic ON r.id = ic.recurso_id " +
                "WHERE r.id = ?";

        try (Connection conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    InstanciaComputacao instancia = new InstanciaComputacao(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getDouble("custoBaseHora"),
                            rs.getInt("vcpus"),
                            rs.getInt("memoriaGb")
                    );
                    instancia.setAtivo(rs.getBoolean("ativo"));
                    instancia.setHorasUsoMes(rs.getInt("horasUsoMes"));
                    return instancia;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar InstanciaComputacao por ID: " + e.getMessage());
        }
        return null;
    }

    // 3. READ (Listar todas as InstanciaComputacao)
    public List<InstanciaComputacao> listarTodos() {
        List<InstanciaComputacao> instancias = new ArrayList<>();
        String sql = "SELECT r.id, r.nome, r.custoBaseHora, r.ativo, r.horasUsoMes, " +
                "ic.vcpus, ic.memoriaGb " +
                "FROM recurso r JOIN instanciacomputacao ic ON r.id = ic.recurso_id " +
                "ORDER BY r.nome";

        try (Connection conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                InstanciaComputacao instancia = new InstanciaComputacao(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("custoBaseHora"),
                        rs.getInt("vcpus"),
                        rs.getInt("memoriaGb")
                );
                instancia.setAtivo(rs.getBoolean("ativo"));
                instancia.setHorasUsoMes(rs.getInt("horasUsoMes"));
                instancias.add(instancia);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar InstanciaComputacao: " + e.getMessage());
        }
        return instancias;
    }

    // 4. UPDATE (Atualizar uma InstanciaComputacao)
    public boolean atualizarInstancia(InstanciaComputacao instancia) {
        Connection conn = null;
        PreparedStatement stmtRecurso = null;
        PreparedStatement stmtInstancia = null;

        String sqlRecurso = "UPDATE recurso SET nome = ?, custoBaseHora = ?, ativo = ?, horasUsoMes = ? WHERE id = ?";
        String sqlInstancia = "UPDATE instanciacomputacao SET vcpus = ?, memoriaGb = ? WHERE recurso_id = ?";

        try {
            conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
            conn.setAutoCommit(false);

            // --- PASSO 1: ATUALIZAR RECURSO PAI ---
            stmtRecurso = conn.prepareStatement(sqlRecurso);
            stmtRecurso.setString(1, instancia.getNome());
            stmtRecurso.setDouble(2, instancia.getCustoBaseHora());
            stmtRecurso.setBoolean(3, instancia.isAtivo());
            stmtRecurso.setInt(4, instancia.getHorasUsoMes());
            stmtRecurso.setInt(5, instancia.getId());

            int updatedRecurso = stmtRecurso.executeUpdate();

            // --- PASSO 2: ATUALIZAR INSTÂNCIA ESPECÍFICA ---
            stmtInstancia = conn.prepareStatement(sqlInstancia);
            stmtInstancia.setInt(1, instancia.getVcpus());
            stmtInstancia.setInt(2, instancia.getMemoriaGb());
            stmtInstancia.setInt(3, instancia.getId());

            int updatedInstancia = stmtInstancia.executeUpdate();

            if (updatedRecurso > 0 && updatedInstancia > 0) {
                conn.commit();
                System.out.println("InstanciaComputacao ID " + instancia.getId() + " atualizada com sucesso.");
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar InstanciaComputacao: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (stmtRecurso != null) stmtRecurso.close();
                if (stmtInstancia != null) stmtInstancia.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }

    // 5. DELETE (Excluir uma InstanciaComputacao)
    public boolean excluirInstancia(int id) {
        Connection conn = null;
        PreparedStatement stmtInstancia = null;
        PreparedStatement stmtRecurso = null;

        String sqlInstancia = "DELETE FROM instanciacomputacao WHERE recurso_id = ?";
        String sqlRecurso = "DELETE FROM recurso WHERE id = ?";

        try {
            conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
            conn.setAutoCommit(false);

            // --- PASSO 1: EXCLUIR INSTÂNCIA ESPECÍFICA ---
            stmtInstancia = conn.prepareStatement(sqlInstancia);
            stmtInstancia.setInt(1, id);
            stmtInstancia.executeUpdate();

            // --- PASSO 2: EXCLUIR RECURSO PAI ---
            stmtRecurso = conn.prepareStatement(sqlRecurso);
            stmtRecurso.setInt(1, id);
            int deletedRecurso = stmtRecurso.executeUpdate();

            if (deletedRecurso > 0) {
                conn.commit();
                System.out.println("InstanciaComputacao ID " + id + " excluída com sucesso.");
                return true;
            } else {
                conn.rollback();
                System.err.println("Falha ao excluir Recurso pai ID " + id + ".");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Erro ao excluir InstanciaComputacao: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (stmtInstancia != null) stmtInstancia.close();
                if (stmtRecurso != null) stmtRecurso.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }
}