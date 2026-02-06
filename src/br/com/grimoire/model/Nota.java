package model;

public class Nota {
    private String titulo;
    private String conteudo;
    private String caminhoArquivo;

    public Nota(String titulo, String conteudo, String caminhoArquivo) {
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.caminhoArquivo = caminhoArquivo;
    }

    public String getTitulo() { return titulo; }

    public String getConteudo() { return conteudo; }

    public String getCaminhoArquivo() { return caminhoArquivo; }

    public void setConteudo(String novoConteudo) {
        this.conteudo = novoConteudo;
    }
}