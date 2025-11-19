package br.com.minicloud.dominio;

import br.com.minicloud.dao.BancoDadosGerenciadoDAO;
import br.com.minicloud.dao.BucketStorageDAO;
import br.com.minicloud.dao.InstanciaComputacaoDAO;
import br.com.minicloud.dao.RecursoCloudDAO;
import br.com.minicloud.exceptions.LimiteRecursosPlanoException;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe de regra de negócio para criação e faturamento de recursos da MiniCloud.
 * Agora integrada com o banco de dados via DAOs.
 */
public class GerenciadorRecursos {

    private final InstanciaComputacaoDAO instanciaDAO = new InstanciaComputacaoDAO();
    private final BancoDadosGerenciadoDAO bancoDAO = new BancoDadosGerenciadoDAO();
    private final BucketStorageDAO bucketDAO = new BucketStorageDAO();
    private final RecursoCloudDAO recursoDAO = new RecursoCloudDAO();

    /**
     * Cria uma nova Instância de Computação para o usuário.
     * Respeita o limite de recursos do plano e persiste no BD.
     */
    public InstanciaComputacao criarInstanciaComputacao(
            Usuario usuario,
            String nome,
            double custoBaseHora,
            int vcpus,
            int memoriaGb
    ) throws LimiteRecursosPlanoException {

        verificarLimiteRecursos(usuario);

        InstanciaComputacao instancia = new InstanciaComputacao(
                0,
                nome,
                custoBaseHora,
                vcpus,
                memoriaGb
        );

        // Persiste no BD
        instancia = instanciaDAO.inserirInstancia(usuario, instancia);

        // Atualiza o modelo em memória
        usuario.adicionarRecurso(instancia);

        return instancia;
    }

    /**
     * Cria um novo Banco de Dados Gerenciado para o usuário.
     */
    public BancoDadosGerenciado criarBancoDadosGerenciado(
            Usuario usuario,
            String nome,
            double custoBaseHora,
            int armazenamentoGb,
            boolean replicacaoAtiva,
            double custoPorGb // por enquanto não está no schema, mas mantemos no domínio
    ) throws LimiteRecursosPlanoException {

        verificarLimiteRecursos(usuario);

        BancoDadosGerenciado banco = new BancoDadosGerenciado(
                0,
                nome,
                custoBaseHora,
                armazenamentoGb,
                replicacaoAtiva,
                custoPorGb
        );

        banco = bancoDAO.inserirBanco(usuario, banco);
        usuario.adicionarRecurso(banco);

        return banco;
    }

    /**
     * Cria um novo Bucket de Storage para o usuário.
     */
    public BucketStorage criarBucketStorage(
            Usuario usuario,
            String nome,
            double custoBaseHora,
            int armazenamentoGb,
            double custoPorGb // idem, mantido no domínio
    ) throws LimiteRecursosPlanoException {

        verificarLimiteRecursos(usuario);

        BucketStorage bucket = new BucketStorage(
                0,
                nome,
                custoBaseHora,
                armazenamentoGb,
                custoPorGb
        );

        bucket = bucketDAO.inserirBucket(usuario, bucket);
        usuario.adicionarRecurso(bucket);

        return bucket;
    }

    public void carregarRecursosDoUsuario(Usuario usuario) {
        List<RecursoCloud> recursos = recursoDAO.listarTodosPorUsuario(usuario.getId());
        usuario.setRecursos(recursos);
    }

    /**
     * Gera a lista de itens de fatura do usuário com base nos recursos
     * atualmente presentes em usuario.getRecursos().
     */
    public List<FaturaItem> gerarFatura(Usuario usuario) {
        List<FaturaItem> itens = new ArrayList<>();

        if (usuario.getRecursos() == null) {
            return itens;
        }

        for (RecursoCloud recurso : usuario.getRecursos()) {

            double horasUso = recurso.getHorasUsoMes();
            double custoMensal = recurso.getCustoBaseHora() * horasUso;

            String tipo;
            if (recurso instanceof InstanciaComputacao) {
                tipo = "Instância de Computação";
            } else if (recurso instanceof BancoDadosGerenciado) {
                tipo = "Banco de Dados Gerenciado";
            } else if (recurso instanceof BucketStorage) {
                tipo = "Bucket de Storage";
            } else {
                tipo = "Recurso Desconhecido";
            }

            FaturaItem item = new FaturaItem(
                    recurso.getNome(),
                    tipo,
                    (int)horasUso,
                    custoMensal
            );

            itens.add(item);
        }

        return itens;
    }

    /**
     * Calcula o valor total da fatura a partir dos FaturaItem.
     */
    public double calcularTotalFatura(Usuario usuario) {
        double total = 0.0;
        for (FaturaItem item : gerarFatura(usuario)) {
            total += item.getCustoMensal();
        }
        return total;
    }

    /**
     * Verifica, com base no plano do usuário, se ainda é possível criar
     * mais um recurso. Usa a quantidade de recursos persistidos no BD.
     */
    private void verificarLimiteRecursos(Usuario usuario) throws LimiteRecursosPlanoException {
        Plano plano = usuario.getPlano();
        if (plano == null) {
            // Se não houver plano associado, você pode decidir:
            // - permitir tudo
            // - ou lançar exceção específica
            return;
        }

        int limite = plano.getLimiteRecursos();

        // Conta quantos recursos existem no BD para esse usuário
        int quantidadeAtual = recursoDAO.listarTodosPorUsuario(usuario.getId()).size();

        if (quantidadeAtual >= limite) {
            String msg = String.format(
                    "O usuário já possui %d recursos. Limite do plano '%s' é %d.",
                    quantidadeAtual,
                    plano.getNome(),
                    limite
            );
            throw new LimiteRecursosPlanoException(msg);
        }
    }
}
