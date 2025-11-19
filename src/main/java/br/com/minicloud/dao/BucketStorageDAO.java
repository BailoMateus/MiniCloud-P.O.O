package br.com.minicloud.dao;

import br.com.minicloud.dominio.BucketStorage;
import br.com.minicloud.dominio.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BucketStorageDAO {

    private static final String TIPO_RECURSO_STORAGE = "STORAGE";

    /**
     * Insere um novo Bucket de Storage para um usuário.
     *
     * 1) INSERT em recursos
     * 2) INSERT em buckets_storage
     */
    public BucketStorage inserirBucket(Usuario usuario, BucketStorage bucket) {
        String sqlRecurso = "INSERT INTO recursos " +
                "(usuario_id, nome, tipo_recurso, ativo, custo_base_hora) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id_recurso";

        String sqlBucket = "INSERT INTO buckets_storage " +
                "(recurso_id, armazenamento_gb, requisicoes_mes) " +
                "VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmtRecurso = null;
        PreparedStatement stmtBucket = null;

        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false);

            // 1) Recurso genérico
            stmtRecurso = conn.prepareStatement(sqlRecurso);
            stmtRecurso.setInt(1, usuario.getId());
            stmtRecurso.setString(2, bucket.getNome());
            stmtRecurso.setString(3, TIPO_RECURSO_STORAGE);
            stmtRecurso.setBoolean(4, true);
            stmtRecurso.setDouble(5, bucket.getCustoBaseHora());

            int idRecurso = -1;
            try (ResultSet rs = stmtRecurso.executeQuery()) {
                if (rs.next()) {
                    idRecurso = rs.getInt("id_recurso");
                }
            }

            if (idRecurso <= 0) {
                throw new SQLException("Falha ao obter id_recurso gerado para Bucket Storage.");
            }

            // 2) Bucket específico
            stmtBucket = conn.prepareStatement(sqlBucket);
            stmtBucket.setInt(1, idRecurso);
            stmtBucket.setInt(2, bucket.getArmazenamentoGb());
            // como o domínio não tem requisicoesMes, usamos 0 por padrão
            stmtBucket.setInt(3, 0);
            stmtBucket.executeUpdate();

            conn.commit();

            bucket.setId(idRecurso);
            System.out.println("Bucket Storage inserido com sucesso. ID recurso = " + idRecurso);
            return bucket;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir Bucket Storage: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transação revertida.");
                } catch (SQLException ex) {
                    System.err.println("Erro ao reverter transação: " + ex.getMessage());
                }
            }
            return null;
        } finally {
            if (stmtBucket != null) {
                try { stmtBucket.close(); } catch (SQLException ignored) {}
            }
            if (stmtRecurso != null) {
                try { stmtRecurso.close(); } catch (SQLException ignored) {}
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Lista todos os Buckets de Storage de um usuário.
     */
    public List<BucketStorage> listarPorUsuario(int idUsuario) {
        List<BucketStorage> lista = new ArrayList<>();

        String sql = """
                SELECT r.id_recurso,
                       r.nome,
                       r.custo_base_hora,
                       r.ativo,
                       b.armazenamento_gb,
                       b.requisicoes_mes
                  FROM recursos r
                  JOIN buckets_storage b
                    ON b.recurso_id = r.id_recurso
                 WHERE r.usuario_id = ?
                   AND r.tipo_recurso = 'STORAGE'
                 ORDER BY r.nome
                """;

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // custoPorGb não está no schema; usamos 0.0 como default
                    BucketStorage bucket = new BucketStorage(
                            rs.getInt("id_recurso"),
                            rs.getString("nome"),
                            rs.getDouble("custo_base_hora"),
                            rs.getInt("armazenamento_gb"),
                            0.0
                    );
                    // Se depois você adicionar requisicoesMes no domínio, pode preencher aqui.
                    lista.add(bucket);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar Buckets Storage: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Exclui um Bucket de Storage pelo ID do recurso.
     */
    public boolean excluirPorIdRecurso(int idRecurso) {
        String sqlDelBucket = "DELETE FROM buckets_storage WHERE recurso_id = ?";
        String sqlDelRecurso = "DELETE FROM recursos WHERE id_recurso = ?";

        Connection conn = null;
        PreparedStatement stmtBucket = null;
        PreparedStatement stmtRecurso = null;

        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false);

            // detalhe
            stmtBucket = conn.prepareStatement(sqlDelBucket);
            stmtBucket.setInt(1, idRecurso);
            stmtBucket.executeUpdate();

            // recurso genérico
            stmtRecurso = conn.prepareStatement(sqlDelRecurso);
            stmtRecurso.setInt(1, idRecurso);
            int linhas = stmtRecurso.executeUpdate();

            conn.commit();
            return linhas > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir Bucket Storage: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (stmtRecurso != null) {
                try { stmtRecurso.close(); } catch (SQLException ignored) {}
            }
            if (stmtBucket != null) {
                try { stmtBucket.close(); } catch (SQLException ignored) {}
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }
}
