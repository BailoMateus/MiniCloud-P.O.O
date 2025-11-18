package br.com.minicloud.dominio;

public class BancoDadosGerenciado extends RecursoCloud {

    private int armazenamentoGb;        // tamanho do banco em GB
    private boolean replicacaoAtiva;    // se tem réplica ou não
    private double custoPorGb;          // custo por GB armazenado

    // Construtor completo (com id - vindo do banco)
    public BancoDadosGerenciado(int id, String nome, double custoBaseHora,
                                int armazenamentoGb, boolean replicacaoAtiva, double custoPorGb) {
        super(id, nome, custoBaseHora);
        this.armazenamentoGb = armazenamentoGb;
        this.replicacaoAtiva = replicacaoAtiva;
        this.custoPorGb = custoPorGb;
    }

    // Construtor sem id (antes de salvar no banco)
    public BancoDadosGerenciado(String nome, double custoBaseHora,
                                int armazenamentoGb, boolean replicacaoAtiva, double custoPorGb) {
        super(0, nome, custoBaseHora); // id = 0 até persistir
        this.armazenamentoGb = armazenamentoGb;
        this.replicacaoAtiva = replicacaoAtiva;
        this.custoPorGb = custoPorGb;
    }

    public int getArmazenamentoGb() {
        return armazenamentoGb;
    }

    public void setArmazenamentoGb(int armazenamentoGb) {
        this.armazenamentoGb = armazenamentoGb;
    }

    public boolean isReplicacaoAtiva() {
        return replicacaoAtiva;
    }

    public void setReplicacaoAtiva(boolean replicacaoAtiva) {
        this.replicacaoAtiva = replicacaoAtiva;
    }

    public double getCustoPorGb() {
        return custoPorGb;
    }

    public void setCustoPorGb(double custoPorGb) {
        this.custoPorGb = custoPorGb;
    }

    @Override
    public double calcularCustoMensal() {
        // custo de horas (compute)
        double custoUsoHoras = getHorasUsoMes() * getCustoBaseHora();

        // custo de armazenamento
        double custoArmazenamento = armazenamentoGb * custoPorGb;

        double custoTotal = custoUsoHoras + custoArmazenamento;

        // se tiver replicação, aplica um fator extra (por exemplo +30%)
        if (replicacaoAtiva) {
            custoTotal *= 1.3;
        }

        return custoTotal;
    }

    @Override
    public String toString() {
        return "BancoDadosGerenciado{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", ativo=" + isAtivo() +
                ", horasUsoMes=" + getHorasUsoMes() +
                ", armazenamentoGb=" + armazenamentoGb +
                ", replicacaoAtiva=" + replicacaoAtiva +
                ", custoPorGb=" + custoPorGb +
                ", custoMensal=" + calcularCustoMensal() +
                '}';
    }
}
