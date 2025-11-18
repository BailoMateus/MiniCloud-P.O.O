package br.com.minicloud.gui;

import br.com.minicloud.dominio.Usuario;
import br.com.minicloud.dao.UsuarioDAO; // Importa o DAO
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

// Importa ConexaoBD para fechar a conexão ao sair (boa prática)
import br.com.minicloud.dao.ConexaoBD;

public class TelaCadastroUsuario extends JFrame {

    // Componentes da GUI (Inputs e Botões)
    private JTextField txtId, txtNome, txtEmail;
    private JPasswordField txtSenha;
    private JButton btnCriar, btnAtualizar, btnExcluir, btnListar;
    private JTable tabelaUsuarios;
    private DefaultTableModel tableModel;

    // Instância do DAO para interação com o banco de dados
    private UsuarioDAO usuarioDAO;

    public TelaCadastroUsuario() {
        super("Cadastro de Usuários");
        this.usuarioDAO = new UsuarioDAO();
        inicializarComponentes();
        configurarLayout();
        adicionarListeners();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Centraliza a tela
        setVisible(true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        // Chama a listagem inicial para popular a tabela ao iniciar
        listarUsuarios();
    }

    // --- Subtasks: Layout da tela ---
    private void inicializarComponentes() {
        // Campos de entrada
        txtId = new JTextField(5);
        txtId.setEditable(false); // O ID é gerado pelo BD
        txtNome = new JTextField(20);
        txtEmail = new JTextField(20);
        txtSenha = new JPasswordField(20);

        // Botões
        btnCriar = new JButton("Criar");
        btnAtualizar = new JButton("Atualizar");
        btnExcluir = new JButton("Excluir");
        btnListar = new JButton("Listar");

        // Tabela
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nome", "Email"}, 0);
        tabelaUsuarios = new JTable(tableModel);

        // Listener para preencher campos ao selecionar uma linha
        tabelaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabelaUsuarios.getSelectedRow() != -1) {
                preencherCamposComSelecao();
            }
        });
    }

    private void configurarLayout() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(10, 10));

        // Painel de Formulário (Norte)
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Dados do Usuário"));

        formPanel.add(new JLabel("ID:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Nome:"));
        formPanel.add(txtNome);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(txtEmail);
        formPanel.add(new JLabel("Senha:"));
        formPanel.add(txtSenha);

        // Painel de Botões (Sul do Painel de Formulário)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(btnCriar);
        buttonPanel.add(btnAtualizar);
        buttonPanel.add(btnExcluir);
        buttonPanel.add(btnListar);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        contentPane.add(topPanel, BorderLayout.NORTH);

        // Tabela de Usuários (Centro)
        JScrollPane scrollPane = new JScrollPane(tabelaUsuarios);
        contentPane.add(scrollPane, BorderLayout.CENTER);
    }

    // --- Subtasks: Lógica de eventos e Integração com UsuarioDAO ---

    private void adicionarListeners() {
        btnCriar.addActionListener(e -> criarUsuario());
        btnAtualizar.addActionListener(e -> atualizarUsuario());
        btnExcluir.addActionListener(e -> excluirUsuario());
        btnListar.addActionListener(e -> listarUsuarios());
    }

    private void preencherCamposComSelecao() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();
        if (linhaSelecionada != -1) {
            // A coluna 0 é o ID, 1 é o Nome, 2 é o Email
            String id = tableModel.getValueAt(linhaSelecionada, 0).toString();
            String nome = tableModel.getValueAt(linhaSelecionada, 1).toString();
            String email = tableModel.getValueAt(linhaSelecionada, 2).toString();

            // Preenche os campos para edição/exclusão
            txtId.setText(id);
            txtNome.setText(nome);
            txtEmail.setText(email);
            txtSenha.setText(""); // Não preenche a senha por segurança
        }
    }

    private void criarUsuario() {
        try {
            String nome = txtNome.getText();
            String email = txtEmail.getText();
            String senha = new String(txtSenha.getPassword());

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                exibirErro("Todos os campos (Nome, Email, Senha) são obrigatórios para a criação.");
                return;
            }

            Usuario novoUsuario = new Usuario(nome, email, senha);
            Usuario resultado = usuarioDAO.inserirUsuario(novoUsuario);

            if (resultado != null) {
                exibirSucesso("Usuário '" + resultado.getNome() + "' criado com sucesso!");
                limparCampos();
                listarUsuarios(); // Atualiza a lista
            }

        } catch (Exception ex) {
            exibirErro("Erro ao criar usuário: " + ex.getMessage());
        }
    }

    private void atualizarUsuario() {
        try {
            int id = Integer.parseInt(txtId.getText());
            String nome = txtNome.getText();
            String email = txtEmail.getText();
            String senha = new String(txtSenha.getPassword());

            // Regra: Se a senha estiver vazia, não atualiza (mantém a antiga)
            if (senha.isEmpty()) {
                Usuario uExistente = usuarioDAO.buscarPorId(id);
                if(uExistente != null) {
                    senha = uExistente.getSenha(); // Pega a senha atual do BD
                } else {
                    exibirErro("Usuário não encontrado para atualização.");
                    return;
                }
            }

            if (nome.isEmpty() || email.isEmpty()) {
                exibirErro("Os campos Nome e Email são obrigatórios para a atualização.");
                return;
            }

            Usuario usuarioAtualizar = new Usuario(id, nome, email, senha);
            boolean sucesso = usuarioDAO.atualizarUsuario(usuarioAtualizar);

            if (sucesso) {
                exibirSucesso("Usuário ID " + id + " atualizado com sucesso!");
                limparCampos();
                listarUsuarios(); // Atualiza a lista
            } else {
                exibirErro("Falha ao atualizar usuário. Verifique o ID.");
            }

        } catch (NumberFormatException | NullPointerException e) {
            exibirErro("Selecione um usuário na tabela (ou preencha o ID) para atualizar.");
        } catch (Exception ex) {
            exibirErro("Erro ao atualizar usuário: " + ex.getMessage());
        }
    }

    private void excluirUsuario() {
        try {
            int id = Integer.parseInt(txtId.getText());

            int confirmacao = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja excluir o usuário ID " + id + "?",
                    "Confirmação de Exclusão", JOptionPane.YES_NO_OPTION);

            if (confirmacao == JOptionPane.YES_OPTION) {
                boolean sucesso = usuarioDAO.excluirUsuario(id);
                if (sucesso) {
                    exibirSucesso("Usuário ID " + id + " excluído com sucesso!");
                    limparCampos();
                    listarUsuarios(); // Atualiza a lista
                } else {
                    exibirErro("Falha ao excluir usuário. Verifique o ID.");
                }
            }

        } catch (NumberFormatException | NullPointerException e) {
            exibirErro("Selecione um usuário na tabela (ou preencha o ID) para excluir.");
        } catch (Exception ex) {
            exibirErro("Erro ao excluir usuário: " + ex.getMessage());
        }
    }

    private void listarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioDAO.listarTodos();

            // Limpa a tabela antes de popular
            tableModel.setRowCount(0);

            for (Usuario u : usuarios) {
                // Adiciona ID, Nome e Email na tabela (não a Senha)
                tableModel.addRow(new Object[]{u.getId(), u.getNome(), u.getEmail()});
            }
            if (usuarios.isEmpty()) {
                exibirInformacao("Nenhum usuário cadastrado.");
            }

        } catch (Exception ex) {
            exibirErro("Erro ao listar usuários: " + ex.getMessage());
        }
    }

    private void limparCampos() {
        txtId.setText("");
        txtNome.setText("");
        txtEmail.setText("");
        txtSenha.setText("");
    }

    // Métodos para Exibir Erros/Exceções com pop-up amigável (Critério de Aceitação)
    private void exibirErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "ERRO", JOptionPane.ERROR_MESSAGE);
    }

    private void exibirSucesso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exibirInformacao(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    // Método Main para iniciar a tela
    public static void main(String[] args) {
        // Garantindo que a GUI rode na Thread de Eventos do Swing (EDT)
        SwingUtilities.invokeLater(TelaCadastroUsuario::new);
    }
}