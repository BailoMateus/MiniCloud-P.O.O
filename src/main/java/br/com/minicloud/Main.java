package br.com.minicloud;

import br.com.minicloud.dao.ConexaoBD;
import br.com.minicloud.dominio.GerenciadorRecursos;
import br.com.minicloud.dominio.Plano;
import br.com.minicloud.dominio.Usuario;
import br.com.minicloud.gui.TelaCadastroUsuario;
import br.com.minicloud.gui.TelaCriacaoRecursos;
import br.com.minicloud.gui.TelaListagemFatura;

import javax.swing.*;
import java.awt.*;
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

            // 2) Backend compartilhado
            GerenciadorRecursos gerenciador = new GerenciadorRecursos();

            // 3) Usuário logado simulado (depois você pode trocar por um de verdade vindo do BD)
            Plano planoPro = new Plano(1, "PRO", 1500.0, 10);
            Usuario usuarioLogado = new Usuario(
                    1,
                    "User Teste Logado",
                    "teste@minicloud.com",
                    planoPro
            );

            // 4) Abre o menu principal
            MainMenu menu = new MainMenu(gerenciador, usuarioLogado);
            menu.setVisible(true);
        });
    }
}

class MainMenu extends JFrame {

    private final GerenciadorRecursos gerenciador;
    private final Usuario usuarioLogado;

    public MainMenu(GerenciadorRecursos gerenciador, Usuario usuarioLogado) {
        super("MiniCloud - Menu Principal");

        this.gerenciador = gerenciador;
        this.usuarioLogado = usuarioLogado;

        inicializarComponentes();
        configurarLayout();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {
        // nada específico aqui por enquanto
    }

    private void configurarLayout() {
        setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("MiniCloud - Projeto POO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelBotoes = new JPanel(new GridLayout(3, 1, 10, 10));

        JButton btnCadastroUsuario = new JButton("Cadastro de Usuário");
        JButton btnCriacaoRecursos = new JButton("Criação de Recursos");
        JButton btnListagemFatura = new JButton("Listagem de Fatura");

        btnCadastroUsuario.addActionListener(e ->
                new TelaCadastroUsuario().setVisible(true)
        );

        btnCriacaoRecursos.addActionListener(e ->
                new TelaCriacaoRecursos(gerenciador, usuarioLogado)
        );

        btnListagemFatura.addActionListener(e ->
                new TelaListagemFatura(gerenciador, usuarioLogado)
        );

        panelBotoes.add(btnCadastroUsuario);
        panelBotoes.add(btnCriacaoRecursos);
        panelBotoes.add(btnListagemFatura);

        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        container.add(panelBotoes, BorderLayout.CENTER);

        add(container, BorderLayout.CENTER);
    }
}
