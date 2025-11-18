package br.com.minicloud.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexaoBD {

    // Agora não guardamos mais a conexão em um campo estático.
    public static Connection getConexao() throws SQLException {
        try {
            Properties props = new Properties();

            // Caminho do arquivo de configuração
            FileInputStream arquivo = new FileInputStream("src/main/resources/config.properties");
            props.load(arquivo);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            // Carrega o driver do PostgreSQL (só precisa uma vez, mas não faz mal repetir)
            Class.forName("org.postgresql.Driver");

            // Cria e devolve uma NOVA conexão
            Connection conexao = DriverManager.getConnection(url, user, password);
            System.out.println("Conexão estabelecida com sucesso!");
            return conexao;

        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo config.properties: " + e.getMessage());
            throw new SQLException("Falha ao carregar config.properties", e);
        } catch (ClassNotFoundException e) {
            System.out.println("Driver do PostgreSQL não encontrado: " + e.getMessage());
            throw new SQLException("Driver do PostgreSQL não encontrado", e);
        }
    }

    public static void fecharConexao(Connection conexao) {
        if (conexao != null) {
            try {
                conexao.close();
                System.out.println("Conexão fechada com sucesso.");
            } catch (SQLException e) {
                System.out.println("Erro ao fechar a conexão: " + e.getMessage());
            }
        }
    }

    // Teste rápido
    public static void main(String[] args) {
        try (Connection conn = getConexao()) {
            // só abre e fecha
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
