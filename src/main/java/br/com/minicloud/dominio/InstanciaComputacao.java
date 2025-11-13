package br.com.minicloud.dominio;

public class InstanciaComputacao extends RecursoCloud {

    private int vcpus;
    private int memoriaGb;

    public InstanciaComputacao(int id, String nome, double custoBaseHora,
                               int vcpus, int memoriaGb) {
        super(id, nome, custoBaseHora);
        this.vcpus = vcpus;
        this.memoriaGb = memoriaGb;
    }

    @Override
    public double calcularCustoMensal() {
        double custoCpu = vcpus * 0.05;
        double custoMemoria = memoriaGb * 0.01;
        double custoTotalHora = getCustoBaseHora() + custoCpu + custoMemoria;
        return custoTotalHora * getHorasUsoMes();
    }

    public int getVcpus() { return vcpus; }
    public void setVcpus(int vcpus) { this.vcpus = vcpus; }

    public int getMemoriaGb() { return memoriaGb; }
    public void setMemoriaGb(int memoriaGb) { this.memoriaGb = memoriaGb; }

    @Override
    public String toString() {
        return "InstanciaComputacao{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", ativo=" + isAtivo() +
                ", vcpus=" + vcpus +
                ", memoriaGb=" + memoriaGb +
                ", horasUsoMes=" + getHorasUsoMes() +
                ", custoBaseHora=" + getCustoBaseHora() +
                '}';
    }
}
