package br.com.minicloud.dominio;

import java.util.ArrayList;
import java.util.List;

public class GerenciadorRecursos {

    private List<RecursoCloud> recursos;

    public GerenciadorRecursos() {
        this.recursos = new ArrayList<>();
    }

    // ============================
    // Regras de limite por plano
    // ============================
    private boolean podeCriarMaisRecursos(Usuario usuario) {
        if (usuario == null || usuario.getPlano() == null) {
            // Se não tiver plano associado, por enquanto não bloqueia
            return true;
        }

        int recursosAtuais = usuario.getRecursos().size();
        int limite = usuario.getPlano().getLimiteRecursos();

        return recursosAtuais < limite;
    }

    // ============================
    // Criação de recursos por usuário
    // ============================

    public InstanciaComputacao criarInstanciaComputacao(
            Usuario usuario,
            String nome,
            double custoBaseHora,
            int vcpus,
            int memoriaGb) {

        if (!podeCriarMaisRecursos(usuario)) {
            // GUI pode tratar null como "limite atingido"
            return null;
        }

        InstanciaComputacao inst = new InstanciaComputacao(
                0,
                nome,
                custoBaseHora,
                vcpus,
                memoriaGb
        );

        recursos.add(inst);
        if (usuario != null) {
            usuario.adicionarRecurso(inst);
        }
        return inst;
    }

    public BancoDadosGerenciado criarBancoDadosGerenciado(
            Usuario usuario,
            String nome,
            double custoBaseHora,
            int armazenamentoGb,
            boolean replicacaoAtiva,
            double custoPorGb) {

        if (!podeCriarMaisRecursos(usuario)) {
            return null;
        }

        BancoDadosGerenciado db = new BancoDadosGerenciado(
                0,
                nome,
                custoBaseHora,
                armazenamentoGb,
                replicacaoAtiva,
                custoPorGb
        );

        recursos.add(db);
        if (usuario != null) {
            usuario.adicionarRecurso(db);
        }
        return db;
    }

    public BucketStorage criarBucketStorage(
            Usuario usuario,
            String nome,
            double custoBaseHora,
            int armazenamentoGb,
            double custoPorGb) {

        if (!podeCriarMaisRecursos(usuario)) {
            return null;
        }

        BucketStorage bucket = new BucketStorage(
                0,
                nome,
                custoBaseHora,
                armazenamentoGb,
                custoPorGb
        );

        recursos.add(bucket);
        if (usuario != null) {
            usuario.adicionarRecurso(bucket);
        }
        return bucket;
    }

    // ============================
    // Operações gerais
    // ============================

    public List<RecursoCloud> getRecursos() {
        return recursos;
    }

    // Custo de todos os recursos do sistema (global)
    public double calcularCustoTotalMensal() {
        double total = 0.0;
        for (RecursoCloud r : recursos) {
            total += r.calcularCustoMensal();
        }
        return total;
    }

    // Custo somente dos recursos de um usuário
    public double calcularCustoTotalMensal(Usuario usuario) {
        if (usuario == null) return 0.0;

        double total = 0.0;
        for (RecursoCloud r : usuario.getRecursos()) {
            total += r.calcularCustoMensal();
        }
        return total;
    }

    // ============================
    // Fatura por usuário
    // ============================

    public List<FaturaItem> gerarFatura(Usuario usuario) {
        List<FaturaItem> itens = new ArrayList<>();
        if (usuario == null) return itens;

        for (RecursoCloud recurso : usuario.getRecursos()) {
            String tipo = recurso.getClass().getSimpleName();
            String nome = recurso.getNome();
            int horas = recurso.getHorasUsoMes();
            double custo = recurso.calcularCustoMensal();

            FaturaItem item = new FaturaItem(tipo, nome, horas, custo);
            itens.add(item);
        }

        return itens;
    }

    public double calcularTotalFatura(Usuario usuario) {
        double total = 0.0;
        for (FaturaItem item : gerarFatura(usuario)) {
            total += item.getCustoMensal();
        }
        return total;
    }

    // ============================
    // Ações em massa
    // ============================

    public void ligarTodos() {
        for (RecursoCloud r : recursos) {
            r.ligar();
        }
    }

    public void desligarTodos() {
        for (RecursoCloud r : recursos) {
            r.desligar();
        }
    }

    public void adicionarHorasUsoTodos(int horas) {
        for (RecursoCloud r : recursos) {
            r.adicionarHorasUso(horas);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GerenciadorRecursos{\n");
        for (RecursoCloud r : recursos) {
            sb.append("  ").append(r).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
