package br.com.minicloud.gui;

import br.com.minicloud.dominio.*;
import br.com.minicloud.exceptions.LimiteRecursosPlanoException;

import javax.swing.*;
import java.awt.*;

public class TelaCriacaoRecursos extends JFrame {

    private final GerenciadorRecursos gerenciador;
    private final Usuario usuarioLogado;

    // --- Componentes da Tela ---
    private JComboBox<String> cmbTipoRecurso;
    private JPanel panelFormulario;
    private JPanel panelCamposEspecificos;
    private JTextField txtNome;
    private JTextField txtCustoBaseHora;
    private JButton btnCriar;

    // --- Componentes Dinâmicos ---
    private JTextField txtVcpus;
    private JTextField txtMemoriaGb;

    private JTextField txtArmazenamentoDb;
    private JCheckBox chkReplicacaoAtiva;
    private JTextField txtCustoPorGbDb;

    private JTextField txtArmazenamentoBucket;
    private JTextField txtCustoPorGbBucket;

    public TelaCriacaoRecursos(GerenciadorRecursos gerenciador, Usuario usuarioLogado) {
        super("Criar Novo Recurso Cloud - " + usuarioLogado.getNome());

        this.gerenciador = gerenciador;
        this.usuarioLogado = usuarioLogado;

        inicializarComponentes();
        configurarLayout();
        adicionarListeners();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 550);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ============================
    // Inicialização de componentes
    // ============================

    private void inicializarComponentes() {
        String[] tipos = {
                "Instância de Computação (EC2)",
                "Banco de Dados Gerenciado (RDS)",
                "Bucket de Storage (S3)"
        };
        cmbTipoRecurso = new JComboBox<>(tipos);

        panelFormulario = new JPanel();
        panelFormulario.setLayout(new BoxLayout(panelFormulario, BoxLayout.Y_AXIS));
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtNome = new JTextField(20);
        txtCustoBaseHora = new JTextField(10);

        panelCamposEspecificos = new JPanel();
        panelCamposEspecificos.setLayout(new BoxLayout(panelCamposEspecificos, BoxLayout.Y_AXIS));

        btnCriar = new JButton("Criar Recurso");

        // Campos específicos começam como EC2
        atualizarFormulario();
    }

