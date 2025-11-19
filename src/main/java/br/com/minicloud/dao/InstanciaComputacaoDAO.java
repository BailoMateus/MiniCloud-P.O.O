package br.com.minicloud.dao;

import br.com.minicloud.dominio.InstanciaComputacao;
import br.com.minicloud.dominio.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstanciaComputacaoDAO {

    private static final String TIPO_RECURSO_COMPUTACAO = "COMPUTACAO";

    /**
     * Insere uma nova Instância de Computação para um usuário.
     *
     * Fluxo:
     * 1) INSERT em recursos (usuario_id, nome, tipo_recurso, ativo, custo_base_hora)
     * 2) INSERT em instancias_computacao (recurso_id, vcpus, memoria_gb)
     * Tudo dentro de uma TRANSAÇÃO.
     */
    public InstanciaComputacao inserirInstancia(Usuario usuario, InstanciaComputacao instancia) {
        String sqlRecurso = "INSERT INTO recursos " +
                "(usuario_id, nome, tipo_recurso, ativo, custo_base_hora) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id_recurso";

        String sqlInstancia = "INSERT INTO instancias_computacao " +
                "(recurso_id, vcpus, memoria_gb) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmtRecurso = null;
        PreparedStatement stmtInstancia = null;

        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false); // inicia a transação

            // 1) Insere na tabela recursos
            stmtRecurso = conn.prepareStatement(sqlRecurso);
            stmtRecurso.setInt(1, usuario.getId());
            stmtRecurso.setString(2, instancia.getNome());
            stmtRecurso.setString(3, TIPO_RECURSO_COMPUTACAO);
            stmtRecurso.setBoolean(4, true); // ativo = true
            stmtRecurso.setDouble(5, instancia.getCustoBaseHora());

            int idRecurso = -1;
            try (ResultSet rs = stmtRecurso.executeQuery()) {
                if (rs.next()) {
                    idRecurso = rs.getInt("id_recurso");
                }
            }

            if (idRecurso <= 0) {
                throw new SQLException("Falha ao obter id_recurso gerado para a instância de computação.");
            }

            // 2) Insere na tabela instancias_computacao
            stmtInstancia = conn.prepareStatement(sqlInstancia);
            stmtInstancia.setInt(1, idRecurso);
            stmtInstancia.setInt(2, instancia.getVcpus());
            stmtInstancia.setInt(3, instancia.getMemoriaGb());
            stmtInstancia.executeUpdate();

            // Confirma transação
            conn.commit();

            // Atualiza o objeto em memória com o ID do recurso
            instancia.setId(idRecurso);
            System.out.println("Instância de computação inserida com sucesso. ID recurso = " + idRecurso);
            return instancia;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir instância de computação: " + e.getMessage());
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
            // Fecha recursos JDBC
            if (stmtInstancia != null) {
                try {
                    stmtInstancia.close();
                } catch (SQLException ignored) {}
            }
            if (stmtRecurso != null) {
                try {
                    stmtRecurso.close();
                } catch (SQLException ignored) {}
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
     * Lista todas as instâncias de computação de um usuário,
     * juntando dados de recursos + instancias_computacao.
     */
    public List<InstanciaComputacao> listarPorUsuario(int idUsuario) {
        List<InstanciaComputacao> lista = new ArrayList<>();

        String sql = """
                SELECT r.id_recurso,
                       r.nome,
                       r.custo_base_hora,
                       r.ativo,
                       ic.vcpus,
                       ic.memoria_gb
                  FROM recursos r
                  JOIN instancias_computacao ic
                    ON ic.recurso_id = r.id_recurso
                 WHERE r.usuario_id = ?
                   AND r.tipo_recurso = 'COMPUTACAO'
                 ORDER BY r.nome
                """;

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InstanciaComputacao inst = new InstanciaComputacao(
                            rs.getInt("id_recurso"),
                            rs.getString("nome"),
                            rs.getDouble("custo_base_hora"),
                            rs.getInt("vcpus"),
                            rs.getInt("memoria_gb")
                    );

                    // se sua classe tiver setAtivo / setHorasUso etc., pode preencher aqui
                    // inst.setAtivo(rs.getBoolean("ativo"));

                    lista.add(inst);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar instâncias de computação: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Exclui uma instância de computação a partir do ID do recurso.
     * Apaga primeiro de instancias_computacao, depois de recursos.
     */
    public boolean excluirPorIdRecurso(int idRecurso) {
        String sqlDelInstancia = "DELETE FROM instancias_computacao WHERE recurso_id = ?";
        String sqlDelRecurso = "DELETE FROM recursos WHERE id_recurso = ?";

        Connection conn = null;
        PreparedStatement stmtInstancia = null;
        PreparedStatement stmtRecurso = null;

        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false);

            // 1) Apaga da tabela de detalhe
            stmtInstancia = conn.prepareStatement(sqlDelInstancia);
            stmtInstancia.setInt(1, idRecurso);
            stmtInstancia.executeUpdate();

            // 2) Apaga da tabela de recursos
            stmtRecurso = conn.prepareStatement(sqlDelRecurso);
            stmtRecurso.setInt(1, idRecurso);
            int linhas = stmtRecurso.executeUpdate();

            conn.commit();
            return linhas > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir instância de computação: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erro ao reverter transação: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (stmtRecurso != null) {
                try {
                    stmtRecurso.close();
                } catch (SQLException ignored) {}
            }
            if (stmtInstancia != null) {
                try {
                    stmtInstancia.close();
                } catch (SQLException ignored) {}
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
