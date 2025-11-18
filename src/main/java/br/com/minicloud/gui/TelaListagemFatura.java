package br.com.minicloud.gui;

import br.com.minicloud.dominio.*;
import br.com.minicloud.dao.RecursoCloudDAO;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class TelaListagemFatura extends JFrame {

    // --- Backend e Simulação ---
    private GerenciadorRecursos gerenciador;
    // Simulação do usuário logado (deve ser o mesmo da tela de criação para manter a consistência)
    private Usuario usuarioLogado;
    // DAO para buscar os recursos (simulando a busca no BD)
    private RecursoCloudDAO recursoDAO = new RecursoCloudDAO();

    // --- Componentes ---
    private JTable tabelaRecursos;
    private RecursoTableModel tableModel;
    private JLabel lblTotalFatura;
    private JButton btnAdicionarHoras; // Ação em massa

    public TelaListagemFatura(GerenciadorRecursos gerenciador, Usuario usuarioLogado) {
        super("Listagem de Recursos e Fatura");
        this.gerenciador = gerenciador;
        this.usuarioLogado = usuarioLogado;

        inicializarComponentes();
        configurarLayout();
        carregarRecursosSimulados(); // Carrega dados iniciais na tabela
        adicionarListeners();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // --- Subtasks: Layout e Modelo de Tabela ---

    private void inicializarComponentes() {
        tableModel = new RecursoTableModel();
        tabelaRecursos = new JTable(tableModel);

        // Formata o valor na label para Real Brasileiro (R$)
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        double total = gerenciador.calcularTotalFatura(usuarioLogado);
        lblTotalFatura = new JLabel("Total da Fatura Mensal: " + nf.format(total));
        lblTotalFatura.setFont(new Font("SansSerif", Font.BOLD, 18));

        btnAdicionarHoras = new JButton("Adicionar 10 Horas de Uso a TODOS"); // Simula o uso mensal
    }

    private void configurarLayout() {
        setLayout(new BorderLayout(10, 10));

        // Tabela de Listagem
        JPanel panelTabela = new JPanel(new BorderLayout());
        panelTabela.setBorder(BorderFactory.createTitledBorder("Recursos Criados"));
        panelTabela.add(new JScrollPane(tabelaRecursos), BorderLayout.CENTER);

        // Painel Inferior (Fatura e Ações)
        JPanel panelFatura = new JPanel(new BorderLayout());
        panelFatura.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotoes.add(btnAdicionarHoras);

        panelFatura.add(lblTotalFatura, BorderLayout.WEST);
        panelFatura.add(panelBotoes, BorderLayout.EAST);

        add(panelTabela, BorderLayout.CENTER);
        add(panelFatura, BorderLayout.SOUTH);
    }

    /**
     * Carrega dados simulados (ou do BD real) para o objeto Usuario e atualiza a tabela.
     */
    private void carregarRecursosSimulados() {
        // NOTA: Em uma aplicação real, você faria uma busca complexa no BD aqui,
        // mas como o GerenciadorRecursos gerencia a lista em memória (por enquanto),
        // usamos a lista interna dele para popular a GUI.

        // Se você não usasse o GerenciadorRecursos para gerenciar o estado,
        // a lógica seria:
        // List<RecursoCloud> recursosDoBD = recursoDAO.listarRecursosPorUsuario(usuarioLogado.getId());
        // usuarioLogado.setRecursos(recursosDoBD);

        tableModel.setFaturaItens(gerenciador.gerarFatura(usuarioLogado));
        atualizarTotalFatura();
    }

    private void adicionarListeners() {
        btnAdicionarHoras.addActionListener(e -> {
            // Lógica: Adiciona 10 horas de uso a CADA recurso do usuário
            for (RecursoCloud recurso : usuarioLogado.getRecursos()) {
                recurso.adicionarHorasUso(10);
            }

            // Recarrega e recalcula
            carregarRecursosSimulados();
            JOptionPane.showMessageDialog(this, "10 horas de uso adicionadas. Fatura recalculada.");
        });
    }

    private void atualizarTotalFatura() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        double total = gerenciador.calcularTotalFatura(usuarioLogado);
        lblTotalFatura.setText("Total da Fatura Mensal: " + nf.format(total));
        tableModel.fireTableDataChanged(); // Força a tabela a redesenhar
    }


    /**
     * Modelo de Tabela Customizado para exibir FaturaItem.
     */
    private class RecursoTableModel extends AbstractTableModel {
        private final String[] colunas = {"ID", "Nome", "Tipo", "Horas/Mês", "Custo Mensal"};
        private List<FaturaItem> faturaItens;

        public RecursoTableModel() {
            this.faturaItens = new ArrayList<>();
        }

        public void setFaturaItens(List<FaturaItem> faturaItens) {
            this.faturaItens = faturaItens;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return faturaItens.size();
        }

        @Override
        public int getColumnCount() {
            return colunas.length;
        }

        @Override
        public String getColumnName(int column) {
            return colunas[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            FaturaItem item = faturaItens.get(rowIndex);

            // Assumimos que o GerenciadorRecursos precisa do ID real do recurso.
            // O FaturaItem não tem o ID. Usamos a posição da linha ou implementamos a busca.
            // Para simplificar, vamos deixar o ID vazio na coluna por enquanto:

            switch (columnIndex) {
                case 0: return "-"; // ID real, complicado sem o mapeamento.
                case 1: return item.getNomeRecurso();
                case 2: return item.getTipoRecurso();
                case 3: return item.getHorasUso();
                case 4:
                    // Formata o custo para exibição (R$ X.XX)
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                    return nf.format(item.getCustoMensal());
                default: return null;
            }
        }
    }

    // --- Método Main para Teste ---
    public static void main(String[] args) {
        // 1. Configurar o ambiente (O mesmo usado na TelaCriacaoRecursos)
        GerenciadorRecursos gr = new GerenciadorRecursos();

        // Usuário Simulado (Plano Básico, limite 3, Crédito 500.0)
        Plano plano = new Plano(1, "Básico", 500.0, 3);
        Usuario usuario = new Usuario(10, "João da Nuvem", "joao@minicloud.com", plano);

        // 2. Criar alguns recursos no Gerenciador (Simula a persistência)
        gr.criarInstanciaComputacao(usuario, "Webserver-Prod", 0.15, 4, 8);
        gr.criarBucketStorage(usuario, "Arquivos-Clientes", 0.0, 500, 0.01);
        gr.criarBancoDadosGerenciado(usuario, "DB-Principal", 0.25, 100, true, 0.04);

        // 3. Simular algum uso inicial
        for (RecursoCloud r : usuario.getRecursos()) {
            r.adicionarHorasUso(100);
        }

        // 4. Iniciar a GUI
        SwingUtilities.invokeLater(() -> new TelaListagemFatura(gr, usuario));
    }
}