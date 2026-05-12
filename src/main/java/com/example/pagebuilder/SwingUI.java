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
public class SwingUI extends JFrame {

    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private ImageRepository imageRepository; // New: Image Repository
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private LoginUI loginUI;

    // Caché de imágenes para evitar recargas constantes y mejorar el rendimiento
    private final Map<String, ImageIcon> imageCache = new HashMap<>();

    // Colores y Fuentes Estilizadas
    private final Color COLOR_PRIMARY = new Color(44, 62, 80);   // Azul oscuro
    private final Color COLOR_SIDEBAR = new Color(38, 41, 44);   // Gris Elementor
    private final Color COLOR_SIDEBAR_DARK = new Color(26, 28, 30);
    private final Color COLOR_ACCENT = new Color(113, 215, 241);    // Celeste Elementor
    private final Color COLOR_BG = new Color(15, 15, 15);           // Negro casi puro
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 15);

    private static final String UPLOAD_DIR = "uploads";
    private static final String STORAGE_PATH = System.getProperty("user.home") + File.separator + ".adminbaloncesto";
    private static final String LOGO_PATH = "/static/logo.png";

    private User currentUser;

    public void setCurrentUser(User user) { this.currentUser = user; }

    private Long editingPostId = null;
    private Long editingMemberId = null;
    private Long editingTeamId = null;
    private JTextField txtClubName, txtLogo, txtFB, txtIG, txtTW, txtHeaderBg, txtHeaderTxt, txtBackgroundImg, txtYoutubeLive;
    private JTextField txtPostTitle, txtTeamName, txtMemName, txtMemPhoto;
    private JComboBox<Team> comboTeams;
    private JComboBox<String> comboRoles;
    private JComboBox<Post> comboPosts;
    private JList<Member> listMembers;
    private DefaultListModel<Member> memberModel;
    private JList<com.example.pagebuilder.Image> listImages; // Usamos ruta completa para evitar conflicto con java.awt.Image
    private DefaultListModel<com.example.pagebuilder.Image> imageModel; 
    private JTextArea txtPostContent;
    private JButton btnTeamSave;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JLabel lblSidebarLogo;

    public SwingUI() {
        setTitle("Admin Club de Baloncesto");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // 1. Inicializar modelos ANTES de crear los paneles para evitar referencias nulas
        imageModel = new DefaultListModel<>();
        memberModel = new DefaultListModel<>();

        // 2. Asegurar que el directorio de subidas existe antes de iniciar la UI
        try {
            Files.createDirectories(Paths.get(STORAGE_PATH, UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Could not create upload directory: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error al crear directorio de subidas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        getContentPane().setBackground(COLOR_SIDEBAR_DARK);
        
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(COLOR_BG);

        // Agregamos las vistas al panel de tarjetas
        cardPanel.add(createClubPanel(), "CLUB");
        cardPanel.add(createPostPanel(), "NOTICIAS");
        cardPanel.add(createTeamPanel(), "EQUIPOS");
        cardPanel.add(createImageGalleryPanel(), "GALERIA");

        // Menú de navegación lateral IZQUIERDO (Botones separados)
        JPanel navPanel = new JPanel(new GridBagLayout());
        navPanel.setBackground(new Color(0, 120, 215));
        navPanel.setPreferredSize(new Dimension(250, 0));
        navPanel.setBorder(new EmptyBorder(30, 15, 30, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Logo Club
        lblSidebarLogo = new JLabel();
        lblSidebarLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblSidebarLogo.setPreferredSize(new Dimension(220, 120));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0); 
        navPanel.add(lblSidebarLogo, gbc);
        loadAppLogo();

        // Definimos los 3 botones principales 
        gbc.insets = new Insets(0, 0, 15, 0); 
        gbc.gridy = 1; navPanel.add(createNavButton("⚙️ CONFIG", "CLUB"), gbc);
        gbc.gridy = 2; navPanel.add(createNavButton("📰 NOTICIAS", "NOTICIAS"), gbc);
        gbc.gridy = 3; navPanel.add(createNavButton("👥 EQUIPOS", "EQUIPOS"), gbc);

        // Botón secundario para la Galería
        gbc.insets = new Insets(40, 0, 0, 0); // Salto de espacio mayor
        gbc.gridy = 4; navPanel.add(createNavButton("🖼️ GALERÍA", "GALERIA"), gbc);

        gbc.gridy = 5; gbc.weighty = 1.0;
        navPanel.add(Box.createVerticalGlue(), gbc);

        // Botón de Cerrar Sesión
        JButton btnLogout = new JButton("🚪 CERRAR SESIÓN");
        styleButton(btnLogout, new Color(192, 57, 43)); // Color rojo para destacar acción de salida
        btnLogout.addActionListener(e -> {
            this.dispose(); // Cierra la ventana actual
            loginUI.setVisible(true); // Muestra de nuevo el login
        });
        gbc.gridy = 6; gbc.weighty = 0; gbc.insets = new Insets(10, 0, 0, 0);
        navPanel.add(btnLogout, gbc);

        add(navPanel, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);

    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(60, 64, 67));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(15, 10, 15, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("FlatLaf.style", "arc: 12;");
        btn.addActionListener(e -> cardLayout.show(cardPanel, cardName));
        return btn;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBorder(new EmptyBorder(15, 25, 15, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // FlatLaf specific styling for rounded corners and hover
        btn.putClientProperty("FlatLaf.style", "arc: 15;"); // Rounded corners
        btn.putClientProperty("JButton.buttonType", "roundRect"); // Another FlatLaf hint
        btn.putClientProperty("JButton.hoverBackground", new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 200)); // Slightly darker on hover
    }

    private JPanel createClubPanel() {
        JPanel mainP = new JPanel(new GridBagLayout()); // Usar GridBagLayout para centrar
        mainP.setBackground(COLOR_BG);

        // Panel para contener todas las secciones de configuración, con márgenes y centrado
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(COLOR_BG);
        contentPanel.setBorder(new EmptyBorder(20, 50, 20, 50)); // Márgenes a los lados

        GridBagConstraints gbcContent = new GridBagConstraints();
        gbcContent.gridx = 0;
        gbcContent.fill = GridBagConstraints.HORIZONTAL;
        gbcContent.weightx = 1.0;
        gbcContent.insets = new Insets(10, 0, 10, 0); // Espacio entre "recuadros"

        txtClubName = new JTextField();
        txtLogo = new JTextField();
        txtFB = new JTextField();
        txtIG = new JTextField();
        txtTW = new JTextField();
        txtHeaderBg = new JTextField("#2c3e50"); // Default a un azul oscuro
        txtHeaderTxt = new JTextField("#ecf0f1"); // Default a un texto claro
        txtBackgroundImg = new JTextField();
        txtYoutubeLive = new JTextField();

        // --- Recuadro: Identidad del Club ---
        JPanel identityPanel = createSectionPanel("Identidad del Club");
        addConfigField(identityPanel, txtClubName, "🏷️ Nombre del Club:", false);
        addConfigField(identityPanel, txtLogo, "🖼️ Logo (URL):", true);
        addConfigField(identityPanel, txtBackgroundImg, "🌆 Imagen Fondo (URL):", true);
        gbcContent.gridy = 0;
        contentPanel.add(identityPanel, gbcContent);

        // --- Recuadro: Redes Sociales ---
        JPanel socialPanel = createSectionPanel("Redes Sociales");
        addConfigField(socialPanel, txtFB, "📘 Facebook:", false);
        addConfigField(socialPanel, txtIG, "📸 Instagram:", false);
        addConfigField(socialPanel, txtTW, "🐦 Twitter:", false);
        gbcContent.gridy = 1;
        contentPanel.add(socialPanel, gbcContent);

        // --- Recuadro: Multimedia ---
        JPanel multimediaPanel = createSectionPanel("Multimedia");
        addConfigField(multimediaPanel, txtYoutubeLive, "📺 YouTube Directo (URL):", false);
        gbcContent.gridy = 2;
        contentPanel.add(multimediaPanel, gbcContent);

        // --- Recuadro: Estilo del Header ---
        JPanel headerStylePanel = createSectionPanel("Estilo del Header");
        JButton btnBgColor = createSidebarButton("Seleccionar Color Fondo Header");
        btnBgColor.addActionListener(e -> pickColor(txtHeaderBg));
        headerStylePanel.add(btnBgColor, createGBC(0, 0, 2, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5)));

        JButton btnTxtColor = createSidebarButton("Seleccionar Color Texto Header");
        btnTxtColor.addActionListener(e -> pickColor(txtHeaderTxt));
        headerStylePanel.add(btnTxtColor, createGBC(0, 1, 2, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5)));
        gbcContent.gridy = 3;
        contentPanel.add(headerStylePanel, gbcContent);

        // --- Botones de Acción ---
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionButtonsPanel.setBackground(COLOR_BG); // Coincide con el fondo principal
        JButton btnSave = new JButton("GUARDAR CAMBIOS");
        styleButton(btnSave, new Color(146, 39, 143));
        btnSave.addActionListener(e -> saveClub());
        actionButtonsPanel.add(btnSave);

        JButton btnOpenWebsite = new JButton("ABRIR PÁGINA WEB");
        styleButton(btnOpenWebsite, COLOR_ACCENT);
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

        // Añadir el contentPanel al mainP, centrado
        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.gridx = 0;
        gbcMain.gridy = 0;
        gbcMain.weightx = 1.0;
        gbcMain.weighty = 1.0;
        gbcMain.fill = GridBagConstraints.BOTH; // Permitir que contentPanel se expanda
        mainP.add(new JScrollPane(contentPanel), gbcMain); // Envolver en JScrollPane si el contenido puede ser largo

        return mainP;
    }

    // Helper method to create a section panel with a titled border
    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 30, 30)); // Fondo más oscuro para el "recuadro"
        panel.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(new LineBorder(new Color(0, 120, 215), 3), title, TitledBorder.LEFT, TitledBorder.TOP, FONT_NORMAL, Color.WHITE),
            new EmptyBorder(10, 10, 10, 10) // Padding interno
        ));
        return panel;
    }

    // Helper method to add a labeled text field (with optional image browse button) to a panel
    private void addConfigField(JPanel targetPanel, JTextField field, String labelText, boolean isImageField) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel(labelText);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = targetPanel.getComponentCount() / 2; // Simple way to get row for new component
        gbc.weightx = 0.0; // La etiqueta no se expande
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
            browseBtn.setMargin(new Insets(0,0,0,0));
            browseBtn.setFocusPainted(false);
            browseBtn.setBackground(COLOR_ACCENT);
            browseBtn.setForeground(Color.WHITE);
            browseBtn.putClientProperty("FlatLaf.style", "arc: 5;");
            browseBtn.addActionListener(e -> showImageSelectionDialog(field));
            fieldPanel.add(browseBtn, BorderLayout.EAST);
        }

        gbc.gridx = 1;
        gbc.weightx = 1.0; // El campo se expande
        targetPanel.add(fieldPanel, gbc);
    }

    // Helper for GridBagConstraints for buttons
    private GridBagConstraints createGBC(int gridx, int gridy, int gridwidth, int fill, Insets insets) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.fill = fill;
        gbc.insets = insets;
        gbc.weightx = 1.0; // Hacer que los botones se expandan horizontalmente
        return gbc;
    }

    private JButton createSidebarButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, new Color(60, 64, 67)); // Aplicar estilo general de botón
        b.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente ligeramente más pequeña para estos botones específicos
        return b;
    }

    // Modified updatePreviewImage to handle local file paths from UPLOAD_DIR
    private void updatePreviewImage(String urlStr, JLabel label) {
        if (urlStr == null || urlStr.isEmpty()) {
            label.setIcon(null);
            label.setText("No Image");
            return;
        }

        if (imageCache.containsKey(urlStr)) {
            label.setIcon(imageCache.get(urlStr));
            label.setText("");
            return;
        }

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                // Calculamos dimensiones: si el componente aún no se dibujó (size 0), usamos el preferred size
                int w = label.getWidth() > 0 ? label.getWidth() : label.getPreferredSize().width;
                int h = label.getHeight() > 0 ? label.getHeight() : label.getPreferredSize().height;
                if (w <= 0) w = 100; if (h <= 0) h = 100;

                // Construct the local file path
                // Remove the leading /uploads/ if present, then combine with UPLOAD_DIR
                String relativePath = urlStr.startsWith("/" + UPLOAD_DIR + "/") ?
                                      urlStr.substring(("/" + UPLOAD_DIR + "/").length()) :
                                      urlStr; // If it's not an uploaded image URL, treat as direct path

                java.nio.file.Path imagePath = Paths.get(STORAGE_PATH, UPLOAD_DIR, relativePath);

                if (Files.exists(imagePath)) {
                    try {
                        BufferedImage img = javax.imageio.ImageIO.read(imagePath.toFile());
                        if (img != null) {
                            return new ImageIcon(img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH));
                        }
                    } catch (IOException e) {
                        System.err.println("Error loading local image: " + imagePath + " - " + e.getMessage());
                    }
                }
                
                // Fallback: Try to load as a full URL (e.g., external images)
                try {
                    URL url = new URL(urlStr);
                    ImageIcon icon = new ImageIcon(url);
                    if (icon.getIconWidth() > 0) {
                        java.awt.Image scaledImg = icon.getImage().getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImg);
                    }
                } catch (MalformedURLException e) {
                    // Not a valid URL, ignore
                } catch (Exception e) {
                    System.err.println("Error loading external image from URL: " + urlStr + " - " + e.getMessage());
                }
                
                return null; // Image could not be loaded
            }
            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        imageCache.put(urlStr, icon);
                        label.setIcon(icon);
                        label.setText("");
                    } else {
                        label.setIcon(null);
                        label.setText("Error"); // Indicate failure to load
                    }
                } catch (Exception ignored) {
                    label.setIcon(null);
                    label.setText("Error");
                }
            }
        }.execute();
    }

    // New method for Image Gallery Panel
    private JPanel createImageGalleryPanel() {
        JPanel mainP = new JPanel(new BorderLayout(10, 10));
        mainP.setBackground(COLOR_BG);
        mainP.setBorder(new EmptyBorder(10, 10, 10, 10));

        listImages = new JList<>(imageModel);
        listImages.setCellRenderer(new ImageCellRenderer());
        listImages.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        listImages.setVisibleRowCount(-1); // Display all items in a single row if possible, then wrap
        listImages.setFixedCellWidth(150); // Fixed width for image cells
        listImages.setFixedCellHeight(150); // Fixed height for image cells

        JScrollPane scrollPane = new JScrollPane(listImages);
        scrollPane.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(new LineBorder(new Color(0, 120, 215), 3), "Imágenes Subidas", TitledBorder.LEFT, TitledBorder.TOP, FONT_NORMAL, Color.WHITE),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JButton btnUpload = new JButton("Subir Nueva Imagen");
        styleButton(btnUpload, new Color(40, 40, 40));
        btnUpload.addActionListener(e -> uploadImage());

        JButton btnDelete = new JButton("Eliminar Imagen Seleccionada");
        styleButton(btnDelete, new Color(192, 57, 43));
        btnDelete.addActionListener(e -> deleteSelectedImage());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(COLOR_BG); // Ahora negro
        toolbar.add(btnUpload);
        toolbar.add(btnDelete);

        mainP.add(toolbar, BorderLayout.NORTH);
        mainP.add(scrollPane, BorderLayout.CENTER);

        return mainP;
    }

    private void refreshImageGallery() {
        imageModel.clear();
        imageRepository.findAll().forEach(imageModel::addElement);
    }

    private void uploadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Imagen para Subir");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String originalFilename = selectedFile.getName();
                String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
                java.nio.file.Path targetPath = Paths.get(STORAGE_PATH, UPLOAD_DIR, uniqueFilename);
                Files.copy(selectedFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                String imageUrl = "/" + UPLOAD_DIR + "/" + uniqueFilename;
                com.example.pagebuilder.Image image = com.example.pagebuilder.Image.builder()
                                .filename(originalFilename)
                                .url(imageUrl)
                                .build();
                imageRepository.save(image);
                refreshImageGallery();
                JOptionPane.showMessageDialog(this, "Imagen subida con éxito: " + originalFilename);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al subir imagen: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("Error uploading image: " + e.getMessage());
            }
        }
    }

    private void deleteSelectedImage() {
        com.example.pagebuilder.Image selectedImage = listImages.getSelectedValue();
        if (selectedImage == null) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona una imagen para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que quieres eliminar esta imagen?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Delete from file system
                java.nio.file.Path imagePath = Paths.get(STORAGE_PATH, UPLOAD_DIR, selectedImage.getUrl().replaceFirst("/" + UPLOAD_DIR + "/", ""));
                Files.deleteIfExists(imagePath);

                // Delete from database
                imageRepository.delete(selectedImage);
                refreshImageGallery();
                JOptionPane.showMessageDialog(this, "Imagen eliminada con éxito.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar archivo de imagen: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("Error deleting image file: " + e.getMessage());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar imagen de la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("Error deleting image from DB: " + e.getMessage());
            }
        }
    }

    private JPanel createPostPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(COLOR_BG);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        txtPostTitle = new JTextField(20);
        txtPostContent = new JTextArea(15, 50);
        txtPostContent.setMargin(new Insets(5,5,5,5)); // Add padding to text area
        txtPostContent.setFont(FONT_NORMAL);
        txtPostContent.setLineWrap(true);
        txtPostContent.setWrapStyleWord(true);
        
        JPanel toolbar = new JPanel(new WrapLayout(FlowLayout.LEFT));
        txtPostTitle.putClientProperty("FlatLaf.placeholderText", "Título de la noticia...");
        toolbar.setBackground(COLOR_BG);
        toolbar.setBorder(new EmptyBorder(5,5,5,5));
        
        comboPosts = new JComboBox<>();
        comboPosts.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Post) setText(((Post) value).getTitle());
                return this;
            }
        });
        JButton btnLoadPost = new JButton("Editar Seleccionada");
        JButton btnNewPost = new JButton("Nueva");
        JButton btnInsertImg = new JButton("Insertar Imagen (URL)");
        JButton btnDeletePost = new JButton("Eliminar");
        styleButton(btnDeletePost, new Color(192, 57, 43));

        btnInsertImg.addActionListener(e -> {
            // Open image selection dialog, then insert into JTextArea
            JDialog dialog = new JDialog(this, "Seleccionar Imagen para Noticia", true);
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.getContentPane().setBackground(COLOR_BG);

            JList<com.example.pagebuilder.Image> selectorList = new JList<>(imageModel); 
            selectorList.setCellRenderer(new ImageCellRenderer());
            selectorList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            selectorList.setVisibleRowCount(-1);
            selectorList.setFixedCellWidth(120);
            selectorList.setFixedCellHeight(120);
            JScrollPane scrollPane = new JScrollPane(selectorList);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

            JButton btnSelect = new JButton("Insertar");
            styleButton(btnSelect, COLOR_ACCENT);
            btnSelect.addActionListener(event -> {
                com.example.pagebuilder.Image selected = selectorList.getSelectedValue();
                if (selected != null) {
                    txtPostContent.append("<img src='" + selected.getUrl() + "' style='max-width:100%; height:auto;'/><br>");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Por favor, selecciona una imagen.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                }
            });

            JButton btnCancel = new JButton("Cancelar");
            styleButton(btnCancel, new Color(192, 57, 43));
            btnCancel.addActionListener(event -> dialog.dispose());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(COLOR_BG);
            buttonPanel.add(btnSelect);
            buttonPanel.add(btnCancel);

            dialog.add(scrollPane, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });
        btnLoadPost.addActionListener(e -> loadSelectedPost());
        btnNewPost.addActionListener(e -> resetPostFields());
        btnDeletePost.addActionListener(e -> deletePost());

        JLabel lblMod = new JLabel("Modificar:");
        lblMod.setForeground(Color.WHITE);
        toolbar.add(lblMod);
        toolbar.add(comboPosts);
        toolbar.add(btnLoadPost);
        toolbar.add(btnNewPost);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        
        JLabel lblTitle = new JLabel("Título:");
        lblTitle.setForeground(Color.WHITE);
        toolbar.add(lblTitle);
        toolbar.add(txtPostTitle);
        toolbar.add(btnInsertImg);
        toolbar.add(btnDeletePost);

        JButton btn = new JButton("Guardar Noticia / Cambios");
        styleButton(btn, COLOR_ACCENT);
        btn.addActionListener(e -> {
            Post post = (editingPostId != null) ? postRepository.findById(editingPostId).orElse(new Post()) : new Post();
            post.setTitle(txtPostTitle.getText());
            post.setContent(txtPostContent.getText());
            postRepository.save(post);
            JOptionPane.showMessageDialog(this, "Noticia guardada con éxito");
            resetPostFields();
            refreshPostCombo();
        });
        
        p.add(toolbar, BorderLayout.NORTH);
        p.add(new JScrollPane(txtPostContent), BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createTeamPanel() {
        JPanel mainP = new JPanel(new BorderLayout(10, 10));
        mainP.setBackground(COLOR_BG); // Negro
        mainP.setBorder(new EmptyBorder(15, 15, 15, 15)); // Padding for the main panel
        
        JPanel leftP = new JPanel(new GridBagLayout()); // Use GridBagLayout for leftP
        leftP.setBackground(COLOR_BG);

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.fill = GridBagConstraints.HORIZONTAL;
        gbcLeft.weightx = 1.0;
        gbcLeft.insets = new Insets(0, 0, 15, 0); // Bottom padding for sections

        // Subpanel: Crear Equipo
        JPanel pTeam = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 5)); // Added hgap, vgap
        Border titledTeam = BorderFactory.createTitledBorder(new LineBorder(new Color(0, 120, 215), 3), "1. Crear Nuevo Equipo", TitledBorder.LEFT, TitledBorder.TOP, FONT_NORMAL, Color.WHITE);
        pTeam.setBorder(new CompoundBorder(titledTeam, new EmptyBorder(10, 10, 10, 10)));
        
        pTeam.setBackground(new Color(30, 30, 30)); // Gris muy oscuro para contraste
        JLabel lblNameTeam = new JLabel("Nombre:");
        lblNameTeam.setForeground(Color.WHITE);
        pTeam.setOpaque(true); // Ensure background is visible
        txtTeamName = new JTextField(15); // Make it a bit smaller
        txtTeamName.setBackground(new Color(45, 48, 51));
        txtTeamName.setForeground(Color.WHITE);
        btnTeamSave = new JButton("Crear Equipo");
        styleButton(btnTeamSave, COLOR_PRIMARY);
        btnTeamSave.addActionListener(e -> {
            String teamName = txtTeamName.getText().trim();
            if (teamName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del equipo no puede estar vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Team t = (editingTeamId != null) ? teamRepository.findById(editingTeamId).orElse(new Team()) : new Team();
            t.setName(teamName);
            if (editingTeamId == null) t.setCategory("General"); // Default category for new teams
            teamRepository.save(t);
            refreshTeamCombo();
            resetTeamFields();
            JOptionPane.showMessageDialog(this, editingTeamId == null ? "Equipo creado: " + teamName : "Equipo actualizado");
        });
        
        pTeam.add(lblNameTeam); 
        pTeam.add(txtTeamName); 
        pTeam.add(btnTeamSave);

        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        leftP.add(pTeam, gbcLeft);

        // Separator
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(COLOR_PRIMARY.brighter());
        gbcLeft.gridy = 1;
        gbcLeft.insets = new Insets(10, 0, 10, 0); // Padding around separator
        leftP.add(separator, gbcLeft);

        // Subpanel: Añadir Jugador / Staff
        JPanel pMem = new JPanel(new GridBagLayout()); // Changed to GridBagLayout for pMem too
        Border titledMem = BorderFactory.createTitledBorder(new LineBorder(new Color(0, 120, 215), 3), "2. Añadir Jugador / Staff", TitledBorder.LEFT, TitledBorder.TOP, FONT_NORMAL, Color.WHITE);
        pMem.setBorder(new CompoundBorder(titledMem, new EmptyBorder(10, 10, 10, 10)));
        pMem.setBackground(new Color(30, 30, 30));
        pMem.setOpaque(true);

        GridBagConstraints gbcMem = new GridBagConstraints();
        gbcMem.insets = new Insets(5, 5, 5, 5);
        gbcMem.fill = GridBagConstraints.HORIZONTAL;
        gbcMem.weightx = 1.0;

        comboTeams = new JComboBox<>();
        comboTeams.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Team) setText(((Team) value).getName());
                setForeground(Color.WHITE);
                setBackground(isSelected ? COLOR_ACCENT : new Color(45, 48, 51));
                return this;
            }
        });
        comboTeams.setBackground(new Color(45, 48, 51));
        comboTeams.setForeground(Color.WHITE);

        comboTeams.addActionListener(e -> refreshMemberList()); // This should be after comboTeams is initialized

        // Panel de acciones para el equipo seleccionado (Modificar/Eliminar)
        JPanel teamActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        teamActionPanel.setOpaque(false);
        teamActionPanel.add(comboTeams);

        JButton btnEditTeam = new JButton("Modificar");
        styleButton(btnEditTeam, COLOR_PRIMARY);
        btnEditTeam.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnEditTeam.setToolTipText("Modificar nombre del equipo seleccionado");
        btnEditTeam.addActionListener(e -> loadSelectedTeam());
        
        JButton btnDelTeam = new JButton("Eliminar");
        styleButton(btnDelTeam, new Color(192, 57, 43));
        btnDelTeam.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnDelTeam.setToolTipText("Eliminar equipo seleccionado");
        btnDelTeam.addActionListener(e -> deleteTeam());

        teamActionPanel.add(btnEditTeam);
        teamActionPanel.add(btnDelTeam);

        txtMemName = new JTextField(20);
        txtMemName.setBackground(new Color(45, 48, 51));
        txtMemName.setForeground(Color.WHITE);
        txtMemName.putClientProperty("FlatLaf.placeholderText", "Nombre completo");

        JPanel photoFieldPanel = new JPanel(new BorderLayout());
        photoFieldPanel.setBackground(new Color(45, 48, 51));
        txtMemPhoto = new JTextField();
        txtMemPhoto.setBackground(new Color(45, 48, 51));
        txtMemPhoto.setForeground(Color.WHITE);
        txtMemPhoto.putClientProperty("FlatLaf.placeholderText", "URL o selecciona...");
        txtMemPhoto.setBorder(new EmptyBorder(5, 5, 5, 5));
        photoFieldPanel.add(txtMemPhoto, BorderLayout.CENTER);

        JButton btnBrowseMem = new JButton("...");
        btnBrowseMem.setPreferredSize(new Dimension(35, 30));
        btnBrowseMem.setBackground(COLOR_ACCENT);
        btnBrowseMem.setForeground(Color.WHITE);
        btnBrowseMem.putClientProperty("FlatLaf.style", "arc: 5;");
        btnBrowseMem.addActionListener(e -> showImageSelectionDialog(txtMemPhoto));
        photoFieldPanel.add(btnBrowseMem, BorderLayout.EAST);

        String[] roles = {"Jugador", "Entrenador", "Delegado", "Fisio"};
        comboRoles = new JComboBox<>(roles);
        comboRoles.setBackground(new Color(45, 48, 51));
        comboRoles.setForeground(Color.WHITE);

        JButton btnMem = new JButton("Guardar / Añadir");
        styleButton(btnMem, COLOR_ACCENT);
        btnMem.addActionListener(e -> {
            Team selectedTeam = (Team) comboTeams.getSelectedItem();
            if (selectedTeam != null) {
                String memberName = txtMemName.getText().trim();
                if (memberName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "El nombre del miembro no puede estar vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Member m = (editingMemberId != null) ? 
                    memberRepository.findById(editingMemberId).orElse(new Member()) : 
                    new Member();
                
                m.setName(memberName);
                m.setRole(comboRoles.getSelectedItem().toString());
                m.setPhotoUrl(txtMemPhoto.getText());
                m.setTeam(selectedTeam);

                memberRepository.save(m);
                refreshMemberList();
                resetMemberFields();
                JOptionPane.showMessageDialog(this, editingMemberId == null ? "Miembro añadido" : "Miembro actualizado");
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, selecciona un equipo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });

        int memRow = 0;
        JLabel lblSelEq = new JLabel("Seleccionar Equipo:"); lblSelEq.setForeground(Color.WHITE);
        pMem.add(lblSelEq, gbcMem(gbcMem, 0, memRow, GridBagConstraints.WEST));
        pMem.add(teamActionPanel, gbcMem(gbcMem, 1, memRow++, GridBagConstraints.EAST));

        JLabel lblNameMem = new JLabel("Nombre:"); lblNameMem.setForeground(Color.WHITE);
        pMem.add(lblNameMem, gbcMem(gbcMem, 0, memRow, GridBagConstraints.WEST));
        pMem.add(txtMemName, gbcMem(gbcMem, 1, memRow++, GridBagConstraints.EAST));

        JLabel lblRolMem = new JLabel("Rol:"); lblRolMem.setForeground(Color.WHITE);
        pMem.add(lblRolMem, gbcMem(gbcMem, 0, memRow, GridBagConstraints.WEST));
        pMem.add(comboRoles, gbcMem(gbcMem, 1, memRow++, GridBagConstraints.EAST));

        JLabel lblPhotoMem = new JLabel("URL Foto:"); lblPhotoMem.setForeground(Color.WHITE);
        pMem.add(lblPhotoMem, gbcMem(gbcMem, 0, memRow, GridBagConstraints.WEST));
        pMem.add(photoFieldPanel, gbcMem(gbcMem, 1, memRow++, GridBagConstraints.EAST));

        gbcMem.gridx = 0; gbcMem.gridy = memRow; gbcMem.gridwidth = 2; gbcMem.anchor = GridBagConstraints.EAST; // Align button to the right
        gbcMem.insets = new Insets(10, 5, 5, 5); // Top padding for button
        pMem.add(btnMem, gbcMem);

        gbcLeft.gridy = 2;
        gbcLeft.insets = new Insets(0, 0, 0, 0); // Reset insets
        leftP.add(pMem, gbcLeft);

        // Subpanel: Gestionar Miembros Existentes
        JPanel rightP = new JPanel(new BorderLayout(10, 10)); // Added gaps
        Border titledRight = BorderFactory.createTitledBorder(new LineBorder(new Color(0, 120, 215), 3), "3. Integrantes del equipo seleccionado", TitledBorder.LEFT, TitledBorder.TOP, FONT_NORMAL, Color.WHITE);
        rightP.setBorder(new CompoundBorder(titledRight, new EmptyBorder(10, 10, 10, 10)));
        rightP.setBackground(new Color(30, 30, 30));
        rightP.setOpaque(true);
        listMembers = new JList<>(memberModel);
        listMembers.setCellRenderer(new MemberListRenderer()); // Renderizador visual
        listMembers.setFixedCellHeight(60); // Give more space for member cards
        listMembers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only one member can be selected

        JButton btnEditMem = new JButton("✏️ Modificar");
        styleButton(btnEditMem, COLOR_ACCENT);
        btnEditMem.addActionListener(e -> loadSelectedMember());

        JButton btnDeleteMem = new JButton("🗑️ Eliminar");
        styleButton(btnDeleteMem, new Color(192, 57, 43));
        btnDeleteMem.addActionListener(e -> deleteMember());

        JPanel memberActions = new JPanel(new GridLayout(1, 2, 10, 0));
        memberActions.setBackground(new Color(30, 30, 30));
        memberActions.add(btnEditMem);
        memberActions.add(btnDeleteMem);

        rightP.add(new JScrollPane(listMembers), BorderLayout.CENTER);
        rightP.add(memberActions, BorderLayout.SOUTH);

        mainP.add(leftP, BorderLayout.WEST);
        mainP.add(rightP, BorderLayout.CENTER);
        return mainP;
    }
    
    // Helper method for GridBagConstraints in pMem
    private GridBagConstraints gbcMem(GridBagConstraints gbc, int gridx, int gridy, int anchor) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = 1;
        gbc.anchor = anchor;
        gbc.weightx = (gridx == 0) ? 0.0 : 1.0; // Labels don't expand, fields do
        return gbc;
    }

    @jakarta.annotation.PostConstruct
    public void loadInitialData() {
        if (currentUser == null || currentUser.getClub() == null) return;
        
        ClubInfo info = currentUser.getClub();
        if(txtClubName != null) txtClubName.setText(info.getName());
        if(txtLogo != null) txtLogo.setText(info.getLogoUrl());
        if(txtBackgroundImg != null) txtBackgroundImg.setText(info.getBackgroundImageUrl());
        if(txtFB != null) txtFB.setText(info.getFacebook());
        if(txtIG != null) txtIG.setText(info.getInstagram());
        if(txtTW != null) txtTW.setText(info.getTwitter());
        if(txtHeaderBg != null) txtHeaderBg.setText(info.getHeaderBackgroundColor());
        if(txtHeaderTxt != null) txtHeaderTxt.setText(info.getHeaderTextColor());
        if(txtYoutubeLive != null) txtYoutubeLive.setText(info.getYoutubeLiveUrl());

        refreshTeamCombo();
        refreshPostCombo();
        refreshImageGallery();
    }

    private void refreshTeamCombo() {
        comboTeams.removeAllItems();
        List<Team> teams = teamRepository.findAll();
        for (Team t : teams) comboTeams.addItem(t);
    }

    private void refreshPostCombo() {
        comboPosts.removeAllItems();
        postRepository.findAll().forEach(comboPosts::addItem);
    }

    private void refreshMemberList() {
        memberModel.clear();
        Team selected = (Team) comboTeams.getSelectedItem();
        if (selected != null && selected.getId() != null) {
            try {
                memberRepository.findByTeam_Id(selected.getId()).forEach(memberModel::addElement);
            } catch (Exception e) {
                System.err.println("Error al refrescar lista de miembros: " + e.getMessage());
            }
        }
    }

    private void loadSelectedPost() {
        Post p = (Post) comboPosts.getSelectedItem();
        if (p != null) {
            editingPostId = p.getId();
            txtPostTitle.setText(p.getTitle());
            txtPostContent.setText(p.getContent());
        }
    }

    private void loadSelectedTeam() {
        Team t = (Team) comboTeams.getSelectedItem();
        if (t != null) {
            editingTeamId = t.getId();
            txtTeamName.setText(t.getName());
            btnTeamSave.setText("Guardar Cambios");
            txtTeamName.requestFocus();
        }
    }

    private void deleteTeam() {
        Team t = (Team) comboTeams.getSelectedItem();
        if (t != null) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Estás seguro de eliminar el equipo '" + t.getName() + "'?\nEsto eliminará también a todos sus integrantes.", 
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                teamRepository.delete(t);
                refreshTeamCombo();
                resetTeamFields();
                memberModel.clear();
                JOptionPane.showMessageDialog(this, "Equipo eliminado correctamente.");
            }
        }
    }

    private void loadSelectedMember() {
        Member m = listMembers.getSelectedValue();
        if (m != null) {
            editingMemberId = m.getId();
            txtMemName.setText(m.getName());
            txtMemPhoto.setText(m.getPhotoUrl());
            comboRoles.setSelectedItem(m.getRole());
            comboTeams.setSelectedItem(m.getTeam());
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un integrante de la lista para modificar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void resetMemberFields() {
        editingMemberId = null;
        txtMemName.setText("");
        txtMemPhoto.setText("");
        comboRoles.setSelectedIndex(0);
    }

    private void resetTeamFields() {
        editingTeamId = null;
        txtTeamName.setText("");
        btnTeamSave.setText("Crear Equipo");
    }

    private void resetPostFields() {
        editingPostId = null;
        txtPostTitle.setText("");
        txtPostContent.setText("");
    }

    private void deletePost() {
        if (editingPostId != null) {
            postRepository.deleteById(editingPostId);
            resetPostFields();
            refreshPostCombo(); 
            JOptionPane.showMessageDialog(this, "Noticia eliminada");
        }
    }

    private void deleteMember() {
        Member m = listMembers.getSelectedValue();
        if (m != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que quieres eliminar a " + m.getName() + "?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                memberRepository.delete(m);
                refreshMemberList();
                JOptionPane.showMessageDialog(this, "Integrante eliminado.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un integrante para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void pickColor(JTextField target) {
        String current = target.getText();
        // Validamos que el texto sea un color hexadecimal válido antes de decodificar
        Color initialColor = (current != null && current.startsWith("#") && current.length() == 7) ? Color.decode(current) : Color.WHITE; // Added length check
        Color c = JColorChooser.showDialog(this, "Selecciona un color", initialColor);
        if (c != null) {
            target.setText(String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
        }
    }

    // Modal dialog for image selection
    private void showImageSelectionDialog(JTextField targetField) {
        JDialog dialog = new JDialog(this, "Seleccionar Imagen", true); // Modal dialog
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(COLOR_BG);

        JList<com.example.pagebuilder.Image> selectorList = new JList<>(imageModel); 
        selectorList.setCellRenderer(new ImageCellRenderer());
        selectorList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        selectorList.setVisibleRowCount(-1);
        selectorList.setFixedCellWidth(120);
        selectorList.setFixedCellHeight(120);
        JScrollPane scrollPane = new JScrollPane(selectorList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JButton btnSelect = new JButton("Seleccionar");
        styleButton(btnSelect, COLOR_ACCENT);
        btnSelect.addActionListener(e -> {
            com.example.pagebuilder.Image selected = selectorList.getSelectedValue();
            if (selected != null) {
                targetField.setText(selected.getUrl());
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Por favor, selecciona una imagen.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnCancel = new JButton("Cancelar");
        styleButton(btnCancel, new Color(192, 57, 43));
        btnCancel.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(COLOR_BG);
            buttonPanel.add(btnSelect);
            buttonPanel.add(btnCancel);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }


    private void saveClub() {
        if (currentUser == null) return;
        ClubInfo info = currentUser.getClub();
        info.setName(txtClubName.getText());
        info.setLogoUrl(txtLogo.getText());
        info.setFacebook(txtFB.getText());
        info.setInstagram(txtIG.getText());
        info.setTwitter(txtTW.getText());
        info.setHeaderBackgroundColor(txtHeaderBg.getText());
        info.setHeaderTextColor(txtHeaderTxt.getText());
        info.setBackgroundImageUrl(txtBackgroundImg.getText());
        info.setYoutubeLiveUrl(txtYoutubeLive.getText());
        clubRepository.save(info);
        JOptionPane.showMessageDialog(this, "Configuración actualizada");
    }

    private void loadAppLogo() {
        try {
            URL resource = getClass().getResource(LOGO_PATH);
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                lblSidebarLogo.setIcon(new ImageIcon(icon.getImage().getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH)));
            } else {
                lblSidebarLogo.setText("👤");
                lblSidebarLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
                lblSidebarLogo.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            lblSidebarLogo.setText("?");
        }
    }

    // Clase para mostrar los miembros como "Tarjetas" en la lista de Swing
    class MemberListRenderer extends JPanel implements ListCellRenderer<Member> {
        private JLabel lblPhoto;
        private JLabel lblInfo;

        public MemberListRenderer() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(5, 5, 5, 5));
            
            lblPhoto = new JLabel();
            lblPhoto.setPreferredSize(new Dimension(50, 50));
            lblPhoto.setHorizontalAlignment(SwingConstants.CENTER);
            lblPhoto.setVerticalAlignment(SwingConstants.CENTER);
            add(lblPhoto, BorderLayout.WEST);

            lblInfo = new JLabel();
            add(lblInfo, BorderLayout.CENTER);
        }

        @Override
        public java.awt.Component getListCellRendererComponent(JList<? extends Member> list, Member value, int index, boolean isSelected, boolean cellHasFocus) {
            Member m = (Member) value;
            
            lblInfo.setText("<html><b>" + m.getName() + "</b><br>" + m.getRole() + "</html>");
            lblInfo.setForeground(Color.WHITE);
            
            setBackground(isSelected ? COLOR_ACCENT : new Color(45, 45, 45));
            setBorder(BorderFactory.createLineBorder(isSelected ? COLOR_PRIMARY : Color.DARK_GRAY, 1));

            // Load member photo if available
            if (m.getPhotoUrl() != null && !m.getPhotoUrl().isEmpty()) {
                updatePreviewImage(m.getPhotoUrl(), lblPhoto);
            } else {
                lblPhoto.setIcon(null);
                lblPhoto.setText("👤"); // Default icon
                lblPhoto.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
                lblPhoto.setForeground(Color.WHITE);
            }

            return this;
        }
    }

    // Custom renderer for Image JList
    class ImageCellRenderer extends JPanel implements ListCellRenderer<com.example.pagebuilder.Image> {
        private JLabel imageLabel;
        private JLabel nameLabel;

        public ImageCellRenderer() {
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            setBackground(new Color(40, 40, 40));

            imageLabel = new JLabel();
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setPreferredSize(new Dimension(100, 100));
            add(imageLabel, BorderLayout.CENTER);

            nameLabel = new JLabel();
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            nameLabel.setForeground(Color.LIGHT_GRAY);
            add(nameLabel, BorderLayout.SOUTH);
        }

        @Override
        public java.awt.Component getListCellRendererComponent(JList<? extends com.example.pagebuilder.Image> list, com.example.pagebuilder.Image value, int index, boolean isSelected, boolean cellHasFocus) {
            nameLabel.setText(value.getFilename());
            
            // Load image asynchronously
            new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                java.nio.file.Path imagePath = java.nio.file.Paths.get(STORAGE_PATH, UPLOAD_DIR, value.getUrl().replaceFirst("/" + UPLOAD_DIR + "/", ""));
                    if (Files.exists(imagePath)) {
                        try {
                            BufferedImage img = javax.imageio.ImageIO.read(imagePath.toFile());
                            if (img != null) {
                                return new ImageIcon(img.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH));
                            }
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
                            imageLabel.setIcon(icon);
                            imageLabel.setText("");
                        } else {
                            imageLabel.setIcon(null);
                            imageLabel.setText("Error");
                        }
                    } catch (Exception ignored) {
                        imageLabel.setIcon(null);
                        imageLabel.setText("Error");
                    }
                }
            }.execute();

            if (isSelected) {
                setBackground(COLOR_ACCENT);
                nameLabel.setForeground(Color.WHITE);
            } else {
                setBackground(new Color(40, 40, 40));
                nameLabel.setForeground(Color.LIGHT_GRAY);
            }
            setBorder(BorderFactory.createLineBorder(isSelected ? COLOR_PRIMARY : Color.LIGHT_GRAY, 2));
            return this;
        }
    }
// Clase auxiliar para bordes punteados (Placeholders)
class DashedBorder extends AbstractBorder {
    private Color color;
    public DashedBorder(Color color) { this.color = color; }
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