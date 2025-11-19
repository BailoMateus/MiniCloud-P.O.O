package br.com.minicloud;

import br.com.minicloud.dao.ConexaoBD;
import br.com.minicloud.gui.TelaLogin;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            // 1) Teste rápido de conexão
            try (Connection conn = ConexaoBD.getConexao()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Não foi possível conectar ao banco de dados.\n" +
                                    "Verifique o arquivo config.properties.",
                            "Erro de Conexão",
                            JOptionPane.ERROR_MESSAGE
                    );
                    System.exit(1);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Erro ao conectar ao banco de dados:\n" + e.getMessage(),
                        "Erro de Conexão",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }

            // 2) Abre a tela de login
            new TelaLogin().setVisible(true);
        });
    }
}
