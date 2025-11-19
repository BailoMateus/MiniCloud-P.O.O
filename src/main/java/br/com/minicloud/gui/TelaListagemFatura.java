package br.com.minicloud.gui;

import br.com.minicloud.dominio.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TelaListagemFatura extends JFrame {

    private final GerenciadorRecursos gerenciador;
    private final Usuario usuarioLogado;

    private JTable tabelaRecursos;
    private RecursoTableModel tableModel;
    private JLabel lblTotalFatura;
    private JButton btnAdicionarHoras;

    public TelaListagemFatura(GerenciadorRecursos gerenciador, Usuario usuarioLogado) {
        super("Listagem de Recursos e Fatura");

        this.gerenciador = gerenciador;
        this.usuarioLogado = usuarioLogado;

        // Carrega recursos do usuário a partir do banco
        this.gerenciador.carregarRecursosDoUsuario(this.usuarioLogado);

        inicializarComponentes();
        configurarLayout();
        carregarRecursos();
        adicionarListeners();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void inicializarComponentes() {
        tableModel = new RecursoTableModel();
        tabelaRecursos = new JTable(tableModel);

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        double total = gerenciador.calcularTotalFatura(usuarioLogado);
        lblTotalFatura = new JLabel("Total da Fatura Mensal: " + nf.format(total));
        lblTotalFatura.setFont(new Font("SansSerif", Font.BOLD, 18));

        btnAdicionarHoras = new JButton("Adicionar 10 Horas de Uso a TODOS");
    }

    private void configurarLayout() {
        setLayout(new BorderLayout(10, 10));

        // Tabela de recursos
        JPanel panelTabela = new JPanel(new BorderLayout());
        panelTabela.setBorder(BorderFactory.createTitledBorder(
                "Recursos do usuário: " + usuarioLogado.getNome()));
        panelTabela.add(new JScrollPane(tabelaRecursos), BorderLayout.CENTER);

        // Painel inferior
        JPanel panelFatura = new JPanel(new BorderLayout());
        panelFatura.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnVoltar = new JButton("Voltar ao Menu");
        btnVoltar.addActionListener(e -> dispose());

        panelBotoes.add(btnVoltar);
        panelBotoes.add(btnAdicionarHoras);

        panelFatura.add(lblTotalFatura, BorderLayout.WEST);
        panelFatura.add(panelBotoes, BorderLayout.EAST);

        add(panelTabela, BorderLayout.CENTER);
        add(panelFatura, BorderLayout.SOUTH);
    }

    private void carregarRecursos() {
        tableModel.setFaturaItens(gerenciador.gerarFatura(usuarioLogado));
        atualizarTotalFatura();
    }

    private void adicionarListeners() {
        btnAdicionarHoras.addActionListener(e -> {
            if (usuarioLogado.getRecursos() != null) {
                for (RecursoCloud recurso : usuarioLogado.getRecursos()) {
                    recurso.adicionarHorasUso(10);
                }
            }

            carregarRecursos();
            JOptionPane.showMessageDialog(this,
                    "10 horas de uso adicionadas a todos os recursos.\nFatura recalculada.");
        });
    }

    private void atualizarTotalFatura() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        double total = gerenciador.calcularTotalFatura(usuarioLogado);
        lblTotalFatura.setText("Total da Fatura Mensal: " + nf.format(total));
        tableModel.fireTableDataChanged();
    }

    // ============================
    // Modelo de tabela
    // ============================

    private static class RecursoTableModel extends AbstractTableModel {

        private final String[] colunas = {"ID", "Tipo", "Nome", "Horas/Mês", "Custo Mensal"};
        private List<FaturaItem> faturaItens;

        public RecursoTableModel() {
            this.faturaItens = new ArrayList<>();
        }

        public void setFaturaItens(List<FaturaItem> faturaItens) {
            this.faturaItens = faturaItens != null ? faturaItens : new ArrayList<>();
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

            switch (columnIndex) {
                case 0:
                    return "-"; // ID do recurso não está em FaturaItem (poderia ser adicionado depois)
                case 1:
                    return item.getTipoRecurso();
                case 2:
                    return item.getNomeRecurso();
                case 3:
                    return item.getHorasUso();
                case 4:
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                    return nf.format(item.getCustoMensal());
                default:
                    return null;
            }
        }
    }
}
