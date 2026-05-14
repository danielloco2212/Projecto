package bdd;

import com.example.pagebuilder.InterfazAcceso; // Renombrado
import com.example.pagebuilder.InterfazAdmin; // Renombrado
import com.example.pagebuilder.Usuario; // Renombrado
import com.example.pagebuilder.UsuarioRepositorio; // Renombrado
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.awt.*;
import javax.swing.JOptionPane;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class PasosAccesoAdmin { // Renombrado
    @Autowired
    private InterfazAcceso interfazAcceso; // Renombrado
    @Autowired
    private InterfazAdmin interfazAdmin; // Renombrado
    @Autowired
    private UsuarioRepositorio usuarioRepositorio; // Renombrado
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Given("que el administrador está en la pantalla de acceso") // Renombrado
    public void elAdminEstaEnPantallaAcceso() { // Renombrado
        usuarioRepositorio.deleteAll(); // Renombrado
        usuarioRepositorio.save(new Usuario(null, "admin", passwordEncoder.encode("password"), null)); // Renombrado
        EventQueue.invokeLater(() -> interfazAcceso.setVisible(true)); // Renombrado
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } // Pequeña pausa para que la UI se cargue
    }
    @When("ingresa {string} como nombre de usuario y {string} como contraseña") // Renombrado
    public void ingresaCredenciales(String nombreUsuario, String contrasena) { // Renombrado
        interfazAcceso.getCampoUsuario().setText(nombreUsuario); // Renombrado
        interfazAcceso.getCampoContrasena().setText(contrasena); // Renombrado
    }
    @When("hace clic en el botón de iniciar sesión") // Renombrado
    public void haceClicEnBotonIniciarSesion() { // Renombrado
        interfazAcceso.getBotonAcceder().doClick(); // Renombrado
    }
    @Then("debería ver la ventana principal de administración") // Renombrado
    public void deberiaVerVentanaAdmin() { // Renombrado
        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } // Pequeña pausa para que la UI se cargue
        assertFalse(interfazAcceso.isVisible()); // Renombrado
        assertTrue(interfazAdmin.isVisible()); // Renombrado
        interfazAdmin.dispose(); // Renombrado
    }
}