package com.example.pagebuilder;

import com.example.pagebuilder.Application;
import com.example.pagebuilder.ClubInfo;
import com.example.pagebuilder.ClubRepository;
import com.example.pagebuilder.Post;
import com.example.pagebuilder.PostRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class WebUITest {
    @LocalServerPort
    private int port;
    private WebDriver driver;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private PostRepository postRepository;
    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }
    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        clubRepository.deleteAll();
        postRepository.deleteAll();
        clubRepository.save(new ClubInfo(null, "Club de Prueba", null, null, null, null, null, null, null, null, null, null));
        postRepository.save(new Post(null, "Noticia Web", "Contenido de la noticia web", null));
    }
    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }
    @Test
    void testHomePageLoadsAndShowsClubName() {
        driver.get("http://localhost:" + port + "/");
        assertTrue(driver.getTitle().contains("Club de Prueba"));
        assertTrue(driver.getPageSource().contains("Noticia Web"));
    }
}