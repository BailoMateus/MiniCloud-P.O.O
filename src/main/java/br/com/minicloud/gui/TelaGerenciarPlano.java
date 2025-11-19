package br.com.minicloud.gui;

import br.com.minicloud.dao.PlanoDAO;
import br.com.minicloud.dao.UsuarioDAO;
import br.com.minicloud.dominio.Plano;
import br.com.minicloud.dominio.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TelaGerenciarPlano extends JFrame {

    private final Usuario usuarioLogado;
    private final PlanoDAO planoDAO = new PlanoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private JLabel lblPlanoAtual;
    private JLabel lblLimiteCreditoAtual;
    private JLabel lblLimiteRecursosAtual;

    private JComboBox<String> comboPlanos;
    private JLabel lblLimiteCreditoNovo;
    private JLabel lblLimiteRecursosNovo;

    private JButton btnSalvar;
    private JButton btnCancelar;

    private List<Plano> planosDisponiveis;

    public TelaGerenciarPlano(Usuario usuarioLogado) {
        super("Gerenciar Plano - " + usuarioLogado.getNome());
        this.usuarioLogado = usuarioLogado;

        inicializarComponentes();
        configurarLayout();
        carregarPlanos();
        adicionarListeners();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void inicializarComponentes() {
        lblPlanoAtual = new JLabel();
        lblLimiteCreditoAtual = new JLabel();
        lblLimiteRecursosAtual = new JLabel();

        comboPlanos = new JComboBox<>();
        lblLimiteCreditoNovo = new JLabel();
        lblLimiteRecursosNovo = new JLabel();

        btnSalvar = new JButton("Salvar Alteração");
        btnCancelar = new JButton("Cancelar");
    }

    private void configurarLayout() {
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitulo = new JLabel("Alterar Plano do Usuário", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new GridLayout(6, 2, 5, 5));
        panelCentro.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Plano atual
        panelCentro.add(new JLabel("Plano atual:"));
        panelCentro.add(lblPlanoAtual);

        panelCentro.add(new JLabel("Crédito atual (R$):"));
        panelCentro.add(lblLimiteCreditoAtual);

        panelCentro.add(new JLabel("Limite de recursos atual:"));
        panelCentro.add(lblLimiteRecursosAtual);

        // Plano novo
        panelCentro.add(new JLabel("Novo plano:"));
        panelCentro.add(comboPlanos);

        panelCentro.add(new JLabel("Crédito do novo plano (R$):"));
        panelCentro.add(lblLimiteCreditoNovo);

        panelCentro.add(new JLabel("Limite de recursos do novo plano:"));
        panelCentro.add(lblLimiteRecursosNovo);

        add(panelCentro, BorderLayout.CENTER);

        JPanel panelSul = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSul.add(btnCancelar);
        panelSul.add(btnSalvar);
        add(panelSul, BorderLayout.SOUTH);
    }

    private void carregarPlanos() {
        planosDisponiveis = planoDAO.listarTodos();

        if (planosDisponiveis == null || planosDisponiveis.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nenhum plano cadastrado no sistema.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // Preenche combo com nomes
        comboPlanos.removeAllItems();
        int indicePlanoAtual = -1;

        for (int i = 0; i < planosDisponiveis.size(); i++) {
            Plano p = planosDisponiveis.get(i);
            comboPlanos.addItem(p.getNome());

            if (usuarioLogado.getPlano() != null &&
                    p.getId() == usuarioLogado.getPlano().getId()) {
                indicePlanoAtual = i;
            }
        }

        // Mostra dados do plano atual
        atualizarInfoPlanoAtual();

        // Seleciona o plano atual no combo
        if (indicePlanoAtual >= 0) {
            comboPlanos.setSelectedIndex(indicePlanoAtual);
        } else {
            comboPlanos.setSelectedIndex(0);
        }

        atualizarInfoPlanoNovo();
    }

    private void atualizarInfoPlanoAtual() {
        Plano planoAtual = usuarioLogado.getPlano();

        if (planoAtual == null) {
            lblPlanoAtual.setText("Sem plano");
            lblLimiteCreditoAtual.setText("-");
            lblLimiteRecursosAtual.setText("-");
        } else {
            lblPlanoAtual.setText(planoAtual.getNome());
            lblLimiteCreditoAtual.setText(String.format("%.2f", planoAtual.getLimiteCredito()));
            lblLimiteRecursosAtual.setText(String.valueOf(planoAtual.getLimiteRecursos()));
        }
    }

    private void atualizarInfoPlanoNovo() {
        int idx = comboPlanos.getSelectedIndex();
        if (idx < 0 || planosDisponiveis == null || idx >= planosDisponiveis.size()) {
            lblLimiteCreditoNovo.setText("-");
            lblLimiteRecursosNovo.setText("-");
            return;
        }

        Plano selecionado = planosDisponiveis.get(idx);
        lblLimiteCreditoNovo.setText(String.format("%.2f", selecionado.getLimiteCredito()));
        lblLimiteRecursosNovo.setText(String.valueOf(selecionado.getLimiteRecursos()));
    }

    private void adicionarListeners() {
        comboPlanos.addActionListener(e -> atualizarInfoPlanoNovo());

        btnCancelar.addActionListener(e -> dispose());

        btnSalvar.addActionListener(e -> salvarAlteracaoPlano());
    }

    private void salvarAlteracaoPlano() {
        int idx = comboPlanos.getSelectedIndex();
        if (idx < 0 || planosDisponiveis == null || idx >= planosDisponiveis.size()) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um plano válido.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Plano planoSelecionado = planosDisponiveis.get(idx);

        if (usuarioLogado.getPlano() != null &&
                planoSelecionado.getId() == usuarioLogado.getPlano().getId()) {
            JOptionPane.showMessageDialog(this,
                    "O usuário já está nesse plano.",
                    "Nenhuma alteração",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Atualiza em memória
        usuarioLogado.setPlano(planoSelecionado);

        // Atualiza no banco
        boolean ok = usuarioDAO.atualizarUsuario(usuarioLogado);
        if (!ok) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao atualizar plano do usuário.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Plano atualizado com sucesso para: " + planoSelecionado.getNome(),
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);

        atualizarInfoPlanoAtual();
        dispose();
    }
}
