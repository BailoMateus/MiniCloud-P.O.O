package br.com.minicloud.dao;

import br.com.minicloud.dominio.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecursoCloudDAO {

    private InstanciaComputacaoDAO instanciaDAO = new InstanciaComputacaoDAO();
    private BancoDadosGerenciadoDAO bancoDAO = new BancoDadosGerenciadoDAO();
    private BucketStorageDAO bucketDAO = new BucketStorageDAO();

    /**
     * Retorna todos os recursos (de qualquer tipo) pertencentes a um usuário.
     */
    public List<RecursoCloud> listarTodosPorUsuario(int idUsuario) {
        List<RecursoCloud> lista = new ArrayList<>();

        // Delegamos para as DAOs específicas
        lista.addAll(instanciaDAO.listarPorUsuario(idUsuario));
        lista.addAll(bancoDAO.listarPorUsuario(idUsuario));
        lista.addAll(bucketDAO.listarPorUsuario(idUsuario));

        return lista;
    }

    /**
     * Busca um recurso pelo ID, detectando automaticamente o tipo.
     */
    public RecursoCloud buscarPorId(int idRecurso) {
        String sql = "SELECT tipo_recurso FROM recursos WHERE id_recurso = ?";

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRecurso);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String tipo = rs.getString("tipo_recurso");

                    return switch (tipo) {
                        case "COMPUTACAO" -> instanciaDAO.listarPorUsuario(
                                        buscarUsuarioDoRecurso(idRecurso)
                                ).stream()
                                .filter(r -> r.getId() == idRecurso)
                                .findFirst()
                                .orElse(null);

                        case "BANCO_DADOS" -> bancoDAO.listarPorUsuario(
                                        buscarUsuarioDoRecurso(idRecurso)
                                ).stream()
                                .filter(r -> r.getId() == idRecurso)
                                .findFirst()
                                .orElse(null);

                        case "STORAGE" -> bucketDAO.listarPorUsuario(
                                        buscarUsuarioDoRecurso(idRecurso)
                                ).stream()
                                .filter(r -> r.getId() == idRecurso)
                                .findFirst()
                                .orElse(null);

                        default -> null;
                    };
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar recurso: " + e.getMessage());
        }

        return null;
    }

    /**
     * Exclui um recurso detectando automaticamente o tipo.
     */
    public boolean excluirPorId(int idRecurso) {
        String sql = "SELECT tipo_recurso FROM recursos WHERE id_recurso = ?";

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRecurso);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String tipo = rs.getString("tipo_recurso");

                    return switch (tipo) {
                        case "COMPUTACAO" -> instanciaDAO.excluirPorIdRecurso(idRecurso);
                        case "BANCO_DADOS" -> bancoDAO.excluirPorIdRecurso(idRecurso);
                        case "STORAGE" -> bucketDAO.excluirPorIdRecurso(idRecurso);
                        default -> false;
                    };
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao excluir recurso: " + e.getMessage());
        }

        return false;
    }

    /**
     * Descobre o ID do usuário dono de um recurso.
     */
    private int buscarUsuarioDoRecurso(int idRecurso) {
        String sql = "SELECT usuario_id FROM recursos WHERE id_recurso = ?";

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRecurso);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("usuario_id");
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário do recurso: " + e.getMessage());
        }

        return -1;
    }
}
