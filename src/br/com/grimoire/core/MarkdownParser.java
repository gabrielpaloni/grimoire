package core;

import model.Nota;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownParser {

    private static final String COR_KEYWORD = "#C678DD";
    private static final String COR_TYPE = "#E5C07B";

    private static final String[] KEYWORDS_JAVA = {
            "public", "private", "protected", "class", "interface", "enum", "extends", "implements",
            "void", "return", "static", "final", "abstract", "new", "this", "super",
            "if", "else", "for", "while", "do", "switch", "case", "default", "break", "continue",
            "try", "catch", "finally", "throw", "throws", "import", "package"
    };

    private static final String[] TYPES_JAVA = {
            "int", "long", "double", "float", "boolean", "char", "byte", "short", "String", "Object", "List", "ArrayList", "Map", "HashMap"
    };

    public Nota parse(String nomeDoArquivo, String conteudoBruto, String caminhoDoArquivo) {
        String titulo = nomeDoArquivo.replace(".md", "");
        return new Nota(titulo, conteudoBruto, caminhoDoArquivo);
    }

    public static String renderizarHtml(Nota nota) {
        if (nota == null) return "";

        String textoMarkdown = nota.getConteudo().replace("\r\n", "\n").replace("\r", "\n");
        String caminhoArquivo = nota.getCaminhoArquivo();

        String css = "<style>"
                + "body { font-family: 'Segoe UI', 'Ubuntu', 'Liberation Sans', 'Cantarell', 'Dialog', sans-serif; font-size: 14px; background-color: #282C34; color: #ABB2BF; margin: 0; }"
                + "h1 { font-size: 26px; color: #E06C75; border-bottom: 1px solid #3E4451; padding-bottom: 5px; margin-top: 20px; }"
                + "h2 { font-size: 20px; color: #61AFEF; margin-top: 20px; border-bottom: 1px solid #3E4451; padding-bottom: 3px; }"
                + "h3 { font-size: 16px; color: #98C379; margin-top: 15px; }"
                + "h4 { font-size: 14px; color: #D19A66; margin-top: 10px; font-weight: bold; }"
                + "h5 { font-size: 13px; color: #56B6C2; margin-top: 10px; font-weight: bold; font-style: italic; }"
                + "h6 { font-size: 12px; color: #ABB2BF; margin-top: 10px; text-decoration: underline; }"
                + ".code-block { font-family: 'Consolas', 'Monaco', 'Ubuntu Mono', 'Liberation Mono', monospace; font-size: 13px; background-color: #1E2127; border: 1px solid #4B5263; padding: 10px; border-radius: 5px; color: #98C379; margin-bottom: 10px; }"
                + "code { font-family: 'Consolas', 'Monaco', 'Ubuntu Mono', 'Liberation Mono', monospace; font-size: 13px; color: #E5C07B; background-color: #2C313A; padding: 2px 4px; border-radius: 3px; }"
                + "blockquote { font-style: italic; color: #DCDCDC; border-left: 4px solid #C678DD; margin: 10px 0; padding: 5px 15px; background-color: #323842; }"
                + "ul, ol { margin-left: 5px; padding-left: 20px; }"
                + "li { margin-bottom: 3px; }"
                + ".sub-item { margin-bottom: 3px; color: #ABB2BF; }"
                + ".todo { font-family: 'Segoe UI Symbol', 'DejaVu Sans', 'Dialog', sans-serif; margin-bottom: 3px; }"
                + ".done { color: #56B6C2; text-decoration: line-through; }"
                + "hr { height: 1px; background-color: #3E4451; border: none; margin: 20px 0; }"
                + "a { color: #61AFEF; text-decoration: none; font-weight: bold; }"
                + "</style>";

        StringBuilder html = new StringBuilder("<html><head>" + css + "</head><body>");

        String pastaBase = "";
        try {
            File arquivo = new File(caminhoArquivo);
            pastaBase = arquivo.getParentFile().toURI().toString();
        } catch (Exception e) {}

        if (textoMarkdown != null) {
            String[] linhas = textoMarkdown.split("\n");
            boolean dentroDeCodigoBloco = false;

            for (String linha : linhas) {
                String l = linha;
                String trimL = l.trim();

                if (trimL.startsWith("```")) {
                    if (dentroDeCodigoBloco) {
                        html.append("</div>"); dentroDeCodigoBloco = false;
                    } else {
                        html.append("<div class='code-block'>"); dentroDeCodigoBloco = true;
                    }
                    continue;
                }

                if (dentroDeCodigoBloco) {
                    String linhaSegura = linha.replace("<", "&lt;").replace(">", "&gt;");
                    int espacos = 0;
                    while (espacos < linhaSegura.length() && linhaSegura.charAt(espacos) == ' ') espacos++;
                    html.append("&nbsp;".repeat(espacos)).append(aplicarSyntaxHighlighting(linhaSegura.substring(espacos))).append("<br>");
                    continue;
                }

                l = l.replace("\\![", "&#33;[");
                l = l.replace("\\`", "&#96;");

                Map<String, String> placeholders = new HashMap<>();
                if (l.contains("`")) {
                    Matcher mCode = Pattern.compile("`([^`]+)`").matcher(l);
                    int idx = 0;
                    StringBuffer sbCode = new StringBuffer();
                    while(mCode.find()) {
                        String key = "##CODE_PROTECTED_" + idx++ + "##";
                        placeholders.put(key, "<code>" + mCode.group(1).replace("<", "&lt;") + "</code>");
                        mCode.appendReplacement(sbCode, key);
                    }
                    mCode.appendTail(sbCode);
                    l = sbCode.toString();
                }

                if (trimL.startsWith("# ")) html.append("<h1>").append(processarTexto(l.substring(2), placeholders)).append("</h1>");
                else if (trimL.startsWith("## ")) html.append("<h2>").append(processarTexto(l.substring(3), placeholders)).append("</h2>");
                else if (trimL.startsWith("### ")) html.append("<h3>").append(processarTexto(l.substring(4), placeholders)).append("</h3>");
                else if (trimL.startsWith("#### ")) html.append("<h4>").append(processarTexto(l.substring(5), placeholders)).append("</h4>");
                else if (trimL.startsWith("##### ")) html.append("<h5>").append(processarTexto(l.substring(6), placeholders)).append("</h5>");
                else if (trimL.startsWith("###### ")) html.append("<h6>").append(processarTexto(l.substring(7), placeholders)).append("</h6>");

                else if (trimL.startsWith("> ")) html.append("<blockquote>").append(processarTexto(l.substring(2), placeholders)).append("</blockquote>");

                else if (trimL.startsWith("- ") || trimL.startsWith("* ")) {
                    int espacos = contarEspacosIniciais(l);
                    if (espacos == 0) {
                        html.append("<li>").append(processarTexto(trimL.substring(2), placeholders)).append("</li>");
                    } else {
                        int margin = 30 + (espacos * 10);
                        String bullet = (espacos >= 4) ? "&#9642;" : "&#9702;";
                        html.append("<div class='sub-item' style='margin-left: " + margin + "px;'>")
                                .append(bullet).append("&nbsp; ")
                                .append(processarTexto(trimL.substring(2), placeholders))
                                .append("</div>");
                    }
                }

                else if (trimL.matches("^\\d+\\.\\s.*")) {
                    int espacos = contarEspacosIniciais(l);
                    String textoLimpo = l.replaceFirst("^\\s*\\d+\\.\\s", "");
                    if (espacos == 0) {
                        html.append("<li>").append(processarTexto(textoLimpo, placeholders)).append("</li>");
                    } else {
                        int margin = 30 + (espacos * 10);
                        html.append("<div class='sub-item' style='margin-left: " + margin + "px;'>")
                                .append("&#8618;&nbsp; ")
                                .append(processarTexto(textoLimpo, placeholders))
                                .append("</div>");
                    }
                }

                else if (trimL.equals("---") || trimL.equals("***")) html.append("<hr>");

                else if (trimL.startsWith("- [ ]")) {
                    int espacos = contarEspacosIniciais(l);
                    int margin = (espacos * 10);
                    html.append("<div class='todo' style='margin-left: "+margin+"px'>&#9744;&nbsp; ").append(processarTexto(l.replace("- [ ]", "").trim(), placeholders)).append("</div>");
                }
                else if (trimL.startsWith("- [x]")) {
                    int espacos = contarEspacosIniciais(l);
                    int margin = (espacos * 10);
                    html.append("<div class='todo done' style='margin-left: "+margin+"px'>&#9745;&nbsp; ").append(processarTexto(l.replace("- [x]", "").trim(), placeholders)).append("</div>");
                }

                else if (l.contains("![")) {
                    Matcher m = Pattern.compile("!\\[(.*?)\\]\\((.*?)\\)").matcher(l);
                    StringBuffer sb = new StringBuffer();
                    while(m.find()) {
                        String alt = m.group(1);
                        String src = m.group(2);
                        if (!src.toLowerCase().startsWith("http")) src = pastaBase + src;
                        String imgTag = "<div align='center'><img src='" + src + "' alt='" + alt + "' width='100%' style='border-radius: 8px; margin: 10px;'></div><br>";
                        m.appendReplacement(sb, Matcher.quoteReplacement(imgTag));
                    }
                    m.appendTail(sb);
                    html.append(restaurar(sb.toString(), placeholders));
                }
                else if (!trimL.isEmpty()) {
                    html.append("<p>").append(processarTexto(l, placeholders)).append("</p>");
                }
            }
            if (dentroDeCodigoBloco) html.append("</div>");
        }
        return html.toString() + "</body></html>";
    }

    private static int contarEspacosIniciais(String linha) {
        int count = 0;
        for (char c : linha.toCharArray()) {
            if (c == ' ') count++;
            else break;
        }
        return count;
    }

    private static String processarTexto(String t, Map<String, String> p) {
        t = t.replaceAll("\\[\\[(.*?)\\]\\]", "<a href='interno:$1' class='wiki'>$1</a>");
        t = t.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "<a href='$2'>$1</a>");
        t = t.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        t = t.replaceAll("\\*(.+?)\\*", "<i>$1</i>");
        t = t.replaceAll("~~(.+?)~~", "<strike>$1</strike>");
        return restaurar(t, p);
    }

    private static String restaurar(String t, Map<String, String> p) {
        for (Map.Entry<String, String> e : p.entrySet()) t = t.replace(e.getKey(), e.getValue());
        return t;
    }

    private static String aplicarSyntaxHighlighting(String l) {
        for (String t : TYPES_JAVA) l = l.replaceAll("\\b" + t + "\\b", "<span style='color: " + COR_TYPE + ";'>" + t + "</span>");
        for (String k : KEYWORDS_JAVA) l = l.replaceAll("\\b" + k + "\\b", "<span style='color: " + COR_KEYWORD + ";'>" + k + "</span>");
        return l;
    }
}