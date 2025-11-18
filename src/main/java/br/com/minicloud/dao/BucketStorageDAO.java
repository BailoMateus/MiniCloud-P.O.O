package br.com.minicloud.dao;

import br.com.minicloud.dominio.BucketStorage;
import br.com.minicloud.dao.ConexaoBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BucketStorageDAO {

    // 1. CREATE (Inserir um novo BucketStorage)
    public BucketStorage inserirBucketStorage(BucketStorage bucket) {
        Connection conn = null;
        PreparedStatement stmtRecurso = null;
        PreparedStatement stmtBucket = null;
        ResultSet rs = null;

        String sqlRecurso = "INSERT INTO recurso (nome, custoBaseHora) VALUES (?, ?) RETURNING id";
        String sqlBucket = "INSERT INTO bucketstorage (recurso_id, armazenamentoGb, custoAdicionalGb) VALUES (?, ?, ?)";

        try {
            conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
            conn.setAutoCommit(false);

            // --- PASSO 1: INSERIR RECURSO PAI ---
            stmtRecurso = conn.prepareStatement(sqlRecurso, Statement.RETURN_GENERATED_KEYS);
            stmtRecurso.setString(1, bucket.getNome());
            stmtRecurso.setDouble(2, bucket.getCustoBaseHora());
            stmtRecurso.executeUpdate();

            rs = stmtRecurso.getGeneratedKeys();
            if (rs.next()) {
                int idGerado = rs.getInt(1);
                bucket.setId(idGerado);

                // --- PASSO 2: INSERIR BUCKET ESPECÍFICO ---
                stmtBucket = conn.prepareStatement(sqlBucket);
                stmtBucket.setInt(1, idGerado);
                stmtBucket.setInt(2, bucket.getArmazenamentoGb());
                stmtBucket.setDouble(3, bucket.getCustoPorGb());

                stmtBucket.executeUpdate();

                conn.commit();
                return bucket;
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
                if (stmtBucket != null) stmtBucket.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }

    // 2. READ (Buscar BucketStorage por ID)
    public BucketStorage buscarPorId(int id) {
        String sql = "SELECT r.id, r.nome, r.custoBaseHora, r.ativo, r.horasUsoMes, " +
                "b.armazenamentoGb, b.custoAdicionalGb " +
                "FROM recurso r JOIN bucketstorage b ON r.id = b.recurso_id " +
                "WHERE r.id = ?";

        try (Connection conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BucketStorage bucket = new BucketStorage(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getDouble("custoBaseHora"),
                            rs.getInt("armazenamentoGb"),
                            rs.getDouble("custoAdicionalGb")
                    );
                    bucket.setAtivo(rs.getBoolean("ativo"));
                    bucket.setHorasUsoMes(rs.getInt("horasUsoMes"));
                    return bucket;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar BucketStorage: " + e.getMessage());
        }
        return null;
    }

    // 3. READ (Listar todos os BucketStorages)
    public List<BucketStorage> listarTodos() {
        List<BucketStorage> buckets = new ArrayList<>();
        String sql = "SELECT r.id, r.nome, r.custoBaseHora, r.ativo, r.horasUsoMes, " +
                "b.armazenamentoGb, b.custoAdicionalGb " +
                "FROM recurso r JOIN bucketstorage b ON r.id = b.recurso_id " +
                "ORDER BY r.nome";

        try (Connection conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BucketStorage bucket = new BucketStorage(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("custoBaseHora"),
                        rs.getInt("armazenamentoGb"),
                        rs.getDouble("custoAdicionalGb")
                );
                bucket.setAtivo(rs.getBoolean("ativo"));
                bucket.setHorasUsoMes(rs.getInt("horasUsoMes"));
                buckets.add(bucket);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar BucketStorage: " + e.getMessage());
        }
        return buckets;
    }

    // 4. UPDATE (Atualizar um BucketStorage)
    public boolean atualizarBucketStorage(BucketStorage bucket) {
        Connection conn = null;
        PreparedStatement stmtRecurso = null;
        PreparedStatement stmtBucket = null;

        String sqlRecurso = "UPDATE recurso SET nome = ?, custoBaseHora = ?, ativo = ?, horasUsoMes = ? WHERE id = ?";
        String sqlBucket = "UPDATE bucketstorage SET armazenamentoGb = ?, custoAdicionalGb = ? WHERE recurso_id = ?";

        try {
            conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
            conn.setAutoCommit(false);

            // --- PASSO 1: ATUALIZAR RECURSO PAI ---
            stmtRecurso = conn.prepareStatement(sqlRecurso);
            stmtRecurso.setString(1, bucket.getNome());
            stmtRecurso.setDouble(2, bucket.getCustoBaseHora());
            stmtRecurso.setBoolean(3, bucket.isAtivo());
            stmtRecurso.setInt(4, bucket.getHorasUsoMes());
            stmtRecurso.setInt(5, bucket.getId());
            int updatedRecurso = stmtRecurso.executeUpdate();

            // --- PASSO 2: ATUALIZAR BUCKET ESPECÍFICO ---
            stmtBucket = conn.prepareStatement(sqlBucket);
            stmtBucket.setInt(1, bucket.getArmazenamentoGb());
            stmtBucket.setDouble(2, bucket.getCustoPorGb());
            stmtBucket.setInt(3, bucket.getId());
            int updatedBucket = stmtBucket.executeUpdate();

            if (updatedRecurso > 0 && updatedBucket > 0) {
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
                if (stmtBucket != null) stmtBucket.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }

    // 5. DELETE (Excluir um BucketStorage - Lógica idêntica aos outros)
    public boolean excluirBucketStorage(int id) {
        Connection conn = null;
        PreparedStatement stmtBucket = null;
        PreparedStatement stmtRecurso = null;

        String sqlBucket = "DELETE FROM bucketstorage WHERE recurso_id = ?";
        String sqlRecurso = "DELETE FROM recurso WHERE id = ?";

        try {
            conn = ConexaoBD.getConexao(); // CORRIGIDO: getConnection()
            conn.setAutoCommit(false);

            stmtBucket = conn.prepareStatement(sqlBucket);
            stmtBucket.setInt(1, id);
            stmtBucket.executeUpdate();

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
                if (stmtBucket != null) stmtBucket.close();
                if (stmtRecurso != null) stmtRecurso.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }
}