package br.com.minicloud.dao;

import br.com.minicloud.dominio.BancoDadosGerenciado;
import br.com.minicloud.dominio.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BancoDadosGerenciadoDAO {

    private static final String TIPO_RECURSO_BANCO = "BANCO_DADOS";

    /**
     * Insere um novo Banco de Dados Gerenciado para um usuário.
     *
     * Fluxo:
     * 1) INSERT em recursos (usuario_id, nome, tipo_recurso, ativo, custo_base_hora)
     * 2) INSERT em bancos_dados_gerenciados (recurso_id, armazenamento_gb, replicacao_ativa)
     */
    public BancoDadosGerenciado inserirBanco(Usuario usuario, BancoDadosGerenciado banco) {
        String sqlRecurso = "INSERT INTO recursos " +
                "(usuario_id, nome, tipo_recurso, ativo, custo_base_hora) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id_recurso";

        String sqlBanco = "INSERT INTO bancos_dados_gerenciados " +
                "(recurso_id, armazenamento_gb, replicacao_ativa) " +
                "VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmtRecurso = null;
        PreparedStatement stmtBanco = null;

        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false);

            // 1) Insere na tabela recursos
            stmtRecurso = conn.prepareStatement(sqlRecurso);
            stmtRecurso.setInt(1, usuario.getId());
            stmtRecurso.setString(2, banco.getNome());
            stmtRecurso.setString(3, TIPO_RECURSO_BANCO);
            stmtRecurso.setBoolean(4, true); // ativo
            stmtRecurso.setDouble(5, banco.getCustoBaseHora());

            int idRecurso = -1;
            try (ResultSet rs = stmtRecurso.executeQuery()) {
                if (rs.next()) {
                    idRecurso = rs.getInt("id_recurso");
                }
            }

            if (idRecurso <= 0) {
                throw new SQLException("Falha ao obter id_recurso gerado para Banco de Dados Gerenciado.");
            }

            // 2) Insere na tabela bancos_dados_gerenciados
            stmtBanco = conn.prepareStatement(sqlBanco);
            stmtBanco.setInt(1, idRecurso);
            stmtBanco.setInt(2, banco.getArmazenamentoGb());
            stmtBanco.setBoolean(3, banco.isReplicacaoAtiva());
            stmtBanco.executeUpdate();

            conn.commit();

            banco.setId(idRecurso);
            System.out.println("Banco de Dados Gerenciado inserido com sucesso. ID recurso = " + idRecurso);
            return banco;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir Banco de Dados Gerenciado: " + e.getMessage());
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
            if (stmtBanco != null) {
                try { stmtBanco.close(); } catch (SQLException ignored) {}
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
     * Lista todos os bancos de dados gerenciados de um usuário.
     */
    public List<BancoDadosGerenciado> listarPorUsuario(int idUsuario) {
        List<BancoDadosGerenciado> lista = new ArrayList<>();

        String sql = """
                SELECT r.id_recurso,
                       r.nome,
                       r.custo_base_hora,
                       r.ativo,
                       b.armazenamento_gb,
                       b.replicacao_ativa
                  FROM recursos r
                  JOIN bancos_dados_gerenciados b
                    ON b.recurso_id = r.id_recurso
                 WHERE r.usuario_id = ?
                   AND r.tipo_recurso = 'BANCO_DADOS'
                 ORDER BY r.nome
                """;

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Não temos coluna custoPorGb no schema, então setamos 0.0
                    BancoDadosGerenciado banco = new BancoDadosGerenciado(
                            rs.getInt("id_recurso"),
                            rs.getString("nome"),
                            rs.getDouble("custo_base_hora"),
                            rs.getInt("armazenamento_gb"),
                            rs.getBoolean("replicacao_ativa"),
                            0.0
                    );
                    lista.add(banco);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar Bancos de Dados Gerenciados: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Exclui um Banco de Dados Gerenciado pelo ID do recurso.
     */
    public boolean excluirPorIdRecurso(int idRecurso) {
        String sqlDelBanco = "DELETE FROM bancos_dados_gerenciados WHERE recurso_id = ?";
        String sqlDelRecurso = "DELETE FROM recursos WHERE id_recurso = ?";

        Connection conn = null;
        PreparedStatement stmtBanco = null;
        PreparedStatement stmtRecurso = null;

        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false);

            // 1) Detalhe
            stmtBanco = conn.prepareStatement(sqlDelBanco);
            stmtBanco.setInt(1, idRecurso);
            stmtBanco.executeUpdate();

            // 2) Recurso genérico
            stmtRecurso = conn.prepareStatement(sqlDelRecurso);
            stmtRecurso.setInt(1, idRecurso);
            int linhas = stmtRecurso.executeUpdate();

            conn.commit();
            return linhas > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir Banco de Dados Gerenciado: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (stmtRecurso != null) {
                try { stmtRecurso.close(); } catch (SQLException ignored) {}
            }
            if (stmtBanco != null) {
                try { stmtBanco.close(); } catch (SQLException ignored) {}
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
