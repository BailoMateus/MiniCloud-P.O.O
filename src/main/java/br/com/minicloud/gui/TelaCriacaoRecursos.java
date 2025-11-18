package br.com.minicloud.gui;

import br.com.minicloud.dominio.*;
import br.com.minicloud.dao.ConexaoBD;

import javax.swing.*;
import java.awt.*;

public class TelaCriacaoRecursos extends JFrame {

    // --- Backend e Simulação de Usuário ---
    private GerenciadorRecursos gerenciador;
    // Simulação de usuário logado com Plano Básico (limite de 3 recursos para teste)
    private Usuario usuarioLogado = new Usuario("Testador", "teste@cloud.com", new Plano("Básico", 500.0, 3));
    // --- Componentes Comuns ---
    private JComboBox<String> cmbTipoRecurso;
    private JTextField txtNomeRecurso;
    private JTextField txtCustoBaseHora;
    private JButton btnCriarRecurso;

    // --- Painéis Dinâmicos ---
    private JPanel panelDinamico;
    private CardLayout cardLayout;

    // EC2
    private JTextField txtEC2VCPUs, txtEC2Memoria;

    // S3 (BucketStorage)
    private JTextField txtS3Armazenamento, txtS3CustoPorGb;

    // RDS (BancoDadosGerenciado)
    private JTextField txtRDSArmazenamento, txtRDSCustoPorGb;
    private JCheckBox chkRDSReplicacao;

    public TelaCriacaoRecursos(GerenciadorRecursos gerenciador) {
        super("Criação de Recursos Cloud");
        this.gerenciador = gerenciador; // Usa a instância fornecida

        // Adiciona recursos iniciais ao usuário para testar o limite
        // (Isso simula recursos já existentes no banco)
        usuarioLogado.adicionarRecurso(new InstanciaComputacao(1, "Instancia_01", 0.5, 2, 4));
        usuarioLogado.adicionarRecurso(new InstanciaComputacao(2, "Instancia_02", 0.5, 2, 4));

        inicializarComponentes();
        configurarLayout();
        adicionarListeners();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // --- Subtasks: Layout ---
    private void inicializarComponentes() {
        cmbTipoRecurso = new JComboBox<>(new String[]{"Selecione o Tipo", "EC2 (Instância Computação)", "S3 (Bucket Storage)", "RDS (Banco Gerenciado)"});

        txtNomeRecurso = new JTextField(15);
        txtCustoBaseHora = new JTextField(10);
        btnCriarRecurso = new JButton("Criar Recurso");

        cardLayout = new CardLayout();
        panelDinamico = new JPanel(cardLayout);

        panelDinamico.add(new JPanel(), "Vazio");
        panelDinamico.add(criarPainelEC2(), "EC2");
        panelDinamico.add(criarPainelS3(), "S3");
        panelDinamico.add(criarPainelRDS(), "RDS");

        cardLayout.show(panelDinamico, "Vazio");
    }

    private JPanel criarPainelEC2() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Configurações EC2"));
        txtEC2VCPUs = new JTextField("2"); // Valores padrão para facilitar
        txtEC2Memoria = new JTextField("4");

        panel.add(new JLabel("vCPUs:"));
        panel.add(txtEC2VCPUs);
        panel.add(new JLabel("Memória (GB):"));
        panel.add(txtEC2Memoria);
        return panel;
    }

    private JPanel criarPainelS3() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Configurações S3"));
        txtS3Armazenamento = new JTextField("100");
        txtS3CustoPorGb = new JTextField("0.02");

