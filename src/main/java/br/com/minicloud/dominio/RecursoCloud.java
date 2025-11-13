package br.com.minicloud.dominio;

public abstract class RecursoCloud {

    private int id;
    private String nome;
    private boolean ativo;
    private double custoBaseHora;
    private int horasUsoMes;

    public RecursoCloud(int id, String nome, double custoBaseHora) {
        this.id = id;
        this.nome = nome;
        this.custoBaseHora = custoBaseHora;
        this.ativo = false;
        this.horasUsoMes = 0;
    }

    public void ligar() {
        if (!ativo) {
            ativo = true;
            System.out.println(nome + " foi ligado.");
        } else {
            System.out.println(nome + " já está ativo.");
        }
    }

    public void desligar() {
        if (ativo) {
            ativo = false;
            System.out.println(nome + " foi desligado.");
        } else {
            System.out.println(nome + " já está desligado.");
        }
    }

    public void adicionarHorasUso(int horas) {
        if (horas > 0) {
            this.horasUsoMes += horas;
        }
    }

    public abstract double calcularCustoMensal();

    public int getId() { return id; }
    public String getNome() { return nome; }
    public boolean isAtivo() { return ativo; }
    public double getCustoBaseHora() { return custoBaseHora; }
    public int getHorasUsoMes() { return horasUsoMes; }

    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public void setCustoBaseHora(double custoBaseHora) { this.custoBaseHora = custoBaseHora; }
    public void setHorasUsoMes(int horasUsoMes) { this.horasUsoMes = horasUsoMes; }

    @Override
    public String toString() {
        return "RecursoCloud{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", ativo=" + ativo +
                ", custoBaseHora=" + custoBaseHora +
                ", horasUsoMes=" + horasUsoMes +
                '}';
    }
}
