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
        File storageDir = new File(System.getProperty("user.home") + File.separator + ".PortalBasket");
        if (!storageDir.exists()) storageDir.mkdirs();
 
        System.setProperty("java.awt.headless", "false");
        boolean isHeadless = GraphicsEnvironment.isHeadless();
 
        ConfigurableApplicationContext contexto = arrancarServidor(args, isHeadless);
        if (contexto == null) return;
 
        if (!isHeadless) {
            final ConfigurableApplicationContext ctx = contexto;
            EventQueue.invokeLater(() -> iniciarInterfaz(ctx));
        }
    }
 
    private static ConfigurableApplicationContext arrancarServidor(String[] args, boolean isHeadless) {
        try {
            return new SpringApplicationBuilder(Application.class)
                    .headless(false)
                    .logStartupInfo(true)
                    .run(args);
        } catch (Throwable t) {
            if (!isHeadless) {
                JOptionPane.showMessageDialog(null,
                    "Error fatal al iniciar el servidor:\n" + t,
                    "Error de Inicio", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("=== ERROR CRÍTICO EN EL ARRANQUE ===");
                t.printStackTrace();
            }
            System.exit(1);
            return null;
        }
    }
 
    private static void iniciarInterfaz(ConfigurableApplicationContext ctx) {
        FlatDarkLaf.setup();
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Button.arc", 10);
 
        InterfazAcceso ui = ctx.getBean(InterfazAcceso.class);
        URL imgUrl = Application.class.getResource("/static/logo.png");
        if (imgUrl != null) ui.setIconImage(new ImageIcon(imgUrl).getImage());
        ui.setVisible(true);
    }
}