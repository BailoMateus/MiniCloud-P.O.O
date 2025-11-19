package br.com.minicloud;

import br.com.minicloud.dominio.GerenciadorRecursos;
import br.com.minicloud.dominio.Usuario;
import br.com.minicloud.gui.TelaCadastroUsuario;
import br.com.minicloud.gui.TelaCriacaoRecursos;
import br.com.minicloud.gui.TelaGerenciarPlano;
import br.com.minicloud.gui.TelaListagemFatura;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

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
        JButton btnGerenciarPlano = new JButton("Gerenciar Plano");

        btnCadastroUsuario.addActionListener(e ->
                new TelaCadastroUsuario(this).setVisible(true)
        );

        btnCriacaoRecursos.addActionListener(e ->
                new TelaCriacaoRecursos(gerenciador, usuarioLogado)
        );

        btnListagemFatura.addActionListener(e ->
                new TelaListagemFatura(gerenciador, usuarioLogado)
        );

        btnGerenciarPlano.addActionListener(e ->
                new TelaGerenciarPlano(usuarioLogado)
        );

        panelBotoes.add(btnCadastroUsuario);
        panelBotoes.add(btnCriacaoRecursos);
        panelBotoes.add(btnListagemFatura);
        panelBotoes.add(btnGerenciarPlano);

        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        container.add(panelBotoes, BorderLayout.CENTER);

        add(container, BorderLayout.CENTER);
    }
}
