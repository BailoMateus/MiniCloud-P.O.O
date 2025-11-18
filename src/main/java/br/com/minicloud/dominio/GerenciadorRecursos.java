package br.com.minicloud.dominio;

import java.util.ArrayList;
import java.util.List;

public class GerenciadorRecursos {

    private List<RecursoCloud> recursos;

    public GerenciadorRecursos() {
        this.recursos = new ArrayList<>();
    }

    // ============================
    // Criação de recursos
    // ============================

    public InstanciaComputacao criarInstanciaComputacao(
            String nome,
            double custoBaseHora,
            int vcpus,
            int memoriaGb) {

        // id = 0 por enquanto (antes de salvar no banco)
        InstanciaComputacao inst = new InstanciaComputacao(
                0,
                nome,
                custoBaseHora,
                vcpus,
                memoriaGb
        );
        recursos.add(inst);
        return inst;
    }

    public BancoDadosGerenciado criarBancoDadosGerenciado(
            String nome,
            double custoBaseHora,
            int armazenamentoGb,
            boolean replicacaoAtiva,
            double custoPorGb) {

        BancoDadosGerenciado db = new BancoDadosGerenciado(
                0,
                nome,
                custoBaseHora,
                armazenamentoGb,
                replicacaoAtiva,
                custoPorGb
        );
        recursos.add(db);
        return db;
    }

    public BucketStorage criarBucketStorage(
            String nome,
            double custoBaseHora,
            int armazenamentoGb,
            double custoPorGb) {

        BucketStorage bucket = new BucketStorage(
                0,
                nome,
                custoBaseHora,
                armazenamentoGb,
                custoPorGb
        );
        recursos.add(bucket);
        return bucket;
    }

    // ============================
    // Operações gerais
    // ============================

    public List<RecursoCloud> getRecursos() {
        return recursos;
    }

    public double calcularCustoTotalMensal() {
        double total = 0.0;
        for (RecursoCloud r : recursos) {
            total += r.calcularCustoMensal();  //  chamada polimórfica
        }
        return total;
    }

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
