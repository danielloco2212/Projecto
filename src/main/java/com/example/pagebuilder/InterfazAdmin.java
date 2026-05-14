package com.example.pagebuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class InterfazAdmin extends JFrame {

    @Autowired private ClubRepositorio clubRepositorio;
    @Autowired private EquipoRepositorio equipoRepositorio;
    @Autowired private NoticiaRepositorio noticiaRepositorio;
    @Autowired private ImagenRepositorio imagenRepositorio;
    @Autowired private MiembroRepositorio miembroRepositorio;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private InterfazAcceso interfazAcceso;

    private final Map<String, ImageIcon> cacheImagenes = new HashMap<>();

    private final Color COLOR_PRIMARY = new Color(44, 62, 80);
    private final Color COLOR_SIDEBAR = new Color(38, 41, 44);
    private final Color COLOR_SIDEBAR_DARK = new Color(26, 28, 30);
    private final Color COLOR_ACCENT = new Color(113, 215, 241);
    private final Color COLOR_BG = new Color(15, 15, 15);
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 15);

    private static final String DIRECTORIO_SUBIDAS = "uploads";
    private static final String RUTA_ALMACENAMIENTO = System.getProperty("user.home") + File.separator + ".adminbaloncesto";
    private static final String LOGO_PATH = "/static/logo.png";

    private Usuario usuarioActual;

    public void establecerUsuarioActual(Usuario usuario) { this.usuarioActual = usuario; }

    private Long idNoticiaEditando = null;
    private Long idMiembroEditando = null;
    private Long idEquipoEditando = null;
    private JTextField campoNombreClub, campoLogo, campoFacebook, campoInstagram, campoTwitter, campoFondoEncabezado, campoTextoEncabezado, campoImagenFondo, campoYoutubeDirecto;
    private JTextField campoTituloNoticia, campoNombreEquipo, campoNombreMiembro, campoFotoMiembro;
    private JComboBox<Equipo> comboEquipos;
    private JComboBox<String> comboRoles;
    private JComboBox<Noticia> comboNoticias;
    private JList<Miembro> listaMiembros;
    private DefaultListModel<Miembro> modeloMiembros;
    private JList<Imagen> listaImagenes;
    private DefaultListModel<Imagen> modeloImagenes;
    private JTextArea areaContenidoNoticia;
    private JButton botonGuardarEquipo;
    private CardLayout disposicionTarjetas;
    private JPanel panelTarjetas;
    private JLabel etiquetaLogoBarraLateral;

    public InterfazAdmin() {
        setTitle("Admin Club de Baloncesto");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        modeloImagenes = new DefaultListModel<>();
        modeloMiembros = new DefaultListModel<>();

        try {
            Files.createDirectories(Paths.get(RUTA_ALMACENAMIENTO, DIRECTORIO_SUBIDAS));
        } catch (IOException e) {
            System.err.println("Could not create upload directory: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error al crear directorio de subidas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        getContentPane().setBackground(COLOR_SIDEBAR_DARK);

        disposicionTarjetas = new CardLayout();
        panelTarjetas = new JPanel(disposicionTarjetas);
        panelTarjetas.setBackground(COLOR_BG);

        panelTarjetas.add(crearPanelClub(), "CLUB");
        panelTarjetas.add(crearPanelNoticias(), "NOTICIAS");
        panelTarjetas.add(crearPanelEquipo(), "EQUIPOS");
        panelTarjetas.add(crearPanelGaleriaImagenes(), "GALERIA");

        JPanel navPanel = new JPanel(new GridBagLayout());
        navPanel.setBackground(new Color(0, 120, 215));
        navPanel.setPreferredSize(new Dimension(250, 0));
        navPanel.setBorder(new EmptyBorder(30, 15, 30, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        etiquetaLogoBarraLateral = new JLabel();
        etiquetaLogoBarraLateral.setHorizontalAlignment(SwingConstants.CENTER);
        etiquetaLogoBarraLateral.setPreferredSize(new Dimension(220, 120));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        navPanel.add(etiquetaLogoBarraLateral, gbc);
        cargarLogoApp();

        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.gridy = 1; navPanel.add(crearBotonNavegacion("⚙️ CONFIG", "CLUB"), gbc);
        gbc.gridy = 2; navPanel.add(crearBotonNavegacion("📰 NOTICIAS", "NOTICIAS"), gbc);
        gbc.gridy = 3; navPanel.add(crearBotonNavegacion("👥 EQUIPOS", "EQUIPOS"), gbc);

        gbc.insets = new Insets(40, 0, 0, 0);
        gbc.gridy = 4; navPanel.add(crearBotonNavegacion("🖼️ GALERÍA", "GALERIA"), gbc);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        navPanel.add(Box.createVerticalGlue(), gbc);

        JButton botonCerrarSesion = new JButton("🚪 CERRAR SESIÓN");
        estilizarBoton(botonCerrarSesion, new Color(192, 57, 43));
        botonCerrarSesion.addActionListener(e -> {
            this.dispose();
            interfazAcceso.setVisible(true);
        });
        gbc.gridy = 6; gbc.weighty = 0; gbc.insets = new Insets(10, 0, 0, 0);
        navPanel.add(botonCerrarSesion, gbc);

        add(navPanel, BorderLayout.WEST);
        add(panelTarjetas, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    private JButton crearBotonNavegacion(String texto, String nombreTarjeta) {
        JButton boton = new JButton(texto);
        boton.setBackground(new Color(60, 64, 67));
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(15, 10, 15, 10));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.putClientProperty("FlatLaf.style", "arc: 12;");
        boton.addActionListener(e -> disposicionTarjetas.show(panelTarjetas, nombreTarjeta));
        return boton;
    }

    private void estilizarBoton(JButton boton, Color colorFondo) {
        boton.setBackground(colorFondo);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        boton.setBorder(new EmptyBorder(15, 25, 15, 25));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.putClientProperty("FlatLaf.style", "arc: 15;");
        boton.putClientProperty("JButton.buttonType", "roundRect");
        boton.putClientProperty("JButton.hoverBackground",
            new Color(colorFondo.getRed(), colorFondo.getGreen(), colorFondo.getBlue(), 200));
    }

    private JPanel crearPanelClub() {
        JPanel mainP = new JPanel(new GridBagLayout());
        mainP.setBackground(COLOR_BG);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(COLOR_BG);
        contentPanel.setBorder(new EmptyBorder(20, 50, 20, 50));

        GridBagConstraints gbcContent = new GridBagConstraints();
        gbcContent.gridx = 0;
        gbcContent.fill = GridBagConstraints.HORIZONTAL;
        gbcContent.weightx = 1.0;
        gbcContent.insets = new Insets(10, 0, 10, 0);

        campoNombreClub = new JTextField();
        campoLogo = new JTextField();
        campoFacebook = new JTextField();
        campoInstagram = new JTextField();
        campoTwitter = new JTextField();
        campoFondoEncabezado = new JTextField("#2c3e50");
        campoTextoEncabezado = new JTextField("#ecf0f1");
        campoImagenFondo = new JTextField();
        campoYoutubeDirecto = new JTextField();

        JPanel panelIdentidad = crearPanelSeccion("Identidad del Club");
        agregarCampoConfiguracion(panelIdentidad, campoNombreClub, "🏷️ Nombre del Club:", false);
        agregarCampoConfiguracion(panelIdentidad, campoLogo, "🖼️ Logo (URL):", true);
        agregarCampoConfiguracion(panelIdentidad, campoImagenFondo, "🌆 Imagen Fondo (URL):", true);
        gbcContent.gridy = 0;
        contentPanel.add(panelIdentidad, gbcContent);

        JPanel panelRedesSociales = crearPanelSeccion("Redes Sociales");
        agregarCampoConfiguracion(panelRedesSociales, campoFacebook, "📘 Facebook:", false);
        agregarCampoConfiguracion(panelRedesSociales, campoInstagram, "📸 Instagram:", false);
        agregarCampoConfiguracion(panelRedesSociales, campoTwitter, "🐦 Twitter:", false);
        gbcContent.gridy = 1;
        contentPanel.add(panelRedesSociales, gbcContent);

        JPanel panelMultimedia = crearPanelSeccion("Multimedia");
        agregarCampoConfiguracion(panelMultimedia, campoYoutubeDirecto, "📺 YouTube Live (URL):", false);
        gbcContent.gridy = 2;
        contentPanel.add(panelMultimedia, gbcContent);

        JPanel panelEstiloEncabezado = crearPanelSeccion("Estilo del Encabezado");
        JButton botonColorFondo = crearBotonBarraLateral("Seleccionar Color Fondo Encabezado");
        botonColorFondo.addActionListener(e -> seleccionarColor(campoFondoEncabezado));
        panelEstiloEncabezado.add(botonColorFondo, crearGBC(0, 0, 2, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5)));

        JButton botonColorTexto = crearBotonBarraLateral("Seleccionar Color Texto Encabezado");
        botonColorTexto.addActionListener(e -> seleccionarColor(campoTextoEncabezado));
        panelEstiloEncabezado.add(botonColorTexto, crearGBC(0, 1, 2, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5)));
        gbcContent.gridy = 3;
        contentPanel.add(panelEstiloEncabezado, gbcContent);

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionButtonsPanel.setBackground(COLOR_BG);

        JButton btnSave = new JButton("GUARDAR CAMBIOS");
        estilizarBoton(btnSave, new Color(146, 39, 143));
        btnSave.addActionListener(e -> guardarClub());
        actionButtonsPanel.add(btnSave);

        JButton btnOpenWebsite = new JButton("ABRIR PÁGINA WEB");
        estilizarBoton(btnOpenWebsite, COLOR_ACCENT);
        btnOpenWebsite.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("http://localhost:8080/"));
            } catch (IOException | URISyntaxException ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir la página web: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        actionButtonsPanel.add(btnOpenWebsite);
        gbcContent.gridy = 4;
        contentPanel.add(actionButtonsPanel, gbcContent);

        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.gridx = 0;
        gbcMain.gridy = 0;
        gbcMain.weightx = 1.0;
        gbcMain.weighty = 1.0;
        gbcMain.fill = GridBagConstraints.BOTH;
        mainP.add(new JScrollPane(contentPanel), gbcMain);

        return mainP;
    }

    private JPanel crearPanelSeccion(String titulo) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(new LineBorder(new Color(0, 120, 215), 3), titulo, TitledBorder.LEFT, TitledBorder.TOP, FONT_NORMAL, Color.WHITE),
            new EmptyBorder(10, 10, 10, 10)
        ));
        return panel;
    }

    private void agregarCampoConfiguracion(JPanel targetPanel, JTextField field, String labelText, boolean isImageField) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel(labelText);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = targetPanel.getComponentCount() / 2;
        gbc.weightx = 0.0;
        targetPanel.add(label, gbc);

        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setBackground(new Color(45, 48, 51));
        field.setBackground(new Color(45, 48, 51));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.putClientProperty("FlatLaf.placeholderText", "Introduce " + labelText.toLowerCase().replace(":", ""));
        field.setBorder(new CompoundBorder(new LineBorder(new Color(60, 64, 67)), new EmptyBorder(5, 5, 5, 5)));
        fieldPanel.add(field, BorderLayout.CENTER);

        if (isImageField) {
            JButton browseBtn = new JButton("...");
            browseBtn.setPreferredSize(new Dimension(30, field.getPreferredSize().height));
            browseBtn.setMargin(new Insets(0, 0, 0, 0));
            browseBtn.setFocusPainted(false);
            browseBtn.setBackground(COLOR_ACCENT);
            browseBtn.setForeground(Color.WHITE);
            browseBtn.putClientProperty("FlatLaf.style", "arc: 5;");
            browseBtn.addActionListener(e -> mostrarDialogoSeleccionImagen(field));
            fieldPanel.add(browseBtn, BorderLayout.EAST);
        }

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        targetPanel.add(fieldPanel, gbc);
    }

    private GridBagConstraints crearGBC(int gridx, int gridy, int gridwidth, int fill, Insets insets) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.fill = fill;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        return gbc;
    }

    private JButton crearBotonBarraLateral(String texto) {
        JButton boton = new JButton(texto);
        estilizarBoton(boton, new Color(60, 64, 67));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return boton;
    }

    private void updatePreviewImage(String urlStr, JLabel label) {
        if (urlStr == null || urlStr.isEmpty()) {
            label.setIcon(null);
            label.setText("No Image");
            return;
        }

        if (cacheImagenes.containsKey(urlStr)) {
            label.setIcon(cacheImagenes.get(urlStr));
            label.setText("");
            return;
        }

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                int w = label.getWidth() > 0 ? label.getWidth() : label.getPreferredSize().width;
                int h = label.getHeight() > 0 ? label.getHeight() : label.getPreferredSize().height;
                if (w <= 0) w = 100;
                if (h <= 0) h = 100;

                String relativePath = urlStr.startsWith("/" + DIRECTORIO_SUBIDAS + "/")
                        ? urlStr.substring(("/" + DIRECTORIO_SUBIDAS + "/").length())
                        : urlStr;

                java.nio.file.Path imagePath = Paths.get(RUTA_ALMACENAMIENTO, DIRECTORIO_SUBIDAS, relativePath);

                if (Files.exists(imagePath)) {
                    try {
                        BufferedImage img = javax.imageio.ImageIO.read(imagePath.toFile());
                        if (img != null)
                            return new ImageIcon(img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH));
                    } catch (IOException e) {
                        System.err.println("Error loading local image: " + imagePath + " - " + e.getMessage());
                    }
                }

                try {
                    URL url = new URL(urlStr);
                    ImageIcon icon = new ImageIcon(url);
                    if (icon.getIconWidth() > 0) {
                        java.awt.Image scaledImg = icon.getImage().getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImg);
                    }
                } catch (MalformedURLException e) {
                    // no es URL válida
                } catch (Exception e) {
                    System.err.println("Error loading external image: " + urlStr + " - " + e.getMessage());
                }

                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        cacheImagenes.put(urlStr, icon);
                        label.setIcon(icon);
                        label.setText("");
                    } else {
                        label.setIcon(null);
                        label.setText("Error");
                    }
                } catch (Exception ignored) {
                    label.setIcon(null);
                    label.setText("Error");
                }
            }
        }.execute();
    }

    private JPanel crearPanelGaleriaImagenes() {
        JPanel mainP = new JPanel(new BorderLayout(10, 10));
        mainP.setBackground(COLOR_BG);
        mainP.setBorder(new EmptyBorder(10, 10, 10, 10));

        listaImagenes = new JList<>(modeloImagenes);
        listaImagenes.setCellRenderer(new RenderizadorCeldaImagen());
        listaImagenes.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        listaImagenes.setVisibleRowCount(-1);
        listaImagenes.setFixedCellWidth(150);
        listaImagenes.setFixedCellHeight(150);

        JScrollPane scrollPane = new JScrollPane(listaImagenes);
        scrollPane.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(new LineBorder(new Color(0, 120, 215), 3), "Imágenes Subidas", TitledBorder.LEFT, TitledBorder.TOP, FONT_NORMAL, Color.WHITE),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JButton botonSubir = new JButton("Subir Nueva Imagen");
        estilizarBoton(botonSubir, new Color(40, 40, 40));
        botonSubir.addActionListener(e -> subirImagen());

        JButton botonEliminar = new JButton("Eliminar Imagen Seleccionada");
        estilizarBoton(botonEliminar, new Color(192, 57, 43));
        botonEliminar.addActionListener(e -> eliminarImagenSeleccionada());

        JPanel barraHerramientas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraHerramientas.setBackground(COLOR_BG);
        barraHerramientas.add(botonSubir);
        barraHerramientas.add(botonEliminar);

        mainP.add(barraHerramientas, BorderLayout.NORTH);
        mainP.add(scrollPane, BorderLayout.CENTER);
        return mainP;
    }

    private void refrescarGaleriaImagenes() {
        modeloImagenes.clear();
        imagenRepositorio.findAll().forEach(modeloImagenes::addElement);
    }

    private void subirImagen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Imagen para Subir");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif"));

        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File archivoSeleccionado = fileChooser.getSelectedFile();
        try {
            String nombreOriginal = archivoSeleccionado.getName();
            String nombreUnico = System.currentTimeMillis() + "_" + nombreOriginal;
            java.nio.file.Path rutaDestino = Paths.get(RUTA_ALMACENAMIENTO, DIRECTORIO_SUBIDAS, nombreUnico);
            Files.copy(archivoSeleccionado.toPath(), rutaDestino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/" + DIRECTORIO_SUBIDAS + "/" + nombreUnico;
            Imagen nuevaImagen = Imagen.builder()
                    .nombreArchivo(nombreOriginal)
                    .urlImagen(imageUrl)
                    .build();
            imagenRepositorio.save(nuevaImagen);
            refrescarGaleriaImagenes();
            JOptionPane.showMessageDialog(this, "Imagen subida con éxito: " + nombreOriginal);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al subir imagen: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error uploading image: " + e.getMessage());
        }
    }

    private void eliminarImagenSeleccionada() {
        Imagen imagenSeleccionada = listaImagenes.getSelectedValue();
        if (imagenSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona una imagen para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que quieres eliminar esta imagen?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            java.nio.file.Path imagePath = Paths.get(RUTA_ALMACENAMIENTO, DIRECTORIO_SUBIDAS,
                    imagenSeleccionada.getUrlImagen().replaceFirst("/" + DIRECTORIO_SUBIDAS + "/", ""));
            Files.deleteIfExists(imagePath);
            imagenRepositorio.delete(imagenSeleccionada);
            refrescarGaleriaImagenes();
            JOptionPane.showMessageDialog(this, "Imagen eliminada con éxito.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar archivo de imagen: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error deleting image file: " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar imagen de la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error deleting image from DB: " + e.getMessage());
        }
    }

    private JPanel crearPanelNoticias() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(COLOR_BG);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        campoTituloNoticia = new JTextField(20);
        areaContenidoNoticia = new JTextArea(15, 50);
        areaContenidoNoticia.setMargin(new Insets(5, 5, 5, 5));
        areaContenidoNoticia.setFont(FONT_NORMAL);
        areaContenidoNoticia.setLineWrap(true);
        areaContenidoNoticia.setWrapStyleWord(true);

        JPanel barraHerramientas = new JPanel(new WrapLayout(FlowLayout.LEFT));
        campoTituloNoticia.putClientProperty("FlatLaf.placeholderText", "Título de la noticia...");
        barraHerramientas.setBackground(COLOR_BG);
        barraHerramientas.setBorder(new EmptyBorder(5, 5, 5, 5));

        comboNoticias = new JComboBox<>();
        comboNoticias.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Noticia) setText(((Noticia) value).getTitulo());
                return this;
            }
        });

        JButton btnCargarPost = new JButton("Editar Seleccionada");
        JButton btnNuevoPost = new JButton("Nueva");
        JButton btnInsertarImg = new JButton("Insertar Imagen (URL)");
        JButton btnEliminarPost = new JButton("Eliminar");
        estilizarBoton(btnEliminarPost, new Color(192, 57, 43));

        btnInsertarImg.addActionListener(e -> {
            JDialog dialog = new JDialog(this, "Seleccionar Imagen para Noticia", true);
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.getContentPane().setBackground(COLOR_BG);

            JList<Imagen> selectorList = new JList<>(modeloImagenes);
            selectorList.setCellRenderer(new RenderizadorCeldaImagen());
            selectorList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            selectorList.setVisibleRowCount(-1);
            selectorList.setFixedCellWidth(120);
            selectorList.setFixedCellHeight(120);

            JScrollPane scrollPane = new JScrollPane(selectorList);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JButton btnSelect = new JButton("Insertar");
            estilizarBoton(btnSelect, COLOR_ACCENT);
            btnSelect.addActionListener(event -> {
                Imagen selected = selectorList.getSelectedValue();
                if (selected != null) {
                    areaContenidoNoticia.append("<img src='" + selected.getUrlImagen() + "' style='max-width:100%; height:auto;'/><br>");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Por favor, selecciona una imagen.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                }
            });

            JButton btnCancel = new JButton("Cancelar");
            estilizarBoton(btnCancel, new Color(192, 57, 43));
            btnCancel.addActionListener(event -> dialog.dispose());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(COLOR_BG);
            buttonPanel.add(btnSelect);
            buttonPanel.add(btnCancel);

            dialog.add(scrollPane, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });

        btnCargarPost.addActionListener(e -> cargarNoticiaSeleccionada());
        btnNuevoPost.addActionListener(e -> reiniciarCamposNoticia());
        btnEliminarPost.addActionListener(e -> eliminarNoticia());

        JLabel lblMod = new JLabel("Modificar:");
        lblMod.setForeground(Color.WHITE);
        barraHerramientas.add(lblMod);
        barraHerramientas.add(comboNoticias);
        barraHerramientas.add(btnCargarPost);
        barraHerramientas.add(btnNuevoPost);
        barraHerramientas.add(new JSeparator(SwingConstants.VERTICAL));

        JLabel lblTitle = new JLabel("Título:");
        lblTitle.setForeground(Color.WHITE);
        barraHerramientas.add(lblTitle);
        barraHerramientas.add(campoTituloNoticia);
        barraHerramientas.add(btnInsertarImg);
        barraHerramientas.add(btnEliminarPost);

        JButton btn = new JButton("Guardar Noticia / Cambios");
        estilizarBoton(btn, COLOR_ACCENT);
        btn.addActionListener(e -> {
            Noticia noticia = (idNoticiaEditando != null)
                    ? noticiaRepositorio.findById(idNoticiaEditando).orElse(new Noticia())
                    : new Noticia();
            noticia.setTitulo(campoTituloNoticia.getText());
            noticia.setContenido(areaContenidoNoticia.getText());
            noticiaRepositorio.save(noticia);
            JOptionPane.showMessageDialog(this, "Noticia guardada con éxito");
            reiniciarCamposNoticia();
            refrescarComboNoticias();
        });

        p.add(barraHerramientas, BorderLayout.NORTH);
        p.add(new JScrollPane(areaContenidoNoticia), BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearPanelEquipo() {
        JPanel mainP = new JPanel(new BorderLayout(10, 10));
        mainP.setBackground(COLOR_BG);
        mainP.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel leftP = new JPanel(new GridBagLayout());
        leftP.setBackground(COLOR_BG);

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.fill = GridBagConstraints.HORIZONTAL;
        gbcLeft.weightx = 1.0;
        gbcLeft.insets = new Insets(0, 0, 15, 0);

        JPanel pTeam = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 5));
        Border titledTeam = BorderFactory.createTitledBorder(new LineBorder(new Color(0, 120, 215), 3), "1. Crear Nuevo Equipo", TitledBorder.LEFT, TitledBorder.TOP, FONT_NORMAL, Color.WHITE);
        pTeam.setBorder(new CompoundBorder(titledTeam, new EmptyBorder(10, 10, 10, 10)));
        pTeam.setBackground(new Color(30, 30, 30));
        pTeam.setOpaque(true);

        JLabel lblNameTeam = new JLabel("Nombre:");
        lblNameTeam.setForeground(Color.WHITE);
        campoNombreEquipo = new JTextField(15);
        campoNombreEquipo.setBackground(new Color(45, 48, 51));
        campoNombreEquipo.setForeground(Color.WHITE);

        botonGuardarEquipo = new JButton("Crear Equipo");
        estilizarBoton(botonGuardarEquipo, COLOR_PRIMARY);
        botonGuardarEquipo.addActionListener(e -> {
            String teamName = campoNombreEquipo.getText().trim();
            if (teamName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del equipo no puede estar vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Equipo t = (idEquipoEditando != null)
                    ? equipoRepositorio.findById(idEquipoEditando).orElse(new Equipo())
                    : new Equipo();
            t.setNombre(teamName);
            if (idEquipoEditando == null) t.setCategoria("General");
            equipoRepositorio.save(t);
            refrescarComboEquipos();
            reiniciarCamposEquipo();
            JOptionPane.showMessageDialog(this, idEquipoEditando == null ? "Equipo creado: " + teamName : "Equipo actualizado");
        });

        pTeam.add(lblNameTeam);
        pTeam.add(campoNombreEquipo);
        pTeam.add(botonGuardarEquipo);

        gbcLeft.gridx = 0; gbcLeft.gridy = 0;
        leftP.add(pTeam, gbcLeft);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(COLOR_PRIMARY.brighter());
        gbcLeft.gridy = 1;
        gbcLeft.insets = new Insets(10, 0, 10, 0);
        leftP.add(separator, gbcLeft);

        JPanel pMem = new JPanel(new GridBagLayout());
        Border titledMem = BorderFactory.createTitledBorder(new LineBorder(new Color(0, 120, 215), 3), "2. Añadir Jugador / Staff", TitledBorder.LEFT, TitledBorder.TOP, FONT_NORMAL, Color.WHITE);
        pMem.setBorder(new CompoundBorder(titledMem, new EmptyBorder(10, 10, 10, 10)));
        pMem.setBackground(new Color(30, 30, 30));
        pMem.setOpaque(true);

        GridBagConstraints gbcMem = new GridBagConstraints();
        gbcMem.insets = new Insets(5, 5, 5, 5);
        gbcMem.fill = GridBagConstraints.HORIZONTAL;
        gbcMem.weightx = 1.0;

        comboEquipos = new JComboBox<>();
        comboEquipos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Equipo) setText(((Equipo) value).getNombre());
                setForeground(Color.WHITE);
                setBackground(isSelected ? COLOR_ACCENT : new Color(45, 48, 51));
                return this;
            }
        });
        comboEquipos.setBackground(new Color(45, 48, 51));
        comboEquipos.setForeground(Color.WHITE);
        comboEquipos.addActionListener(e -> refrescarListaMiembros());

        JPanel teamActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        teamActionPanel.setOpaque(false);
        teamActionPanel.add(comboEquipos);

        JButton btnEditarEquipo = new JButton("Modificar");
        estilizarBoton(btnEditarEquipo, COLOR_PRIMARY);
        btnEditarEquipo.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnEditarEquipo.setToolTipText("Modificar nombre del equipo seleccionado");
        btnEditarEquipo.addActionListener(e -> cargarEquipoSeleccionado());

        JButton btnEliminarEquipo = new JButton("Eliminar");
        estilizarBoton(btnEliminarEquipo, new Color(192, 57, 43));
        btnEliminarEquipo.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnEliminarEquipo.setToolTipText("Eliminar equipo seleccionado");
        btnEliminarEquipo.addActionListener(e -> eliminarEquipo());

        teamActionPanel.add(btnEditarEquipo);
        teamActionPanel.add(btnEliminarEquipo);

        campoNombreMiembro = new JTextField(20);
        campoNombreMiembro.setBackground(new Color(45, 48, 51));
        campoNombreMiembro.setForeground(Color.WHITE);
        campoNombreMiembro.putClientProperty("FlatLaf.placeholderText", "Nombre completo");

        JPanel photoFieldPanel = new JPanel(new BorderLayout());
        photoFieldPanel.setBackground(new Color(45, 48, 51));
        campoFotoMiembro = new JTextField();
        campoFotoMiembro.setBackground(new Color(45, 48, 51));
        campoFotoMiembro.setForeground(Color.WHITE);
        campoFotoMiembro.putClientProperty("FlatLaf.placeholderText", "URL o selecciona...");
        campoFotoMiembro.setBorder(new EmptyBorder(5, 5, 5, 5));
        photoFieldPanel.add(campoFotoMiembro, BorderLayout.CENTER);

        JButton btnBrowseMem = new JButton("...");
        btnBrowseMem.setPreferredSize(new Dimension(35, 30));
        btnBrowseMem.setBackground(COLOR_ACCENT);
        btnBrowseMem.setForeground(Color.WHITE);
        btnBrowseMem.putClientProperty("FlatLaf.style", "arc: 5;");
        btnBrowseMem.addActionListener(e -> mostrarDialogoSeleccionImagen(campoFotoMiembro));
        photoFieldPanel.add(btnBrowseMem, BorderLayout.EAST);

        String[] roles = {"Jugador", "Entrenador", "Delegado", "Fisio"};
        comboRoles = new JComboBox<>(roles);
        comboRoles.setBackground(new Color(45, 48, 51));
        comboRoles.setForeground(Color.WHITE);

        JButton btnMem = new JButton("Guardar / Añadir");
        estilizarBoton(btnMem, COLOR_ACCENT);
        btnMem.addActionListener(e -> {
            Equipo selectedTeam = (Equipo) comboEquipos.getSelectedItem();
            if (selectedTeam == null) {
                JOptionPane.showMessageDialog(this, "Por favor, selecciona un equipo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String memberName = campoNombreMiembro.getText().trim();
            if (memberName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del miembro no puede estar vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Miembro miembro = (idMiembroEditando != null)
                    ? miembroRepositorio.findById(idMiembroEditando).orElse(new Miembro())
                    : new Miembro();
            miembro.setNombre(memberName);
            miembro.setRol(comboRoles.getSelectedItem().toString());
            miembro.setUrlFoto(campoFotoMiembro.getText());
            miembro.setEquipo(selectedTeam);
            miembroRepositorio.save(miembro);
            refrescarListaMiembros();
            reiniciarCamposMiembro();
            JOptionPane.showMessageDialog(this, idMiembroEditando == null ? "Miembro añadido" : "Miembro actualizado");
        });

        int memRow = 0;
        JLabel lblSelEq = new JLabel("Seleccionar Equipo:"); lblSelEq.setForeground(Color.WHITE);
        pMem.add(lblSelEq, obtenerGBCMiembro(gbcMem, 0, memRow, GridBagConstraints.WEST));
        pMem.add(teamActionPanel, obtenerGBCMiembro(gbcMem, 1, memRow++, GridBagConstraints.EAST));

        JLabel lblNameMem = new JLabel("Nombre:"); lblNameMem.setForeground(Color.WHITE);
        pMem.add(lblNameMem, obtenerGBCMiembro(gbcMem, 0, memRow, GridBagConstraints.WEST));
        pMem.add(campoNombreMiembro, obtenerGBCMiembro(gbcMem, 1, memRow++, GridBagConstraints.EAST));

        JLabel lblRolMem = new JLabel("Rol:"); lblRolMem.setForeground(Color.WHITE);
        pMem.add(lblRolMem, obtenerGBCMiembro(gbcMem, 0, memRow, GridBagConstraints.WEST));
        pMem.add(comboRoles, obtenerGBCMiembro(gbcMem, 1, memRow++, GridBagConstraints.EAST));

        JLabel lblPhotoMem = new JLabel("URL Foto:"); lblPhotoMem.setForeground(Color.WHITE);
        pMem.add(lblPhotoMem, obtenerGBCMiembro(gbcMem, 0, memRow, GridBagConstraints.WEST));
        pMem.add(photoFieldPanel, obtenerGBCMiembro(gbcMem, 1, memRow++, GridBagConstraints.EAST));

        gbcMem.gridx = 0; gbcMem.gridy = memRow; gbcMem.gridwidth = 2;
        gbcMem.anchor = GridBagConstraints.EAST;
        gbcMem.insets = new Insets(10, 5, 5, 5);
        pMem.add(btnMem, gbcMem);

        gbcLeft.gridy = 2;
        gbcLeft.insets = new Insets(0, 0, 0, 0);
        leftP.add(pMem, gbcLeft);

        JPanel rightP = new JPanel(new BorderLayout(10, 10));
        Border titledRight = BorderFactory.createTitledBorder(new LineBorder(new Color(0, 120, 215), 3), "3. Integrantes del equipo seleccionado", TitledBorder.LEFT, TitledBorder.TOP, FONT_NORMAL, Color.WHITE);
        rightP.setBorder(new CompoundBorder(titledRight, new EmptyBorder(10, 10, 10, 10)));
        rightP.setBackground(new Color(30, 30, 30));
        rightP.setOpaque(true);

        listaMiembros = new JList<>(modeloMiembros);
        listaMiembros.setCellRenderer(new RenderizadorListaMiembros());
        listaMiembros.setFixedCellHeight(60);
        listaMiembros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton btnEditMem = new JButton("✏️ Modificar");
        estilizarBoton(btnEditMem, COLOR_ACCENT);
        btnEditMem.addActionListener(e -> cargarMiembroSeleccionado());

        JButton btnDeleteMem = new JButton("🗑️ Eliminar");
        estilizarBoton(btnDeleteMem, new Color(192, 57, 43));
        btnDeleteMem.addActionListener(e -> eliminarMiembro());

        JPanel memberActions = new JPanel(new GridLayout(1, 2, 10, 0));
        memberActions.setBackground(new Color(30, 30, 30));
        memberActions.add(btnEditMem);
        memberActions.add(btnDeleteMem);

        rightP.add(new JScrollPane(listaMiembros), BorderLayout.CENTER);
        rightP.add(memberActions, BorderLayout.SOUTH);

        mainP.add(leftP, BorderLayout.WEST);
        mainP.add(rightP, BorderLayout.CENTER);
        return mainP;
    }

    private GridBagConstraints obtenerGBCMiembro(GridBagConstraints gbc, int gridx, int gridy, int anchor) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = 1;
        gbc.anchor = anchor;
        gbc.weightx = (gridx == 0) ? 0.0 : 1.0;
        return gbc;
    }

    @jakarta.annotation.PostConstruct
    public void cargarDatosAlIniciar() {
        try {
            if (usuarioActual == null) return;

            if (usuarioActual.getClub() == null) {
                usuarioActual.setClub(new ClubInformacion());
                usuarioActual.getClub().setNombre("Mi Club");
            }

            ClubInformacion info = usuarioActual.getClub();
            if (campoNombreClub != null)    campoNombreClub.setText(info.getNombre());
            if (campoLogo != null)          campoLogo.setText(info.getUrlLogo());
            if (campoImagenFondo != null)   campoImagenFondo.setText(info.getUrlImagenFondo());
            if (campoFacebook != null)      campoFacebook.setText(info.getFacebook());
            if (campoInstagram != null)     campoInstagram.setText(info.getInstagram());
            if (campoTwitter != null)       campoTwitter.setText(info.getTwitter());
            if (campoFondoEncabezado != null) campoFondoEncabezado.setText(info.getColorFondoEncabezado());
            if (campoTextoEncabezado != null) campoTextoEncabezado.setText(info.getColorTextoEncabezado());
            if (campoYoutubeDirecto != null)  campoYoutubeDirecto.setText(info.getUrlYoutubeDirecto());

            refrescarComboEquipos();
            refrescarComboNoticias();
            refrescarGaleriaImagenes();
        } catch (Exception e) {
            System.err.println("Error al cargar datos iniciales: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refrescarComboEquipos() {
        comboEquipos.removeAllItems();
        equipoRepositorio.findAll().forEach(comboEquipos::addItem);
    }

    private void refrescarComboNoticias() {
        comboNoticias.removeAllItems();
        noticiaRepositorio.findAll().forEach(comboNoticias::addItem);
    }

    private void refrescarListaMiembros() {
        modeloMiembros.clear();
        Equipo selected = (Equipo) comboEquipos.getSelectedItem();
        if (selected != null && selected.getId() != null) {
            try {
                miembroRepositorio.findByEquipo_Id(selected.getId()).forEach(modeloMiembros::addElement);
            } catch (Exception e) {
                System.err.println("Error al refrescar lista de miembros: " + e.getMessage());
            }
        }
    }

    private void cargarNoticiaSeleccionada() {
        Noticia p = (Noticia) comboNoticias.getSelectedItem();
        if (p != null) {
            idNoticiaEditando = p.getId();
            campoTituloNoticia.setText(p.getTitulo());
            areaContenidoNoticia.setText(p.getContenido());
        }
    }

    private void cargarEquipoSeleccionado() {
        Equipo t = (Equipo) comboEquipos.getSelectedItem();
        if (t != null) {
            idEquipoEditando = t.getId();
            campoNombreEquipo.setText(t.getNombre());
            botonGuardarEquipo.setText("Guardar Cambios");
            campoNombreEquipo.requestFocus();
        }
    }

    private void eliminarEquipo() {
        Equipo t = (Equipo) comboEquipos.getSelectedItem();
        if (t == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de eliminar el equipo '" + t.getNombre() + "'?\nEsto eliminará también a todos sus integrantes.",
            "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            equipoRepositorio.delete(t);
            refrescarComboEquipos();
            reiniciarCamposEquipo();
            modeloMiembros.clear();
            JOptionPane.showMessageDialog(this, "Equipo eliminado correctamente.");
        }
    }

    private void cargarMiembroSeleccionado() {
        Miembro m = listaMiembros.getSelectedValue();
        if (m != null) {
            idMiembroEditando = m.getId();
            campoNombreMiembro.setText(m.getNombre());
            campoFotoMiembro.setText(m.getUrlFoto());
            comboRoles.setSelectedItem(m.getRol());
            comboEquipos.setSelectedItem(m.getEquipo());
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un integrante de la lista para modificar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void reiniciarCamposMiembro() {
        idMiembroEditando = null;
        campoNombreMiembro.setText("");
        campoFotoMiembro.setText("");
        comboRoles.setSelectedIndex(0);
    }

    private void reiniciarCamposEquipo() {
        idEquipoEditando = null;
        campoNombreEquipo.setText("");
        botonGuardarEquipo.setText("Crear Equipo");
    }

    private void reiniciarCamposNoticia() {
        idNoticiaEditando = null;
        campoTituloNoticia.setText("");
        areaContenidoNoticia.setText("");
    }

    private void eliminarNoticia() {
        if (idNoticiaEditando == null) return;
        noticiaRepositorio.deleteById(idNoticiaEditando);
        reiniciarCamposNoticia();
        refrescarComboNoticias();
        JOptionPane.showMessageDialog(this, "Noticia eliminada");
    }

    private void eliminarMiembro() {
        Miembro m = listaMiembros.getSelectedValue();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un integrante para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de que quieres eliminar a " + m.getNombre() + "?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            miembroRepositorio.delete(m);
            refrescarListaMiembros();
            JOptionPane.showMessageDialog(this, "Integrante eliminado.");
        }
    }

    private void seleccionarColor(JTextField target) {
        String current = target.getText();
        Color initialColor = (current != null && current.startsWith("#") && current.length() == 7)
                ? Color.decode(current) : Color.WHITE;
        Color c = JColorChooser.showDialog(this, "Selecciona un color", initialColor);
        if (c != null)
            target.setText(String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
    }

    private void mostrarDialogoSeleccionImagen(JTextField targetField) {
        JDialog dialog = new JDialog(this, "Seleccionar Imagen", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(COLOR_BG);

        JList<Imagen> selectorList = new JList<>(modeloImagenes);
        selectorList.setCellRenderer(new RenderizadorCeldaImagen());
        selectorList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        selectorList.setVisibleRowCount(-1);
        selectorList.setFixedCellWidth(120);
        selectorList.setFixedCellHeight(120);

        JScrollPane scrollPane = new JScrollPane(selectorList);
        scrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        JButton btnSelect = new JButton("Seleccionar");
        estilizarBoton(btnSelect, COLOR_ACCENT);
        btnSelect.addActionListener(e -> {
            Imagen selected = selectorList.getSelectedValue();
            if (selected != null) {
                targetField.setText(selected.getUrlImagen());
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Por favor, selecciona una imagen.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnCancel = new JButton("Cancelar");
        estilizarBoton(btnCancel, new Color(192, 57, 43));
        btnCancel.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(COLOR_BG);
        buttonPanel.add(btnSelect);
        buttonPanel.add(btnCancel);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void cargarLogoApp() {
        try {
            URL resource = getClass().getResource(LOGO_PATH);
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                etiquetaLogoBarraLateral.setIcon(new ImageIcon(icon.getImage().getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH)));
            } else {
                etiquetaLogoBarraLateral.setText("👤");
                etiquetaLogoBarraLateral.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
                etiquetaLogoBarraLateral.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            etiquetaLogoBarraLateral.setText("?");
        }
    }

    private void guardarClub() {
        if (usuarioActual == null) {
            JOptionPane.showMessageDialog(this, "Error: No hay un usuario autenticado.", "Error de Sesión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (usuarioActual.getClub() == null)
            usuarioActual.setClub(new ClubInformacion());

        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Deseas guardar los cambios en la configuración del club?",
            "Confirmar Guardado", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) return;

        try {
            ClubInformacion info = usuarioActual.getClub();
            info.setNombre(campoNombreClub.getText());
            info.setUrlLogo(campoLogo.getText());
            info.setFacebook(campoFacebook.getText());
            info.setInstagram(campoInstagram.getText());
            info.setTwitter(campoTwitter.getText());
            info.setColorFondoEncabezado(campoFondoEncabezado.getText());
            info.setColorTextoEncabezado(campoTextoEncabezado.getText());
            info.setUrlImagenFondo(campoImagenFondo.getText());
            info.setUrlYoutubeDirecto(campoYoutubeDirecto.getText());
            clubRepositorio.save(info);
            JOptionPane.showMessageDialog(this, "¡Configuración actualizada con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error crítico al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    class RenderizadorListaMiembros extends JPanel implements ListCellRenderer<Miembro> {
        private JLabel etiquetaFoto;
        private JLabel etiquetaInfo;

        public RenderizadorListaMiembros() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(5, 5, 5, 5));

            etiquetaFoto = new JLabel();
            etiquetaFoto.setPreferredSize(new Dimension(50, 50));
            etiquetaFoto.setHorizontalAlignment(SwingConstants.CENTER);
            etiquetaFoto.setVerticalAlignment(SwingConstants.CENTER);
            add(etiquetaFoto, BorderLayout.WEST);

            etiquetaInfo = new JLabel();
            add(etiquetaInfo, BorderLayout.CENTER);
        }

        @Override
        public java.awt.Component getListCellRendererComponent(JList<? extends Miembro> list, Miembro value, int index, boolean isSelected, boolean cellHasFocus) {
            etiquetaInfo.setText("<html><b>" + value.getNombre() + "</b><br>" + value.getRol() + "</html>");
            etiquetaInfo.setForeground(Color.WHITE);
            setBackground(isSelected ? COLOR_ACCENT : new Color(45, 45, 45));
            setBorder(BorderFactory.createLineBorder(isSelected ? COLOR_PRIMARY : Color.DARK_GRAY, 1));

            if (value.getUrlFoto() != null && !value.getUrlFoto().isEmpty()) {
                updatePreviewImage(value.getUrlFoto(), etiquetaFoto);
            } else {
                etiquetaFoto.setIcon(null);
                etiquetaFoto.setText("👤");
                etiquetaFoto.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
                etiquetaFoto.setForeground(Color.WHITE);
            }
            return this;
        }
    }

    class RenderizadorCeldaImagen extends JPanel implements ListCellRenderer<Imagen> {
        private JLabel etiquetaImagen;
        private JLabel etiquetaNombre;

        public RenderizadorCeldaImagen() {
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            setBackground(new Color(40, 40, 40));

            etiquetaImagen = new JLabel();
            etiquetaImagen.setHorizontalAlignment(SwingConstants.CENTER);
            etiquetaImagen.setPreferredSize(new Dimension(100, 100));
            add(etiquetaImagen, BorderLayout.CENTER);

            etiquetaNombre = new JLabel();
            etiquetaNombre.setHorizontalAlignment(SwingConstants.CENTER);
            etiquetaNombre.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            etiquetaNombre.setForeground(Color.LIGHT_GRAY);
            add(etiquetaNombre, BorderLayout.SOUTH);
        }

        @Override
        public java.awt.Component getListCellRendererComponent(JList<? extends Imagen> list, Imagen value, int index, boolean isSelected, boolean cellHasFocus) {
            etiquetaNombre.setText(value.getNombreArchivo());

            new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    java.nio.file.Path imagePath = java.nio.file.Paths.get(RUTA_ALMACENAMIENTO, DIRECTORIO_SUBIDAS,
                            value.getUrlImagen().replaceFirst("/" + DIRECTORIO_SUBIDAS + "/", ""));
                    if (Files.exists(imagePath)) {
                        try {
                            BufferedImage img = javax.imageio.ImageIO.read(imagePath.toFile());
                            if (img != null)
                                return new ImageIcon(img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH));
                        } catch (IOException e) {
                            System.err.println("Error loading image for gallery: " + imagePath + " - " + e.getMessage());
                        }
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        ImageIcon icon = get();
                        if (icon != null) {
                            etiquetaImagen.setIcon(icon);
                            etiquetaImagen.setText("");
                        } else {
                            etiquetaImagen.setIcon(null);
                            etiquetaImagen.setText("Error");
                        }
                    } catch (Exception ignored) {
                        etiquetaImagen.setIcon(null);
                        etiquetaImagen.setText("Error");
                    }
                }
            }.execute();

            if (isSelected) {
                setBackground(COLOR_ACCENT);
                etiquetaNombre.setForeground(Color.WHITE);
            } else {
                setBackground(new Color(40, 40, 40));
                etiquetaNombre.setForeground(Color.LIGHT_GRAY);
            }
            setBorder(BorderFactory.createLineBorder(isSelected ? COLOR_PRIMARY : Color.LIGHT_GRAY, 2));
            return this;
        }
    }

    class BordeDiscontinuo extends AbstractBorder {
        private Color color;

        public BordeDiscontinuo(Color color) { this.color = color; }

        @Override
        public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color);
            float[] dash = {5f, 5f};
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, dash, 0f));
            g2d.drawRect(x, y, width - 1, height - 1);
            g2d.dispose();
        }
    }
}