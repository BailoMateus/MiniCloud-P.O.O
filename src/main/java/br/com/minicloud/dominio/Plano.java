package br.com.minicloud.dominio;

public class Plano {

    private int id;
    private String nome;
    private double limiteCredito;
    private int limiteRecursos;

    public Plano() {
    }

    public Plano(int id, String nome, double limiteCredito, int limiteRecursos) {
        this.id = id;
        this.nome = nome;
        this.limiteCredito = limiteCredito;
        this.limiteRecursos = limiteRecursos;
    }

    // Construtor sem ID (para inserir no banco, que gera o SERIAL)
    public Plano(String nome, double limiteCredito, int limiteRecursos) {
        this.nome = nome;
        this.limiteCredito = limiteCredito;
        this.limiteRecursos = limiteRecursos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getLimiteCredito() {
        return limiteCredito;
    }

    public void setLimiteCredito(double limiteCredito) {
        this.limiteCredito = limiteCredito;
    }

    public int getLimiteRecursos() {
        return limiteRecursos;
    }

    public void setLimiteRecursos(int limiteRecursos) {
        this.limiteRecursos = limiteRecursos;
    }

    @Override
    public String toString() {
        return "Plano{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", limiteCredito=" + limiteCredito +
                ", limiteRecursos=" + limiteRecursos +
                '}';
    }
}

