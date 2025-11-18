package br.com.minicloud.dominio;

import java.util.ArrayList;
import java.util.List;

public class Usuario {

    private int id;
    private String nome;
    private String email;

    private Plano plano;                      // Plano do usuário (FREE, STANDARD, PRO)
    private List<RecursoCloud> recursos;      // Recursos criados por esse usuário

    // Construtor completo (útil se quiser ligar com banco depois)
    public Usuario(int id, String nome, String email, Plano plano) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.plano = plano;
        this.recursos = new ArrayList<>();
    }

    // Construtor sem ID (para usar na lógica antes de salvar no banco)
    public Usuario(String nome, String email, Plano plano) {
        this(0, nome, email, plano);
    }

    // ============ Getters e Setters ============
    public int getId() {
        return id;
    }

    public void setId(int id) { // se depois você quiser sincronizar com o banco
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) {
        this.plano = plano;
    }

    public List<RecursoCloud> getRecursos() {
        if (recursos == null) {
            recursos = new ArrayList<>();
        }
        return recursos;
    }

    public void setRecursos(List<RecursoCloud> recursos) {
        this.recursos = recursos;
    }

    // ============ Métodos de conveniência ============
    public void adicionarRecurso(RecursoCloud recurso) {
        getRecursos().add(recurso);
    }

    public void removerRecurso(RecursoCloud recurso) {
        getRecursos().remove(recurso);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", plano=" + (plano != null ? plano.getNome() : "sem plano") +
                ", qtdRecursos=" + (recursos != null ? recursos.size() : 0) +
                '}';
    }
}
