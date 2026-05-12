package com.example.pagebuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import javax.swing.*;
import java.io.File;
import javax.swing.border.*;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

@Component
public class LoginUI extends JFrame {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    @Lazy
    private SwingUI swingUI;
    @Autowired
    private RegisterUI registerUI;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblLogo;

    private final Color COLOR_PRIMARY = new Color(44, 62, 80);
    private final Color COLOR_ACCENT = new Color(52, 152, 219);
    private final Color COLOR_BG = new Color(236, 240, 241);
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final String LOGO_PATH = "/static/logo.png";

    public LoginUI() {
        setTitle("Acceso al Panel - Club Baloncesto");
        setSize(400, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla
        getContentPane().setBackground(COLOR_BG);
        add(createLoginPanel());
    }

    private JPanel createLoginPanel() {
        JPanel mainP = new JPanel(new GridBagLayout());
        mainP.setBackground(COLOR_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200), 1, true), new EmptyBorder(30, 30, 30, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo de la Empresa
        lblLogo = new JLabel("", SwingConstants.CENTER);
        lblLogo.setPreferredSize(new Dimension(150, 150));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 15, 0);
        card.add(lblLogo, gbc);
        loadAppLogo(); // Cargar tu logo corporativo

        JLabel lblTitle = new JLabel("Acceso Administración", SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 20, 0);
        card.add(lblTitle, gbc);

        gbc.insets = new Insets(8, 8, 8, 8); // Restaurar espacios para los campos
        gbc.gridwidth = 1;
        
        txtUsername = new JTextField(15);
        txtUsername.putClientProperty("FlatLaf.placeholderText", "Usuario");
        
        txtPassword = new JPasswordField(15);
        txtPassword.putClientProperty("FlatLaf.placeholderText", "Contraseña");

        JButton btnLogin = new JButton("Iniciar Sesión");
        styleButton(btnLogin, COLOR_ACCENT);
        JButton btnRegister = new JButton("Registrar");
        styleButton(btnRegister, COLOR_PRIMARY);

        gbc.gridx = 0; gbc.gridy = 2; card.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; card.add(txtUsername, gbc);
        gbc.gridx = 0; gbc.gridy = 3; card.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; card.add(txtPassword, gbc);
        gbc.gridx = 0; gbc.gridy = 4; card.add(btnLogin, gbc);
        gbc.gridx = 1; card.add(btnRegister, gbc);

        btnLogin.addActionListener(e -> performLogin());
        btnRegister.addActionListener(e -> performRegister());

        mainP.add(card);
        return mainP;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    private void loadAppLogo() {
        try {
            URL imgUrl = getClass().getResource(LOGO_PATH);
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(imgUrl);
                lblLogo.setIcon(new ImageIcon(icon.getImage().getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH)));
            } else {
                lblLogo.setText("👤");
                lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
            }
        } catch (Exception e) {
            lblLogo.setText("?");
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        // FlatLaf specific styling for rounded corners and hover
        btn.putClientProperty("FlatLaf.style", "arc: 10;"); // Slightly less rounded than main UI
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.hoverBackground", new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 200)); // Slightly darker on hover
    }

    private void performLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        
        userRepository.findByUsername(username) // Busca el usuario por nombre
                .filter(u -> passwordEncoder.matches(password, u.getPassword())) // Compara la contraseña encriptada
                .ifPresentOrElse(u -> {
                    swingUI.setCurrentUser(u); // Pasar el usuario a la UI principal
                    this.dispose(); 
                    swingUI.loadInitialData(); 
                    swingUI.setVisible(true); // Muestra la interfaz principal
                }, () -> JOptionPane.showMessageDialog(this, "Credenciales incorrectas", "Error", JOptionPane.ERROR_MESSAGE));
    }

    private void performRegister() {
        registerUI.setVisible(true);
    }
}