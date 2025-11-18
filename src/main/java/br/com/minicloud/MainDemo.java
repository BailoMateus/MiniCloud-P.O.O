package br.com.minicloud;

import br.com.minicloud.dominio.*;
import br.com.minicloud.exceptions.LimiteRecursosPlanoException;

import java.util.List;

public class MainDemo {

    public static void main(String[] args) {

        // 1) Criar um plano
        Plano planoFree = new Plano(1, "FREE", 50.0, 2); // limiteRecursos = 2

        // 2) Criar um usuário da MiniCloud (domínio)
        Usuario usuario = new Usuario("Mateus Demo", "mateus.demo@minicloud.com", planoFree);

        // 3) Criar o gerenciador de recursos
        GerenciadorRecursos gerenciador = new GerenciadorRecursos();

        try {
            // 4) Criar alguns recursos para esse usuário
            InstanciaComputacao ec2 = gerenciador.criarInstanciaComputacao(
                    usuario,
                    "EC2-Dev",
                    0.10,
                    2,
                    4
            );

            BancoDadosGerenciado rds = gerenciador.criarBancoDadosGerenciado(
                    usuario,
                    "RDS-Dev",
                    0.20,
                    100,
                    false,
                    0.02
            );

            // Tenta criar um terceiro recurso no plano FREE (limite = 2)
            // Isso deve lançar a LimiteRecursosPlanoException
            BucketStorage s3 = gerenciador.criarBucketStorage(
                    usuario,
                    "S3-Logs",
                    0.01,
                    500,
                    0.005
            );

            // Se chegar aqui sem exception, beleza (mas em teoria deve estourar o limite)
            if (s3 != null) {
                System.out.println("Bucket criado (não era esperado no plano FREE): " + s3.getNome());
            }

            // 5) Simular horas de uso
            if (ec2 != null) {
                ec2.adicionarHorasUso(100);
            }

            if (rds != null) {
                rds.adicionarHorasUso(150);
            }

        } catch (LimiteRecursosPlanoException e) {
            // Aqui você vê a regra de negócio em ação
            System.out.println("⚠️ Não foi possível criar recurso: " + e.getMessage());
        }

        // 6) Listar recursos do usuário
        System.out.println("\n=== Recursos do usuário ===");
        for (RecursoCloud recurso : usuario.getRecursos()) {
            System.out.println(recurso);
        }

        // 7) Gerar fatura
        System.out.println("\n=== Fatura MiniCloud ===");
        List<FaturaItem> itens = gerenciador.gerarFatura(usuario);
        for (FaturaItem item : itens) {
            System.out.printf(
                    "%s - %s | Horas: %d | Custo: R$ %.2f%n",
                    item.getTipoRecurso(),
                    item.getNomeRecurso(),
                    item.getHorasUso(),
                    item.getCustoMensal()
            );
        }

        double total = gerenciador.calcularTotalFatura(usuario);
        System.out.println("--------------------------");
        System.out.printf("Total: R$ %.2f%n", total);
    }
}
