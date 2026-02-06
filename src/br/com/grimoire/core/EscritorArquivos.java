package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EscritorArquivos {

    public void escreverArquivo(String caminho, String conteudo) {
        try {
            Files.writeString(Path.of(caminho), conteudo);

            System.out.println("Arquivo salvo com sucesso: " + caminho);

        } catch (IOException e) {
            System.out.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }
}