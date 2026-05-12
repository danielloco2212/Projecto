package com.example.pagebuilder;

import com.formdev.flatlaf.FlatDarkLaf;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.io.File;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.net.URL;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // Forzar que la base de datos se guarde en la carpeta del usuario para evitar errores de permisos
        String storagePath = System.getProperty("user.home") + File.separator + ".adminbaloncesto";
        
        File storageDir = new File(storagePath);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        // Asegurar que Java detecte el entorno gráfico correctamente
        System.setProperty("java.awt.headless", "false");

        // Detectamos si el entorno permite gráficos (Desktop vs Servidor)
        boolean isHeadless = GraphicsEnvironment.isHeadless();

        ConfigurableApplicationContext context = null;
        try {
            context = new SpringApplicationBuilder(Application.class)
                    .headless(false)
                    // Evita que la app se cierre si no hay consola
                    .logStartupInfo(true)
                    .run(args);
        } catch (Throwable t) {
            if (!isHeadless) {
                JOptionPane.showMessageDialog(null, 
                    "Error fatal al iniciar el servidor:\n" + t.toString(), 
                    "Error de Inicio", 
                    JOptionPane.ERROR_MESSAGE);
            } else {
                // Si estamos en consola, mostramos el error detallado
                System.err.println("=== ERROR CRÍTICO EN EL ARRANQUE ===");
                t.printStackTrace();
            }
            System.exit(1);
        }

        // Solo iniciamos Swing si no estamos en modo headless (entorno local)
        if (!isHeadless && context != null) {
            final ConfigurableApplicationContext appContext = context;
            EventQueue.invokeLater(() -> {
                FlatDarkLaf.setup();
                
                UIManager.put("Component.arc", 12);
                UIManager.put("TextComponent.arc", 8);
                UIManager.put("Button.arc", 10);

                LoginUI ui = appContext.getBean(LoginUI.class);
                // Intentar poner el logo como icono de la ventana principal
                URL imgUrl = Application.class.getResource("/static/logo.png");
                if (imgUrl != null) {
                    ui.setIconImage(new ImageIcon(imgUrl).getImage());
                }
                ui.setVisible(true);
            });
        }
    }
}