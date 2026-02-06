package view;

import core.Caderno;
import core.Configuracao;
import core.EscritorArquivos;
import core.LeitorArquivos;
import core.MarkdownParser;
import model.Nota;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JanelaPrincipal extends JFrame {

    private Nota notaAtual = null;
    private final LeitorArquivos leitor = new LeitorArquivos();
    private final Caderno cadernoRef;
    private final Configuracao config = new Configuracao();

    private final List<Nota> todasAsNotas;
    private final DefaultListModel<String> modeloDaLista;
    private final JList<String> listaVisual;

    private JPanel painelLateral;
    private boolean sidebarVisivel = true;
    private JTextField txtBusca;
    private JEditorPane painelPreview;

    private JButton btnExcluir, btnRecarregar, btnEditar, btnPin, btnExportar, btnNovo, btnMaximizar, btnAjuda;

    private int xMouse, yMouse;
    private boolean isPinned = false;

    private final Color COR_SIDEBAR = new Color(33, 37, 43);
    private final Color COR_EDITOR = new Color(40, 44, 52);
    private final Color COR_HEADER = new Color(33, 37, 43);
    private final Color COR_BUSCA = new Color(44, 49, 58);
    private final Color COR_PLACEHOLDER = new Color(100, 100, 100);

    private final String FONT_UI = getFonteUI();

    public JanelaPrincipal(Caderno caderno) {
        this.cadernoRef = caderno;
        this.todasAsNotas = new ArrayList<>(caderno.getNotas());

        setUndecorated(true);
        setTitle("Grimoire");

        setBounds(config.getX(), config.getY(), config.getWidth(), config.getHeight());
        this.isPinned = config.isPinned();
        setAlwaysOnTop(isPinned);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            File arquivoIcone = new File("icon.png");
            if (arquivoIcone.exists()) {
                Image icon = Toolkit.getDefaultToolkit().getImage(arquivoIcone.getAbsolutePath());
                this.setIconImage(icon);
                try { if (Taskbar.isTaskbarSupported()) Taskbar.getTaskbar().setIconImage(icon); } catch (Exception e) {}
            }
        } catch (Exception ignored) {}

        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.setBackground(COR_HEADER);
        painelTopo.setPreferredSize(new Dimension(0, 50));
        painelTopo.setBorder(new EmptyBorder(0, 10, 0, 10));

        painelTopo.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) { xMouse = evt.getX(); yMouse = evt.getY(); }
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && btnMaximizar != null) btnMaximizar.doClick();
            }
        });
        painelTopo.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent evt) {
                if (getExtendedState() != JFrame.MAXIMIZED_BOTH) setLocation(evt.getXOnScreen() - xMouse, evt.getYOnScreen() - yMouse);
            }
        });

        JPanel cantoEsquerdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        cantoEsquerdo.setOpaque(false);
        JButton btnMenu = new JButton("☰");
        styleHeaderButton(btnMenu);
        cantoEsquerdo.add(btnMenu);

        JLabel logo = new JLabel("GRIMOIRE");
        logo.setForeground(new Color(198, 120, 221));
        logo.setFont(new Font(FONT_UI, Font.BOLD, 16));
        logo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logo.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                listaVisual.clearSelection();
                txtBusca.setText("Buscar...");
                txtBusca.setForeground(COR_PLACEHOLDER);
                atualizarListaVisual(todasAsNotas);
                mostrarTelaInicial();
            }
        });
        cantoEsquerdo.add(logo);
        painelTopo.add(cantoEsquerdo, BorderLayout.WEST);

        JPanel cantoDireito = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        cantoDireito.setOpaque(false);

        btnPin = new JButton("*");
        styleToolbarButton(btnPin, new Color(229, 192, 123));
        if (isPinned) {
            btnPin.setForeground(new Color(229, 192, 123));
            btnPin.setBorder(new CompoundBorder(new MatteBorder(0, 0, 2, 0, new Color(229, 192, 123)), new EmptyBorder(5, 10, 3, 10)));
        }
        btnPin.addActionListener(e -> {
            isPinned = !isPinned;
            setAlwaysOnTop(isPinned);
            if (isPinned) {
                btnPin.setForeground(new Color(229, 192, 123));
                btnPin.setBorder(new CompoundBorder(new MatteBorder(0, 0, 2, 0, new Color(229, 192, 123)), new EmptyBorder(5, 10, 3, 10)));
            } else {
                btnPin.setForeground(new Color(100, 100, 100));
                btnPin.setBorder(new EmptyBorder(5, 10, 5, 10));
            }
        });
        cantoDireito.add(btnPin);

        btnExportar = new JButton("📤");
        styleToolbarButton(btnExportar, new Color(198, 120, 221));
        btnExportar.setVisible(false);
        btnExportar.addActionListener(e -> exportarNotaAtual());
        cantoDireito.add(btnExportar);

        btnEditar = new JButton("✏️");
        styleToolbarButton(btnEditar, new Color(152, 195, 121));
        btnEditar.setVisible(false);
        btnEditar.addActionListener(e -> {
            if (notaAtual != null) {
                boolean sucesso = false;
                for (String cmd : new String[]{"code", "code.cmd", "idea", "idea64"}) {
                    try { new ProcessBuilder(cmd, notaAtual.getCaminhoArquivo()).start(); sucesso = true; break; } catch (Exception ignored) {}
                }
                if (!sucesso) JOptionPane.showMessageDialog(this, "Editor (VS Code/IntelliJ) não encontrado no PATH.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        cantoDireito.add(btnEditar);

        JButton btnAjuda = new JButton("?");
        styleWindowControl(btnAjuda, false);
        btnAjuda.addActionListener(e -> {
            String msg = "<html><body style='width: 250px; font-family: Segoe UI, sans-serif; color: #ABB2BF; background-color: #282C34'>" +
                    "<h2 style='color: #E06C75; text-align: center;'>Grimoire v1.0</h2>" +
                    "<p style='text-align: center;'>Seu Segundo Cérebro Digital</p><br>" +
                    "<b>Atalhos:</b><br>" +
                    "• <b>Ctrl + N:</b> Nova Nota<br>" +
                    "• <b>F5:</b> Recarregar Nota<br>" +
                    "• <b>Delete:</b> Excluir Nota<br>" +
                    "<br><p style='font-size: 10px; text-align: right;'>Dev: Gabriel Paloni</p>" +
                    "</body></html>";

            UIManager.put("OptionPane.background", new Color(40, 44, 52));
            UIManager.put("Panel.background", new Color(40, 44, 52));
            UIManager.put("OptionPane.messageForeground", new Color(171, 178, 191));

            JOptionPane.showMessageDialog(this, msg, "Sobre", JOptionPane.PLAIN_MESSAGE);
        });
        cantoDireito.add(btnAjuda, 0);

        btnRecarregar = new JButton("⟳");
        styleToolbarButton(btnRecarregar, new Color(97, 175, 239));
        btnRecarregar.setVisible(false);
        btnRecarregar.addActionListener(e -> {
            if (notaAtual != null) {
                try {
                    String novoConteudo = leitor.lerArquivo(notaAtual.getCaminhoArquivo());
                    notaAtual.setConteudo(novoConteudo);
                    atualizarVisualizacao(painelPreview, notaAtual);
                } catch (Exception ex) { JOptionPane.showMessageDialog(null, "Erro: " + ex.getMessage()); }
            }
        });
        cantoDireito.add(btnRecarregar);

        btnExcluir = new JButton("🗑️");
        styleToolbarButton(btnExcluir, new Color(224, 108, 117));
        btnExcluir.setVisible(false);
        btnExcluir.addActionListener(e -> excluirNotaAtual());
        cantoDireito.add(btnExcluir);

        JLabel separador = new JLabel("|");
        separador.setForeground(new Color(60, 60, 60));
        separador.setBorder(new EmptyBorder(0, 10, 0, 10));
        cantoDireito.add(separador);

        JButton btnMinimizar = new JButton("—");
        styleWindowControl(btnMinimizar, false);
        btnMinimizar.addActionListener(e -> setState(Frame.ICONIFIED));
        cantoDireito.add(btnMinimizar);

        btnMaximizar = new JButton("□");
        styleWindowControl(btnMaximizar, false);
        btnMaximizar.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        btnMaximizar.addActionListener(e -> {
            if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.NORMAL);
                btnMaximizar.setText("□");
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                btnMaximizar.setText("❐");
            }
        });
        cantoDireito.add(btnMaximizar);

        JButton btnFechar = new JButton("X");
        styleWindowControl(btnFechar, true);
        btnFechar.addActionListener(e -> salvarEFechar());
        cantoDireito.add(btnFechar);

        painelTopo.add(cantoDireito, BorderLayout.EAST);
        add(painelTopo, BorderLayout.NORTH);

        painelLateral = new JPanel(new BorderLayout());
        painelLateral.setBackground(COR_SIDEBAR);
        painelLateral.setPreferredSize(new Dimension(260, 0));

        JPanel painelFerramentas = new JPanel(new BorderLayout(5, 0));
        painelFerramentas.setBackground(COR_SIDEBAR);
        painelFerramentas.setBorder(new EmptyBorder(15, 15, 5, 15));

        txtBusca = new JTextField("Buscar...");
        styleSearchField(txtBusca);
        adicionarPlaceholder(txtBusca, "Buscar...");
        painelFerramentas.add(txtBusca, BorderLayout.CENTER);

        btnNovo = new JButton("+");
        styleSmallButton(btnNovo);
        painelFerramentas.add(btnNovo, BorderLayout.EAST);
        painelLateral.add(painelFerramentas, BorderLayout.NORTH);

        modeloDaLista = new DefaultListModel<>();
        atualizarListaVisual(todasAsNotas);

        listaVisual = new JList<>(modeloDaLista);
        listaVisual.setBackground(COR_SIDEBAR);
        listaVisual.setCellRenderer(new RenderizadorModerno());

        JScrollPane scrollLateral = new JScrollPane(listaVisual);
        scrollLateral.setBorder(null);
        scrollLateral.getVerticalScrollBar().setUI(new ScrollbarCustomizada());
        scrollLateral.getVerticalScrollBar().setUnitIncrement(16);
        painelLateral.add(scrollLateral, BorderLayout.CENTER);
        add(painelLateral, BorderLayout.WEST);

        painelPreview = new JEditorPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() { return true; }
        };
        painelPreview.setContentType("text/html");
        painelPreview.setEditable(false);
        painelPreview.setBackground(COR_EDITOR);
        painelPreview.setBorder(new EmptyBorder(10, 30, 30, 30));

        painelPreview.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String desc = e.getDescription();
                if (desc.startsWith("interno:")) navegarParaNota(desc.substring(8));
                else try { Desktop.getDesktop().browse(e.getURL().toURI()); } catch (Exception ex) {}
            }
        });

        JScrollPane scrollPreview = new JScrollPane(painelPreview);
        scrollPreview.setBorder(null);
        scrollPreview.getVerticalScrollBar().setUI(new ScrollbarCustomizada());
        scrollPreview.getVerticalScrollBar().setUnitIncrement(20);
        scrollPreview.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPreview, BorderLayout.CENTER);

        mostrarTelaInicial();
        configurarAtalhos();

        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String termo = txtBusca.getText().toLowerCase();
                if (termo.equals("buscar...") || termo.isEmpty()) { atualizarListaVisual(todasAsNotas); return; }
                List<Nota> filtradas = new ArrayList<>();
                for (Nota n : todasAsNotas) {
                    if (n.getTitulo().toLowerCase().contains(termo) || n.getConteudo().toLowerCase().contains(termo)) filtradas.add(n);
                }
                atualizarListaVisual(filtradas);
            }
        });

        btnNovo.addActionListener(e -> {
            String nome = JOptionPane.showInputDialog(this, "Nome:");
            if (nome != null && !nome.trim().isEmpty()) criarNovaNota(nome);
        });

        listaVisual.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaVisual.getSelectedIndex() != -1) {
                String titulo = listaVisual.getSelectedValue();
                notaAtual = encontrarNotaPorTitulo(titulo);
                if (notaAtual != null) {
                    atualizarVisualizacao(painelPreview, notaAtual);
                    btnExcluir.setVisible(true); btnRecarregar.setVisible(true); btnEditar.setVisible(true); btnExportar.setVisible(true);
                }
            }
        });

        btnMenu.addActionListener(e -> { sidebarVisivel = !sidebarVisivel; painelLateral.setVisible(sidebarVisivel); revalidate(); repaint(); });

        String ultimaNotaTitulo = config.getLastNote();
        if (ultimaNotaTitulo != null) {
            Nota n = encontrarNotaPorTitulo(ultimaNotaTitulo);
            if (n != null) listaVisual.setSelectedValue(n.getTitulo(), true);
        }
        setVisible(true);
    }

    private void salvarEFechar() {
        String tituloNota = (notaAtual != null) ? notaAtual.getTitulo() : null;
        config.salvar(getX(), getY(), getWidth(), getHeight(), isPinned, tituloNota);
        System.exit(0);
    }

    private void exportarNotaAtual() {
        if (notaAtual == null) return;
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar HTML");
        fc.setSelectedFile(new File(notaAtual.getTitulo() + ".html"));
        fc.setFileFilter(new FileNameExtensionFilter("HTML", "html"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dest = fc.getSelectedFile();
            if (!dest.getName().endsWith(".html")) dest = new File(dest.getAbsolutePath() + ".html");
            new EscritorArquivos().escreverArquivo(dest.getAbsolutePath(), MarkdownParser.renderizarHtml(notaAtual));
            try { Desktop.getDesktop().open(dest); } catch(Exception ignored){}
        }
    }

    private void excluirNotaAtual() {
        if (notaAtual == null) return;
        if (JOptionPane.showConfirmDialog(this, "Excluir?", "Confirma", JOptionPane.YES_NO_OPTION) == 0) {
            new File(notaAtual.getCaminhoArquivo()).delete();
            todasAsNotas.remove(notaAtual);
            cadernoRef.getNotas().remove(notaAtual);
            atualizarListaVisual(todasAsNotas);
            mostrarTelaInicial();
        }
    }

    private void mostrarTelaInicial() {
        if(btnExcluir!=null)btnExcluir.setVisible(false); if(btnRecarregar!=null)btnRecarregar.setVisible(false);
        if(btnEditar!=null)btnEditar.setVisible(false); if(btnExportar!=null)btnExportar.setVisible(false);
        notaAtual = null;
        painelPreview.setText("<html><body style='background-color:#282C34;font-family:Segoe UI'><div align='center' style='margin-top:100px'><div style='font-size:80px;color:#3E4451'>&#9679;</div><div style='font-size:24px;color:#5C6370'>Bem-vindo ao Grimoire</div></div></body></html>");
    }

    private void criarNovaNota(String titulo) {
        String pasta = "meusEstudos";
        if (!todasAsNotas.isEmpty()) pasta = new File(todasAsNotas.get(0).getCaminhoArquivo()).getParent();
        String path = pasta + File.separator + titulo + ".md";
        String content = "# " + titulo + "\n\nNova nota...";
        new EscritorArquivos().escreverArquivo(path, content);
        Nota n = new Nota(titulo, content, path);
        cadernoRef.adicionarNota(n);
        todasAsNotas.add(n);
        atualizarListaVisual(todasAsNotas);
        listaVisual.setSelectedValue(titulo, true);
    }

    private void navegarParaNota(String t) {
        Nota n = encontrarNotaPorTitulo(t);
        if (n!=null) listaVisual.setSelectedValue(n.getTitulo(), true);
        else if (JOptionPane.showConfirmDialog(this, "Criar '"+t+"'?", "404", JOptionPane.YES_NO_OPTION)==0) criarNovaNota(t);
    }

    private void atualizarVisualizacao(JEditorPane p, Nota n) { p.setText(MarkdownParser.renderizarHtml(n)); p.setCaretPosition(0); }
    private void atualizarListaVisual(List<Nota> l) { modeloDaLista.clear(); for (Nota n : l) modeloDaLista.addElement(n.getTitulo()); }
    private Nota encontrarNotaPorTitulo(String t) { for (Nota n : todasAsNotas) if (n.getTitulo().equals(t)) return n; return null; }

    private void configurarAtalhos() {
        JRootPane rootPane = getRootPane();

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("F5"), "recarregar");
        actionMap.put("recarregar", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (btnRecarregar.isVisible()) {
                    btnRecarregar.doClick();
                    System.out.println("F5 Pressionado: Recarregando...");
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("DELETE"), "excluir");
        actionMap.put("excluir", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (btnExcluir.isVisible()) {
                    btnExcluir.doClick();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("control N"), "novaNota");
        actionMap.put("novaNota", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                btnNovo.doClick();
            }
        });
    }

    private void adicionarPlaceholder(JTextField t, String p) {
        t.setForeground(COR_PLACEHOLDER);
        t.addFocusListener(new FocusListener(){public void focusGained(FocusEvent e){if(t.getText().equals(p)){t.setText("");t.setForeground(Color.WHITE);}}public void focusLost(FocusEvent e){if(t.getText().isEmpty()){t.setText(p);t.setForeground(COR_PLACEHOLDER);}}});
    }
    private void styleSearchField(JTextField t) { t.setBackground(COR_BUSCA); t.setCaretColor(Color.WHITE); t.setFont(new Font(FONT_UI, Font.PLAIN, 14)); t.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0,0,2,0,new Color(60,64,72)), new EmptyBorder(5,5,5,5))); }
    private void styleHeaderButton(JButton b) {
        b.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
        b.setForeground(new Color(157,165,180));
        b.setBackground(COR_HEADER);
        b.setBorder(null);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){b.setForeground(Color.WHITE);}
            public void mouseExited(MouseEvent e){b.setForeground(new Color(157,165,180));}
        });
    }

    private void styleSmallButton(JButton b) {
        b.setBackground(COR_BUSCA);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
        b.setBorder(new EmptyBorder(0,10,0,10));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){b.setBackground(new Color(60,64,72));}
            public void mouseExited(MouseEvent e){b.setBackground(COR_BUSCA);}
        });
    }

    private void styleToolbarButton(JButton b, Color c) {
        b.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        b.setForeground(new Color(157,165,180));
        b.setBackground(COR_HEADER);
        b.setBorder(new EmptyBorder(5,10,5,10));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){b.setForeground(c);}
            public void mouseExited(MouseEvent e){if(b==btnPin && isPinned)return;b.setForeground(new Color(157,165,180));}
        });
    }

    private void styleWindowControl(JButton b, boolean close) {
        b.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        b.setForeground(new Color(157,165,180));
        b.setBackground(COR_HEADER);
        b.setBorder(new EmptyBorder(5,12,5,12));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){
                if(close){b.setBackground(new Color(232,17,35));b.setForeground(Color.WHITE);}
                else{b.setBackground(new Color(60,60,60));}
            }
            public void mouseExited(MouseEvent e){
                b.setBackground(COR_HEADER);b.setForeground(new Color(157,165,180));
            }
        });
    }

    private String getFonteUI() {
        String[] fonts = {"Segoe UI", "Ubuntu", "Liberation Sans", "DejaVu Sans", "Cantarell", "Arial"};
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String f : fonts) {
            for (String avail : available) {
                if (f.equalsIgnoreCase(avail)) return f;
            }
        }
        return "SansSerif";
    }
}