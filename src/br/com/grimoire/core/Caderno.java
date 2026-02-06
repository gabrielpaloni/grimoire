package core;

import java.util.ArrayList;
import model.Nota;

public class Caderno{

    private ArrayList<Nota> listaDeNotas;

    public Caderno(){
        this.listaDeNotas = new ArrayList<>();
    }

    public void adicionarNota(Nota n){
        listaDeNotas.add(n);
    }

    public ArrayList<Nota> getNotas() {
        return this.listaDeNotas;
    }

    public void listarTodas() {
        System.out.println("\n--- Sumário do Grimoire ---");

        if (listaDeNotas.isEmpty()) {
            System.out.println("O Grimório está vazio.");
        } else {
            for (Nota nota : listaDeNotas) {
                System.out.println("- " + nota.getTitulo());
            }
        }
    }
}