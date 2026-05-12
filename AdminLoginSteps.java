package bdd;

import com.example.pagebuilder.LoginUI;
import com.example.pagebuilder.SwingUI;
import com.example.pagebuilder.User;
import com.example.pagebuilder.UserRepository;
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
public class AdminLoginSteps {
    @Autowired
    private LoginUI loginUI;
    @Autowired
    private SwingUI swingUI;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Given("que el administrador está en la pantalla de login")
    public void adminIsInLoginPage() {
        userRepository.deleteAll();
        userRepository.save(new User(null, "admin", passwordEncoder.encode("password")));
        EventQueue.invokeLater(() -> loginUI.setVisible(true));
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
    @When("ingresa {string} como usuario y {string} como contraseña")
    public void entersCredentials(String username, String password) {
        loginUI.getUsernameField().setText(username);
        loginUI.getPasswordField().setText(password);
    }
    @When("hace clic en el botón {string}")
    public void clicksButton(String buttonText) {
        loginUI.getLoginButton().doClick();
    }
    @Then("debería ver la ventana principal de administración")
    public void shouldSeeAdminWindow() {
        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertFalse(loginUI.isVisible());
        assertTrue(swingUI.isVisible());
        swingUI.dispose();
    }
}