package br.com.minicloud.gui;

import br.com.minicloud.dominio.*; // IMPORT GERAL para Usuario, Plano, RecursoCloud, etc.
import br.com.minicloud.dominio.GerenciadorRecursos; // CORREÇÃO 1: Assumindo que GerenciadorRecursos está em .dominio
import br.com.minicloud.dao.*;

import javax.swing.*;
import java.awt.*;

public class TelaCriacaoRecursos extends JFrame {

    // CORREÇÃO 2: Usando o construtor completo do Plano (ID, Nome, Crédito, Limite Recursos)
    private Usuario usuarioLogado = new Usuario(
            1, // ID Simulado
            "User Teste Logado",
            "teste@minicloud.com",
            new Plano(1, "PRO", 1500.0, 10)
    );

    // --- Backend e DAOs ---
    private GerenciadorRecursos gerenciador;
    private InstanciaComputacaoDAO icDAO = new InstanciaComputacaoDAO();
    private BancoDadosGerenciadoDAO dbDAO = new BancoDadosGerenciadoDAO();
    private BucketStorageDAO bucketDAO = new BucketStorageDAO();

    // --- Componentes da Tela (mantidos) ---
    private JComboBox<String> cmbTipoRecurso;
    private JPanel panelFormulario;
    private JTextField txtNome;
    private JTextField txtCustoBaseHora;
    private JButton btnCriar;

    // --- Componentes Dinâmicos (mantidos) ---
    private JTextField txtVcpus;
    private JTextField txtMemoriaGb;
    private JTextField txtArmazenamentoDb;
    private JCheckBox chkReplicacaoAtiva;
    private JTextField txtCustoPorGbDb;
    private JTextField txtArmazenamentoBucket;
    private JTextField txtCustoPorGbBucket;


    public TelaCriacaoRecursos(GerenciadorRecursos gerenciador) {
        // CORREÇÃO 3: O super() deve vir antes de qualquer uso de variáveis de instância.
        // A string do título pode ser construída depois.
        super("Criar Novo Recurso Cloud");

        // Agora podemos chamar o getNome()
        this.setTitle("Criar Novo Recurso Cloud - " + usuarioLogado.getNome());

        this.gerenciador = gerenciador;

        // Simulação de dados: Adiciona alguns recursos existentes ao usuário
        usuarioLogado.adicionarRecurso(new InstanciaComputacao(0, "EC2 Antigo", 0.05, 2, 4));
        usuarioLogado.adicionarRecurso(new BucketStorage(0, "S3 Antigo", 0.0, 10, 0.005));

        inicializarComponentes();
        configurarLayout();
        adicionarListeners();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 550);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Resto do código (inicializarComponentes, configurarLayout, etc.) é mantido...

    // -----------------------------------------------------
    // [Seu código para as funções inicializarComponentes,
    // configurarLayout, adicionarListeners, atualizarFormulario,
    // adicionarCamposEC2, adicionarCamposRDS, adicionarCamposS3,
    // criarRecurso, limparCampos e o main]
    // -----------------------------------------------------

    // ... (inclua aqui o restante do código que eu te forneci anteriormente,
    // começando de inicializarComponentes())


    // --- Método Main para Teste ---
    public static void main(String[] args) {
        // NOTE: GerenciadorRecursos aqui está como um mock,
        // mas deve ser o singleton real na aplicação.
        GerenciadorRecursos gr = new GerenciadorRecursos();
        SwingUtilities.invokeLater(() -> new TelaCriacaoRecursos(gr));
    }
}