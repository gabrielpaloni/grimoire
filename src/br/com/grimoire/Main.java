import core.Caderno;
import core.LeitorArquivos;
import model.Nota;
import view.JanelaPrincipal;

import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            File pasta = new File("meusEstudos");
            if (!pasta.exists()) { pasta.mkdir(); }

            Caderno caderno = new Caderno();
            LeitorArquivos leitor = new LeitorArquivos();
            File[] arquivos = pasta.listFiles((dir, name) -> name.toLowerCase().endsWith(".md"));

            if (arquivos != null) {
                for (File arquivo : arquivos) {
                    try {
                        String titulo = arquivo.getName().replace(".md", "");
                        String conteudo = leitor.lerArquivo(arquivo.getAbsolutePath());
                        caderno.adicionarNota(new Nota(titulo, conteudo, arquivo.getAbsolutePath()));
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            new JanelaPrincipal(caderno);
        });
    }
}
