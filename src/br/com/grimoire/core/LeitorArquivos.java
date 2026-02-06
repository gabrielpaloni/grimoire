package core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LeitorArquivos {
    public String lerArquivo(String caminho) {
        try {
            return Files.readString(Path.of(caminho));
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            return null;
        }
    }
    public File[] pegarArquivosDaPasta(String caminhoPasta) {
        File pasta = new File(caminhoPasta);
        return pasta.listFiles();
    }
}