    private void configurarLayout() {
        setLayout(new BorderLayout(10, 10));

        // Cabeçalho
        JLabel lblTitulo = new JLabel(
                "Criar Recurso para: " + usuarioLogado.getNome() +
                        " | Plano: " + (usuarioLogado.getPlano() != null ? usuarioLogado.getPlano().getNome() : "Não definido"),
                SwingConstants.CENTER
        );
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(lblTitulo, BorderLayout.NORTH);

        // Corpo
        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));

        JPanel panelTipo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTipo.add(new JLabel("Tipo de Recurso:"));
        panelTipo.add(cmbTipoRecurso);

        panelCentro.add(panelTipo, BorderLayout.NORTH);

        // Formulário
        panelFormulario.removeAll();
        panelFormulario.add(criarLinhaCampo("Nome do Recurso:", txtNome));
        panelFormulario.add(criarLinhaCampo("Custo Base por Hora:", txtCustoBaseHora));
        panelFormulario.add(Box.createVerticalStrut(10));
        panelFormulario.add(panelCamposEspecificos);

        panelCentro.add(panelFormulario, BorderLayout.CENTER);

        add(panelCentro, BorderLayout.CENTER);

        // Rodapé
        JPanel panelSul = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSul.add(btnCriar);
        add(panelSul, BorderLayout.SOUTH);
    }

    private JPanel criarLinhaCampo(String rotulo, JComponent campo) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JLabel label = new JLabel(rotulo);
        panel.add(label, BorderLayout.WEST);
        panel.add(campo, BorderLayout.CENTER);
        return panel;
    }

    // ============================
    // Listeners
    // ============================

    private void adicionarListeners() {
        cmbTipoRecurso.addActionListener(e -> atualizarFormulario());

        btnCriar.addActionListener(e -> criarRecurso());
    }

    // ============================
    // Atualização dinâmica do formulário
    // ============================

    private void atualizarFormulario() {
        panelCamposEspecificos.removeAll();

        String tipo = (String) cmbTipoRecurso.getSelectedItem();
        if (tipo == null) return;

        switch (tipo) {
            case "Instância de Computação (EC2)":
                adicionarCamposEC2();
                break;
            case "Banco de Dados Gerenciado (RDS)":
                adicionarCamposRDS();
                break;
            case "Bucket de Storage (S3)":
                adicionarCamposS3();
                break;
        }

        panelCamposEspecificos.revalidate();
        panelCamposEspecificos.repaint();
    }

    private void adicionarCamposEC2() {
        txtVcpus = new JTextField(10);
        txtMemoriaGb = new JTextField(10);

        panelCamposEspecificos.add(criarLinhaCampo("vCPUs:", txtVcpus));
        panelCamposEspecificos.add(criarLinhaCampo("Memória (GB):", txtMemoriaGb));
    }

    private void adicionarCamposRDS() {
        txtArmazenamentoDb = new JTextField(10);
        chkReplicacaoAtiva = new JCheckBox("Replicação Ativa");
        txtCustoPorGbDb = new JTextField(10);

        panelCamposEspecificos.add(criarLinhaCampo("Armazenamento (GB):", txtArmazenamentoDb));

        JPanel panelCheck = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCheck.add(chkReplicacaoAtiva);
        panelCamposEspecificos.add(panelCheck);

        panelCamposEspecificos.add(criarLinhaCampo("Custo por GB:", txtCustoPorGbDb));
    }

    private void adicionarCamposS3() {
        txtArmazenamentoBucket = new JTextField(10);
        txtCustoPorGbBucket = new JTextField(10);

        panelCamposEspecificos.add(criarLinhaCampo("Armazenamento (GB):", txtArmazenamentoBucket));
        panelCamposEspecificos.add(criarLinhaCampo("Custo por GB:", txtCustoPorGbBucket));
    }

    // ============================
    // Criação de recurso
    // ============================

    private void criarRecurso() {
        String tipo = (String) cmbTipoRecurso.getSelectedItem();
        String nome = txtNome.getText().trim();
        String custoStr = txtCustoBaseHora.getText().trim();

        if (nome.isEmpty() || custoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Preencha pelo menos Nome e Custo Base por Hora.",
                    "Campos obrigatórios",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        double custoBaseHora;
        try {
            custoBaseHora = Double.parseDouble(custoStr.replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Custo Base por Hora inválido.",
                    "Erro de formato",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if ("Instância de Computação (EC2)".equals(tipo)) {
                int vcpus = Integer.parseInt(txtVcpus.getText().trim());
                int memoria = Integer.parseInt(txtMemoriaGb.getText().trim());

                gerenciador.criarInstanciaComputacao(
                        usuarioLogado,
                        nome,
                        custoBaseHora,
                        vcpus,
                        memoria
                );

            } else if ("Banco de Dados Gerenciado (RDS)".equals(tipo)) {
                int armazenamento = Integer.parseInt(txtArmazenamentoDb.getText().trim());
                boolean replicacao = chkReplicacaoAtiva.isSelected();
                double custoPorGb = Double.parseDouble(txtCustoPorGbDb.getText().trim().replace(",", "."));

                gerenciador.criarBancoDadosGerenciado(
                        usuarioLogado,
                        nome,
                        custoBaseHora,
                        armazenamento,
                        replicacao,
                        custoPorGb
                );

            } else if ("Bucket de Storage (S3)".equals(tipo)) {
                int armazenamento = Integer.parseInt(txtArmazenamentoBucket.getText().trim());
                double custoPorGb = Double.parseDouble(txtCustoPorGbBucket.getText().trim().replace(",", "."));

                gerenciador.criarBucketStorage(
                        usuarioLogado,
                        nome,
                        custoBaseHora,
                        armazenamento,
                        custoPorGb
                );
            }

            JOptionPane.showMessageDialog(this,
                    "Recurso criado com sucesso para o usuário " + usuarioLogado.getNome() + "!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            limparCampos();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Um dos campos numéricos está inválido.",
                    "Erro de formato",
                    JOptionPane.ERROR_MESSAGE);
        } catch (LimiteRecursosPlanoException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Limite de recursos atingido",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtCustoBaseHora.setText("");
        if (txtVcpus != null) txtVcpus.setText("");
        if (txtMemoriaGb != null) txtMemoriaGb.setText("");
        if (txtArmazenamentoDb != null) txtArmazenamentoDb.setText("");
        if (txtCustoPorGbDb != null) txtCustoPorGbDb.setText("");
        if (chkReplicacaoAtiva != null) chkReplicacaoAtiva.setSelected(false);
        if (txtArmazenamentoBucket != null) txtArmazenamentoBucket.setText("");
        if (txtCustoPorGbBucket != null) txtCustoPorGbBucket.setText("");
    }
}
