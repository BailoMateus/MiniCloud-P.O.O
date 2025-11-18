package br.com.minicloud.dominio;

import java.util.ArrayList;
import java.util.List;

public class Usuario {

    private int id;
    private String nome;
    private String email;
    private String senha;
    private Plano plano;
    private List<RecursoCloud> recursos;

    // Construtor base privado para centralizar inicialização
    private void initLista() {
        if (this.recursos == null) {
            this.recursos = new ArrayList<>();
        }
    }

    // ==== CONSTRUTORES ====

    // 1) Usado pelo UsuarioDAO (SELECT)
    public Usuario(int id, String nome, String email, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.plano = null;
        initLista();
    }

    // 2) Usado pelo UsuarioDAO (INSERT)
    public Usuario(String nome, String email, String senha) {
        this(0, nome, email, senha);
    }

    // 3) Completo, com plano (pode ser usado em SELECT que joinam plano)
    public Usuario(int id, String nome, String email, String senha, Plano plano) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.plano = plano;
        initLista();
    }

    // 4) Sem id, com plano (INSERT quando já sabe o plano)
    public Usuario(String nome, String email, String senha, Plano plano) {
        this(0, nome, email, senha, plano);
    }

    // 5) Construtor usado na TelaCriacaoRecursos (sem senha, com plano)
    public Usuario(int id, String nome, String email, Plano plano) {
        this(id, nome, email, null, plano);
    }

    // 6) Versão sem id e sem senha, com plano (para testes se precisar)
    public Usuario(String nome, String email, Plano plano) {
        this(0, nome, email, null, plano);
    }

    // ==== GETTERS / SETTERS ====

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) {
        this.plano = plano;
    }

    public List<RecursoCloud> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<RecursoCloud> recursos) {
        this.recursos = recursos;
    }

    // ==== MÉTODOS DE DOMÍNIO ====

    public void adicionarRecurso(RecursoCloud recurso) {
        initLista();
        if (recurso != null) {
            recursos.add(recurso);
        }
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", plano=" + (plano != null ? plano.getNome() : "sem plano") +
                '}';
    }
}
