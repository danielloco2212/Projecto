package com.example.pagebuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

@Component
public class RegisterUI extends JFrame {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;

    private final Color COLOR_PRIMARY = new Color(44, 62, 80);
    private final Color COLOR_ACCENT = new Color(52, 152, 219); // Azul para registro
    private final Color COLOR_BG = new Color(236, 240, 241);

    public RegisterUI() {
        setTitle("Registro de Administrador");
        setSize(400, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        initUI();
    }

    private void initUI() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(200, 200, 200), 1, true), new EmptyBorder(30, 30, 30, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        JLabel lblTitle = new JLabel("Nuevo Usuario", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridy = 0; card.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        txtConfirmPassword = new JPasswordField(15);

        gbc.gridy = 1; gbc.gridx = 0; card.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; card.add(txtUsername, gbc);

        gbc.gridy = 2; gbc.gridx = 0; card.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; card.add(txtPassword, gbc);

        gbc.gridy = 3; gbc.gridx = 0; card.add(new JLabel("Confirmar:"), gbc);
        gbc.gridx = 1; card.add(txtConfirmPassword, gbc);

        JButton btnRegister = new JButton("Registrar");
        styleButton(btnRegister, COLOR_ACCENT);
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        card.add(btnRegister, gbc);

        btnRegister.addActionListener(e -> performRegister());

        setLayout(new GridBagLayout());
        add(card);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
    }

    private void performRegister() {
        String user = txtUsername.getText();
        String pass = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos");
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (userRepository.findByUsername(user).isPresent()) {
            JOptionPane.showMessageDialog(this, "El usuario ya existe");
            return;
        }

        // Crear Usuario y su Club automáticamente
        User newUser = new User();
        newUser.setUsername(user);
        newUser.setPassword(passwordEncoder.encode(pass));
        
        ClubInfo defaultClub = new ClubInfo();
        defaultClub.setName("Club de " + user);
        newUser.setClub(defaultClub);

        userRepository.save(newUser);
        JOptionPane.showMessageDialog(this, "Registro completado con éxito");
        this.dispose();
    }
}