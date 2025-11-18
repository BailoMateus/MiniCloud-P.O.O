package br.com.minicloud.dao;

import br.com.minicloud.dominio.Usuario;
import br.com.minicloud.dominio.Plano;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // CREATE – inserir usuário (já com plano definido)
    public Usuario inserirUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, email, plano_id) VALUES (?, ?, ?) RETURNING id_usuario";

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());

            if (usuario.getPlano() == null) {
                throw new IllegalArgumentException("O usuário precisa ter um plano definido para ser salvo.");
            }
            stmt.setInt(3, usuario.getPlano().getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt("id_usuario"));
                }
            }

            System.out.println("Usuário inserido com sucesso: " + usuario.getNome());
            return usuario;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuário: " + e.getMessage());
            return null;
        }
    }

    // READ – listar todos
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();

        String sql = """
                SELECT u.id_usuario,
                       u.nome,
                       u.email,
                       u.plano_id,
                       p.nome      AS nome_plano,
                       p.limite_credito,
                       p.limite_recursos
                  FROM usuarios u
                  JOIN planos p ON u.plano_id = p.id_plano
                 ORDER BY u.nome
                """;

        try (Connection conn = ConexaoBD.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Plano plano = new Plano(
                        rs.getInt("plano_id"),
                        rs.getString("nome_plano"),
                        rs.getDouble("limite_credito"),
                        rs.getInt("limite_recursos")
                );

                Usuario u = new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        plano
                );

                usuarios.add(u);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar usuários: " + e.getMessage());
        }

        return usuarios;
    }

    // READ – buscar por ID
    public Usuario buscarPorId(int id) {
        String sql = """
                SELECT u.id_usuario,
                       u.nome,
                       u.email,
                       u.plano_id,
                       p.nome      AS nome_plano,
                       p.limite_credito,
                       p.limite_recursos
                  FROM usuarios u
                  JOIN planos p ON u.plano_id = p.id_plano
                 WHERE u.id_usuario = ?
                """;

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Plano plano = new Plano(
                            rs.getInt("plano_id"),
                            rs.getString("nome_plano"),
                            rs.getDouble("limite_credito"),
                            rs.getInt("limite_recursos")
                    );

                    return new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nome"),
                            rs.getString("email"),
                            plano
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por ID: " + e.getMessage());
        }
        return null;
    }

    // UPDATE
    public boolean atualizarUsuario(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome = ?, email = ?, plano_id = ? WHERE id_usuario = ?";

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());

            if (usuario.getPlano() == null) {
                throw new IllegalArgumentException("O usuário precisa ter um plano definido para ser atualizado.");
            }
            stmt.setInt(3, usuario.getPlano().getId());

            stmt.setInt(4, usuario.getId());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Usuário atualizado com sucesso: " + usuario.getNome());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
        }
        return false;
    }

    // DELETE
    public boolean excluirUsuario(int id) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int linhas = stmt.executeUpdate();

            if (linhas > 0) {
                System.out.println("Usuário com ID " + id + " excluído com sucesso.");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Erro ao excluir usuário: " + e.getMessage());
        }
        return false;
    }
}
