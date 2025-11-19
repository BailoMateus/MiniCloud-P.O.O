package br.com.minicloud.gui;

import br.com.minicloud.MainMenu;
import br.com.minicloud.dao.UsuarioDAO;
import br.com.minicloud.dominio.GerenciadorRecursos;
import br.com.minicloud.dominio.Usuario;

import javax.swing.*;
import java.awt.*;

public class TelaLogin extends JFrame {

    private JTextField txtEmail;
    private JButton btnLogin;
    private JButton btnCadastrar;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    public TelaLogin() {
        super("MiniCloud - Login");

        inicializarComponentes();
        configurarLayout();
        adicionarListeners();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 220);
        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {
        txtEmail = new JTextField(20);

        btnLogin = new JButton("Entrar");
        btnCadastrar = new JButton("Criar Usuário");
    }

    private void configurarLayout() {
        setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("Acessar MiniCloud", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridLayout(2, 1, 5, 5));
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));

        panelForm.add(new JLabel("E-mail do usuário:"));
        panelForm.add(txtEmail);

        add(panelForm, BorderLayout.CENTER);

        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotoes.add(btnCadastrar);
        panelBotoes.add(btnLogin);

        add(panelBotoes, BorderLayout.SOUTH);
    }

    private void adicionarListeners() {
        btnLogin.addActionListener(e -> fazerLogin());
        btnCadastrar.addActionListener(e -> abrirCadastroUsuario());
    }

    private void fazerLogin() {
        String email = txtEmail.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Informe o e-mail do usuário.",
                    "Campo obrigatório",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario usuario = usuarioDAO.buscarPorEmail(email);

        if (usuario == null) {
            JOptionPane.showMessageDialog(this,
                    "Usuário não encontrado para o e-mail informado.",
                    "Erro de login",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        GerenciadorRecursos gerenciador = new GerenciadorRecursos();
        MainMenu menu = new MainMenu(gerenciador, usuario);
        menu.setVisible(true);

        dispose();
        }

    private void abrirCadastroUsuario() {
        // passa a própria TelaLogin como telaAnterior
        new TelaCadastroUsuario(this).setVisible(true);
        this.setVisible(false);
    }
}