        panel.add(new JLabel("Armazenamento (GB):"));
        panel.add(txtS3Armazenamento);
        panel.add(new JLabel("Custo por GB:"));
        panel.add(txtS3CustoPorGb);
        return panel;
    }

    private JPanel criarPainelRDS() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Configurações RDS"));
        txtRDSArmazenamento = new JTextField("200");
        txtRDSCustoPorGb = new JTextField("0.05");
        chkRDSReplicacao = new JCheckBox("Replicação Ativa"); // Critério: Replicação

        panel.add(new JLabel("Armazenamento (GB):"));
        panel.add(txtRDSArmazenamento);
        panel.add(new JLabel("Custo por GB:"));
        panel.add(txtRDSCustoPorGb);
        panel.add(new JLabel("Replicação:"));
        panel.add(chkRDSReplicacao);
        return panel;
    }

    private void configurarLayout() {
        setLayout(new BorderLayout(10, 10));

        // Painel Principal (Topo)
        JPanel panelGeral = new JPanel(new GridLayout(3, 2, 5, 5));
        panelGeral.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        panelGeral.add(new JLabel("Tipo de Recurso:"));
        panelGeral.add(cmbTipoRecurso);
        panelGeral.add(new JLabel("Nome:"));
        panelGeral.add(txtNomeRecurso);
        panelGeral.add(new JLabel("Custo Base Hora:"));
        panelGeral.add(txtCustoBaseHora);

        add(panelGeral, BorderLayout.NORTH);
        add(panelDinamico, BorderLayout.CENTER);

        JPanel panelSouth = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSouth.add(btnCriarRecurso);
        add(panelSouth, BorderLayout.SOUTH);
    }

    // --- Subtasks: Eventos e Integração Service ---

    private void adicionarListeners() {
        cmbTipoRecurso.addActionListener(e -> {
            String tipo = (String) cmbTipoRecurso.getSelectedItem();
            String card = "Vazio";
            if (tipo.startsWith("EC2")) {
                card = "EC2";
            } else if (tipo.startsWith("S3")) {
                card = "S3";
            } else if (tipo.startsWith("RDS")) {
                card = "RDS";
            }
            cardLayout.show(panelDinamico, card);
            limparCamposDinamicos();
        });

        btnCriarRecurso.addActionListener(e -> criarNovoRecurso());
    }

    private void criarNovoRecurso() {
        String tipo = (String) cmbTipoRecurso.getSelectedItem();
        String nome = txtNomeRecurso.getText();
        String custoBaseHoraStr = txtCustoBaseHora.getText();
        RecursoCloud persistido = null;

        try {
            if (tipo.startsWith("Selecione") || nome.isEmpty() || custoBaseHoraStr.isEmpty()) {
                exibirErro("Preencha o tipo, nome e custo base.");
                return;
            }
            double custoBaseHora = Double.parseDouble(custoBaseHoraStr);

            // 1. Chamar o método específico no GerenciadorRecursos
            if (tipo.startsWith("EC2")) {
                int vcpus = Integer.parseInt(txtEC2VCPUs.getText());
                int memoria = Integer.parseInt(txtEC2Memoria.getText());
                persistido = gerenciador.criarInstanciaComputacao(usuarioLogado, nome, custoBaseHora, vcpus, memoria);
            } else if (tipo.startsWith("S3")) {
                int armazenamento = Integer.parseInt(txtS3Armazenamento.getText());
                double custoPorGb = Double.parseDouble(txtS3CustoPorGb.getText());
                persistido = gerenciador.criarBucketStorage(usuarioLogado, nome, custoBaseHora, armazenamento, custoPorGb);
            } else if (tipo.startsWith("RDS")) {
                int armazenamento = Integer.parseInt(txtRDSArmazenamento.getText());
                boolean replicacao = chkRDSReplicacao.isSelected();
                double custoPorGb = Double.parseDouble(txtRDSCustoPorGb.getText());
                persistido = gerenciador.criarBancoDadosGerenciado(usuarioLogado, nome, custoBaseHora, armazenamento, replicacao, custoPorGb);
            }

            // 2. Critério: Mensagens de sucesso/erro e Validação do Plano
            if (persistido != null) {
                exibirSucesso("Recurso '" + persistido.getNome() + "' criado com sucesso! (Recursos atuais: " + usuarioLogado.getRecursos().size() + ")");
                limparTodosCampos();
                // Critério: Atualização automática da lista de recursos
                // A GUI de listagem teria que ser notificada aqui.
            } else {
                // Se o GerenciadorRecursos retornar null, é porque o limite foi atingido
                exibirErro("Falha na criação: Limite de recursos do plano atingido (" + usuarioLogado.getPlano().getLimiteRecursos() + " recursos).");
            }

        } catch (NumberFormatException ex) {
            exibirErro("Entrada inválida. Verifique se os campos numéricos estão corretos.");
        } catch (Exception ex) {
            exibirErro("Erro de Criação: " + ex.getMessage());
        }
    }

    private void limparCamposDinamicos() {
        txtEC2VCPUs.setText("");
        txtEC2Memoria.setText("");
        txtS3Armazenamento.setText("");
        txtS3CustoPorGb.setText("");
        txtRDSArmazenamento.setText("");
        txtRDSCustoPorGb.setText("");
        chkRDSReplicacao.setSelected(false);
    }

    private void limparTodosCampos() {
        cmbTipoRecurso.setSelectedIndex(0);
        txtNomeRecurso.setText("");
        txtCustoBaseHora.setText("");
        limparCamposDinamicos();
        cardLayout.show(panelDinamico, "Vazio");
    }

    private void exibirErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "ERRO", JOptionPane.ERROR_MESSAGE);
    }

    private void exibirSucesso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        // Inicializa o Gerenciador de Recursos e passa para a tela
        GerenciadorRecursos gr = new GerenciadorRecursos();
        SwingUtilities.invokeLater(() -> new TelaCriacaoRecursos(gr));
    }
}