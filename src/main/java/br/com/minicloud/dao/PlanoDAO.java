package br.com.minicloud.dao;

import br.com.minicloud.dominio.Plano;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanoDAO {

    public List<Plano> listarTodos() {
        List<Plano> planos = new ArrayList<>();

        String sql = "SELECT id_plano, nome, limite_credito, limite_recursos FROM planos ORDER BY id_plano";

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Plano plano = new Plano();
                plano.setId(rs.getInt("id_plano"));
                plano.setNome(rs.getString("nome"));
                plano.setLimiteCredito(rs.getDouble("limite_credito"));
                plano.setLimiteRecursos(rs.getInt("limite_recursos"));

                planos.add(plano);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar planos: " + e.getMessage());
        }

        return planos;
    }

    public void inserir(Plano plano) {
        String sql = "INSERT INTO planos (nome, limite_credito, limite_recursos) VALUES (?, ?, ?)";

        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, plano.getNome());
            stmt.setDouble(2, plano.getLimiteCredito());
            stmt.setInt(3, plano.getLimiteRecursos());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    plano.setId(idGerado);
                }
            }

            System.out.println("Plano inserido com sucesso: " + plano.getNome());

        } catch (SQLException e) {
            System.out.println("Erro ao inserir plano: " + e.getMessage());
        }
    }
}
