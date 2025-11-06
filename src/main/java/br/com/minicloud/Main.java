package br.com.minicloud;

import br.com.minicloud.dao.PlanoDAO;
import br.com.minicloud.dominio.Plano;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        PlanoDAO planoDAO = new PlanoDAO();

        System.out.println("Planos cadastrados no banco:");
        List<Plano> planos = planoDAO.listarTodos();
        for (Plano p : planos) {
            System.out.println(p);
        }
    }
}
