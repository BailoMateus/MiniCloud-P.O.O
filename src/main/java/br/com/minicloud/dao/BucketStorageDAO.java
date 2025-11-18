package br.com.minicloud.dao;

import br.com.minicloud.dominio.BucketStorage;
// Importa o RecursoCloud e ConexaoBD de onde estão no seu projeto
import br.com.minicloud.dominio.RecursoCloud;
import br.com.minicloud.dao.ConexaoBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BucketStorageDAO {

    // Assumimos que existe uma tabela 'recurso' e 'bucketstorage' no BD

    // 1. CREATE (Inserir um novo BucketStorage)
    public BucketStorage inserirBucketStorage(BucketStorage bucket) {
        Connection conn = null;
        PreparedStatement stmtRecurso = null;
        PreparedStatement stmtBucket = null;
        ResultSet rs = null;

        // SQL 1: Inserir na tabela pai (Recurso) para obter o ID
        // Assumimos que a tabela recurso tem: nome, custoBaseHora, ativo, horasUsoMes
        // Vamos inserir apenas o que é obrigatório para obter o ID.
        String sqlRecurso = "INSERT INTO recurso (nome, custoBaseHora) VALUES (?, ?) RETURNING id";

        // SQL 2: Inserir na tabela específica (BucketStorage)
        String sqlBucket = "INSERT INTO bucketstorage (recurso_id, armazenamentoGb, custoPorGb) VALUES (?, ?, ?)";

        try {
            conn = ConexaoBD.getConexao();
            // Inicia Transação
            conn.setAutoCommit(false);

            // --- PASSO 1: INSERIR RECURSO PAI ---
            stmtRecurso = conn.prepareStatement(sqlRecurso, Statement.RETURN_GENERATED_KEYS);
            stmtRecurso.setString(1, bucket.getNome());
            stmtRecurso.setDouble(2, bucket.getCustoBaseHora());

            stmtRecurso.executeUpdate();

            // Obter o ID gerado
            rs = stmtRecurso.getGeneratedKeys();
            if (rs.next()) {
                int idGerado = rs.getInt(1);
                bucket.setId(idGerado); // Define o ID no objeto Java

                // --- PASSO 2: INSERIR BUCKET ESPECÍFICO ---
                stmtBucket = conn.prepareStatement(sqlBucket);
                stmtBucket.setInt(1, idGerado); // Usa o ID como FK
                stmtBucket.setInt(2, bucket.getArmazenamentoGb());
                stmtBucket.setDouble(3, bucket.getCustoPorGb());

                stmtBucket.executeUpdate();

                // Finaliza Transação
                conn.commit();
                System.out.println("BucketStorage inserido com sucesso. ID: " + idGerado);
                return bucket;
            } else {
                conn.rollback();
                throw new SQLException("Falha ao obter o ID gerado do Recurso Pai.");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao inserir BucketStorage: " + e.getMessage());
            try {
                if (conn != null) conn.rollback(); // Desfaz em caso de erro
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
            }
            return null;
        } finally {
            // Fecha recursos e restaura o AutoCommit
            try {
                if (rs != null) rs.close();
                if (stmtRecurso != null) stmtRecurso.close();
                if (stmtBucket != null) stmtBucket.close();
                if (conn != null) conn.setAutoCommit(true);
                // NOTA: Não fechar conn aqui se ConexaoBD.getConexao() gerencia uma conexão única
                // Se a ConexaoBD usa pool ou cria nova, feche: ConexaoBD.fecharConexao(conn);
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
        return null;
    }

    // 2. READ (Buscar BucketStorage por ID)
    public BucketStorage buscarPorId(int id) {
        String sql = "SELECT r.id, r.nome, r.custoBaseHora, r.ativo, r.horasUsoMes, " +
                "bs.armazenamentoGb, bs.custoPorGb " +
                "FROM recurso r JOIN bucketstorage bs ON r.id = bs.recurso_id " +
                "WHERE r.id = ?";

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BucketStorage bucket = new BucketStorage(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getDouble("custoBaseHora"),
                            rs.getInt("armazenamentoGb"),
                            rs.getDouble("custoPorGb")
                    );

                    // Configura atributos herdados de RecursoCloud (ativo e horasUsoMes)
                    bucket.setAtivo(rs.getBoolean("ativo"));
                    bucket.setHorasUsoMes(rs.getInt("horasUsoMes"));
                    return bucket;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar BucketStorage por ID: " + e.getMessage());
        }
        return null; // Retorna null se não encontrar
    }

    // 3. READ (Listar todos os BucketStorage)
    public List<BucketStorage> listarTodos() {
        List<BucketStorage> buckets = new ArrayList<>();
        String sql = "SELECT r.id, r.nome, r.custoBaseHora, r.ativo, r.horasUsoMes, " +
                "bs.armazenamentoGb, bs.custoPorGb " +
                "FROM recurso r JOIN bucketstorage bs ON r.id = bs.recurso_id " +
                "ORDER BY r.nome";

        try (Connection conn = ConexaoBD.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BucketStorage bucket = new BucketStorage(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("custoBaseHora"),
                        rs.getInt("armazenamentoGb"),
                        rs.getDouble("custoPorGb")
                );
                // Configura atributos herdados
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

        // SQL 1: Atualizar a tabela pai (Recurso)
        String sqlRecurso = "UPDATE recurso SET nome = ?, custoBaseHora = ?, ativo = ?, horasUsoMes = ? WHERE id = ?";

        // SQL 2: Atualizar a tabela específica (BucketStorage)
        String sqlBucket = "UPDATE bucketstorage SET armazenamentoGb = ?, custoPorGb = ? WHERE recurso_id = ?";

        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false); // Inicia Transação

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
                System.out.println("BucketStorage ID " + bucket.getId() + " atualizado com sucesso.");
                return true;
            } else if (updatedRecurso > 0 && updatedBucket == 0) {
                // Caso o recurso exista, mas o subtipo não (erro no BD)
                conn.rollback();
                System.err.println("Erro de consistência: Recurso atualizado, mas BucketStorage não encontrado/atualizado.");
                return false;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar BucketStorage: " + e.getMessage());
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

    // 5. DELETE (Excluir um BucketStorage)
    public boolean excluirBucketStorage(int id) {
        Connection conn = null;
        PreparedStatement stmtBucket = null;
        PreparedStatement stmtRecurso = null;

        // SQL 1: Excluir da tabela específica (BucketStorage)
        String sqlBucket = "DELETE FROM bucketstorage WHERE recurso_id = ?";

        // SQL 2: Excluir da tabela pai (Recurso)
        // Isso pressupõe que a FK em 'bucketstorage' NÃO tem ON DELETE CASCADE
        // Se tivesse CASCADE, bastaria excluir o recurso pai.
        String sqlRecurso = "DELETE FROM recurso WHERE id = ?";

        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false); // Inicia Transação

            // --- PASSO 1: EXCLUIR BUCKET ESPECÍFICO ---
            stmtBucket = conn.prepareStatement(sqlBucket);
            stmtBucket.setInt(1, id);
            stmtBucket.executeUpdate(); // Não precisa checar count, pois pode não existir

            // --- PASSO 2: EXCLUIR RECURSO PAI ---
            stmtRecurso = conn.prepareStatement(sqlRecurso);
            stmtRecurso.setInt(1, id);
            int deletedRecurso = stmtRecurso.executeUpdate();

            if (deletedRecurso > 0) {
                conn.commit();
                System.out.println("BucketStorage ID " + id + " excluído com sucesso.");
                return true;
            } else {
                conn.rollback();
                System.err.println("Falha ao excluir Recurso pai ID " + id + ".");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Erro ao excluir BucketStorage: " + e.getMessage());
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