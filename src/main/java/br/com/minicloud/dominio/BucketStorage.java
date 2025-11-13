package br.com.minicloud.dominio;

public class BucketStorage extends RecursoCloud {

    private int armazenamentoGb;   // capacidade armazenada
    private double custoPorGb;     // valor cobrado por GB

    // Construtor completo (com id, para quando vier do banco)
    public BucketStorage(int id, String nome, double custoBaseHora,
                         int armazenamentoGb, double custoPorGb) {
        super(id, nome, custoBaseHora);
        this.armazenamentoGb = armazenamentoGb;
        this.custoPorGb = custoPorGb;
    }

    // Construtor sem id (para criar antes de salvar no banco)
    public BucketStorage(String nome, double custoBaseHora,
                         int armazenamentoGb, double custoPorGb) {
        super(0, nome, custoBaseHora);  // 👈 passa id = 0 por enquanto
        this.armazenamentoGb = armazenamentoGb;
        this.custoPorGb = custoPorGb;
    }

    public int getArmazenamentoGb() {
        return armazenamentoGb;
    }

    public void setArmazenamentoGb(int armazenamentoGb) {
        this.armazenamentoGb = armazenamentoGb;
    }

    public double getCustoPorGb() {
        return custoPorGb;
    }

    public void setCustoPorGb(double custoPorGb) {
        this.custoPorGb = custoPorGb;
    }

    @Override
    public double calcularCustoMensal() {
        double custoUsoHoras = getHorasUsoMes() * getCustoBaseHora();
        double custoArmazenamento = armazenamentoGb * custoPorGb;
        return custoUsoHoras + custoArmazenamento;
    }

    @Override
    public String toString() {
        return "BucketStorage{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", ativo=" + isAtivo() +
                ", horasUsoMes=" + getHorasUsoMes() +
                ", armazenamentoGb=" + armazenamentoGb +
                ", custoPorGb=" + custoPorGb +
                ", custoMensal=" + calcularCustoMensal() +
                '}';
    }
}
