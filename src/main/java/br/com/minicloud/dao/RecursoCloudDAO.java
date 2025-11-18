package br.com.minicloud.dao;

import br.com.minicloud.dominio.RecursoCloud;
import br.com.minicloud.dominio.InstanciaComputacao;
import br.com.minicloud.dominio.BucketStorage;
import br.com.minicloud.dominio.BancoDadosGerenciado;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections; // Para ordenação se necessário

/**
 * DAO de agregação: Responsável por buscar todos os tipos de RecursoCloud
 * para um usuário ou para listagem geral.
 */
public class RecursoCloudDAO {

    // Instâncias dos DAOs específicos (Assumindo que eles existem e buscam dados no BD)
    private InstanciaComputacaoDAO instanciaDAO = new InstanciaComputacaoDAO();
    private BucketStorageDAO bucketDAO = new BucketStorageDAO();
    private BancoDadosGerenciadoDAO bancoDAO = new BancoDadosGerenciadoDAO();

    // NOTA: Este método está simplificado para listar TODOS os recursos do sistema,
    // pois o DAO não tem o relacionamento com a tabela 'usuario' implementado.
    public List<RecursoCloud> listarTodosRecursos() {
        List<RecursoCloud> todosRecursos = new ArrayList<>();

        // 1. Busca EC2 (InstanciaComputacao)
        todosRecursos.addAll(instanciaDAO.listarTodos());

        // 2. Busca S3 (BucketStorage)
        todosRecursos.addAll(bucketDAO.listarTodos());

        // 3. Busca RDS (BancoDadosGerenciado)
        todosRecursos.addAll(bancoDAO.listarTodos());

        // Opcional: Ordenar a lista, por exemplo, por nome
        // Collections.sort(todosRecursos, (r1, r2) -> r1.getNome().compareTo(r2.getNome()));

        return todosRecursos;
    }

    // NOTA: Para uma solução real, você precisaria de um método no DAO
    // que busca recursos APENAS pelo ID do usuário.
    // Exemplo: List<RecursoCloud> listarRecursosPorUsuario(int usuarioId) { ... }
}