package br.com.minicloud.dominio;

public class FaturaItem {

    private String tipoRecurso;
    private String nomeRecurso;
    private int horasUso;
    private double custoMensal;

    public FaturaItem(String tipoRecurso, String nomeRecurso, int horasUso, double custoMensal) {
        this.tipoRecurso = tipoRecurso;
        this.nomeRecurso = nomeRecurso;
        this.horasUso = horasUso;
        this.custoMensal = custoMensal;
    }

    public String getTipoRecurso() {
        return tipoRecurso;
    }

    public String getNomeRecurso() {
        return nomeRecurso;
    }

    public int getHorasUso() {
        return horasUso;
    }

    public double getCustoMensal() {
        return custoMensal;
    }

    @Override
    public String toString() {
        return "FaturaItem{" +
                "tipoRecurso='" + tipoRecurso + '\'' +
                ", nomeRecurso='" + nomeRecurso + '\'' +
                ", horasUso=" + horasUso +
                ", custoMensal=" + custoMensal +
                '}';
    }
}
