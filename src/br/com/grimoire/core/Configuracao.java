package core;

import java.io.*;
import java.util.Properties;

public class Configuracao {

    private static final String ARQUIVO_CONFIG = "grimoire.properties";
    private final Properties props = new Properties();

    public Configuracao() {
        carregar();
    }

    private void carregar() {
        try (InputStream input = new FileInputStream(ARQUIVO_CONFIG)) {
            props.load(input);
        } catch (IOException ex) {

        }
    }

    public void salvar(int x, int y, int w, int h, boolean pinned, String ultimaNota) {
        try (OutputStream output = new FileOutputStream(ARQUIVO_CONFIG)) {
            props.setProperty("window.x", String.valueOf(x));
            props.setProperty("window.y", String.valueOf(y));
            props.setProperty("window.width", String.valueOf(w));
            props.setProperty("window.height", String.valueOf(h));
            props.setProperty("window.pinned", String.valueOf(pinned));
            if (ultimaNota != null) {
                props.setProperty("last.note", ultimaNota);
            } else {
                props.setProperty("last.note", "");
            }
            props.store(output, "Configuracoes do Grimoire");
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
    public int getX() { return Integer.parseInt(props.getProperty("window.x", "100")); }
    public int getY() { return Integer.parseInt(props.getProperty("window.y", "100")); }
    public int getWidth() { return Integer.parseInt(props.getProperty("window.width", "1100")); }
    public int getHeight() { return Integer.parseInt(props.getProperty("window.height", "750")); }
    public boolean isPinned() { return Boolean.parseBoolean(props.getProperty("window.pinned", "false")); }
    public String getLastNote() {
        String nota = props.getProperty("last.note", "");
        return nota.isEmpty() ? null : nota;
    }
}