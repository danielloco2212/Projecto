package com.example.pagebuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

@Component
public class InterfazRegistro extends JFrame { // Renombrado

    @Autowired
    private UsuarioRepositorio usuarioRepositorio; // Renombrado
    @Autowired
    private PasswordEncoder passwordEncoder;

    private JTextField campoUsuario; // Renombrado
    private JPasswordField campoContrasena; // Renombrado
    private JPasswordField campoConfirmarContrasena; // Renombrado

    private final Color COLOR_PRIMARY = new Color(44, 62, 80);
    private final Color COLOR_ACCENT = new Color(52, 152, 219); // Azul para registro
    private final Color COLOR_BG = new Color(236, 240, 241);

    public InterfazRegistro() {
        setTitle("Registro de Administrador");
        setSize(400, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG); // Establece el color de fondo del panel de contenido
        inicializarUI(); // Renombrado
    }

    private void inicializarUI() {
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
        campoUsuario = new JTextField(15); // Renombrado
        campoContrasena = new JPasswordField(15); // Renombrado
        campoConfirmarContrasena = new JPasswordField(15); // Renombrado

        gbc.gridy = 1; gbc.gridx = 0; card.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; card.add(campoUsuario, gbc); // Renombrado

        gbc.gridy = 2; gbc.gridx = 0; card.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; card.add(campoContrasena, gbc); // Renombrado

        gbc.gridy = 3; gbc.gridx = 0; card.add(new JLabel("Confirmar:"), gbc);
        gbc.gridx = 1; card.add(campoConfirmarContrasena, gbc); // Renombrado

        JButton botonRegistrar = new JButton("Registrar"); // Renombrado
        estilizarBoton(botonRegistrar, COLOR_ACCENT); // Renombrado
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        card.add(botonRegistrar, gbc); // Renombrado

        botonRegistrar.addActionListener(e -> realizarRegistro()); // Renombrado

        setLayout(new GridBagLayout());
        add(card);
    }

    private void estilizarBoton(JButton boton, Color colorFondo) { // Renombrado
        boton.setBackground(colorFondo);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(10, 15, 10, 15));
    }

    private void realizarRegistro() { // Renombrado
        String usuario = campoUsuario.getText(); // Renombrado
        String contrasena = new String(campoContrasena.getPassword()); // Renombrado
        String confirmarContrasena = new String(campoConfirmarContrasena.getPassword()); // Renombrado

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos");
            return;
        }
        if (!contrasena.equals(confirmarContrasena)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (usuarioRepositorio.findByUsername(usuario).isPresent()) { // Renombrado
            JOptionPane.showMessageDialog(this, "El usuario ya existe");
            return;
        }

        // Crear Usuario y su Club automáticamente
        Usuario nuevoUsuario = new Usuario(); // Renombrado
        nuevoUsuario.setUsername(usuario);
        nuevoUsuario.setPassword(passwordEncoder.encode(contrasena));
        
        ClubInformacion clubPorDefecto = new ClubInformacion(); // Renombrado
        clubPorDefecto.setNombre("Club de " + usuario); // Renombrado
        nuevoUsuario.setClub(clubPorDefecto); // Renombrado

        usuarioRepositorio.save(nuevoUsuario); // Renombrado
        JOptionPane.showMessageDialog(this, "Registro completado con éxito");
        this.dispose();
    }
}