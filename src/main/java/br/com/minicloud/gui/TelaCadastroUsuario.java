package br.com.minicloud.gui;

import br.com.minicloud.dao.UsuarioDAO;
import br.com.minicloud.dominio.Plano;
import br.com.minicloud.dominio.Usuario;

import javax.swing.*;
import java.awt.*;

public class TelaCadastroUsuario extends JFrame {

    private JTextField txtNome;
    private JTextField txtEmail;
    private JButton btnSalvar;
    private JButton btnCancelar;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Janela que abriu esta tela (pode ser TelaLogin ou MainMenu)
    private final JFrame telaAnterior;

    // Construtor “genérico”: pode ser chamado por qualquer tela
    public TelaCadastroUsuario(JFrame telaAnterior) {
        super("Cadastro de Usuário");
        this.telaAnterior = telaAnterior;

        inicializarComponentes();
        configurarLayout();
        adicionarListeners();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // fecha só esta tela
        setSize(400, 220);
        setLocationRelativeTo(null);
    }

    // Construtor sem parâmetro (se alguém ainda usar)
    public TelaCadastroUsuario() {
        this(null);
    }

    private void inicializarComponentes() {
        txtNome = new JTextField(20);
        txtEmail = new JTextField(20);

        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");
    }

    private void configurarLayout() {
        setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("Cadastrar novo usuário", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridLayout(4, 1, 5, 5));
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));

        panelForm.add(new JLabel("Nome:"));
        panelForm.add(txtNome);

        panelForm.add(new JLabel("E-mail:"));
        panelForm.add(txtEmail);

        add(panelForm, BorderLayout.CENTER);

        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotoes.add(btnCancelar);
        panelBotoes.add(btnSalvar);

        add(panelBotoes, BorderLayout.SOUTH);
    }

    private void adicionarListeners() {
        btnSalvar.addActionListener(e -> salvarUsuario());
        btnCancelar.addActionListener(e -> voltarTelaAnterior());
    }

    private void salvarUsuario() {
        String nome = txtNome.getText().trim();
        String email = txtEmail.getText().trim();

        if (nome.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Preencha nome e e-mail.",
                    "Campos obrigatórios",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Usuário sem plano explícito: o DAO colocará o plano FREE automaticamente
        Usuario novo = new Usuario(nome, email, (Plano)null);

        Usuario salvo = usuarioDAO.inserirUsuario(novo);

        if (salvo == null) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao criar usuário. Verifique o console/log.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Usuário cadastrado com sucesso!\nPlano inicial: FREE",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);

        voltarTelaAnterior();
    }

    private void voltarTelaAnterior() {
        // Se foi aberta por alguma tela (login ou menu), volta para ela
        if (telaAnterior != null) {
            telaAnterior.setVisible(true);
        }
        dispose();
    }
}
