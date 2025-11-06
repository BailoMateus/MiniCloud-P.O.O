package br.com.minicloud.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexaoBD {

    private static Connection conexao = null;

    public static Connection getConexao() {
        if (conexao == null) {
            try {
                Properties props = new Properties();

                // Lê o arquivo de configuração
                FileInputStream arquivo = new FileInputStream("src/main/resources/config.properties");
                props.load(arquivo);

                String url = props.getProperty("db.url");
                String user = props.getProperty("db.user");
                String password = props.getProperty("db.password");

                // Carrega o driver do PostgreSQL
                Class.forName("org.postgresql.Driver");

                // Conecta ao PostgreSQL
                conexao = DriverManager.getConnection(url, user, password);
                System.out.println(" Conexão estabelecida com sucesso!");

            } catch (IOException e) {
                System.out.println(" Erro ao ler o arquivo config.properties: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                System.out.println(" Driver do PostgreSQL não encontrado: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println(" Erro ao conectar ao banco de dados: " + e.getMessage());
            }
        }
        return conexao;
    }

    public static void fecharConexao() {
        if (conexao != null) {
            try {
                conexao.close();
                conexao = null;
                System.out.println(" Conexão fechada com sucesso.");
            } catch (SQLException e) {
                System.out.println(" Erro ao fechar a conexão: " + e.getMessage());
            }
        }
    }

    // Teste rápido de conexão
    public static void main(String[] args) {
        getConexao();    // tenta conectar
        fecharConexao(); // fecha ao final
    }
}
