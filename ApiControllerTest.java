package com.example.pagebuilder;

import com.example.pagebuilder.ClubInfo;
import com.example.pagebuilder.ClubRepository;
import com.example.pagebuilder.Post;
import com.example.pagebuilder.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private PostRepository postRepository;
    @BeforeEach
    void setUp() {
        clubRepository.deleteAll();
        postRepository.deleteAll();
        ClubInfo clubInfo = new ClubInfo();
        clubInfo.setName("Test Club");
        clubInfo.setLogoUrl("http://test.com/logo.png");
        clubRepository.save(clubInfo);
        Post post1 = new Post();
        post1.setTitle("Noticia de Prueba 1");
        post1.setContent("Contenido de la noticia 1");
        postRepository.save(post1);
        Post post2 = new Post();
        post2.setTitle("Noticia de Prueba 2");
        post2.setContent("Contenido de la noticia 2");
        postRepository.save(post2);
    }
    @Test
    void testGetClubInfo() throws Exception {
        mockMvc.perform(get("/api/club")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Club"))
                .andExpect(jsonPath("$.logoUrl").value("http://test.com/logo.png"));
    }
    @Test
    void testGetPosts() throws Exception {
        mockMvc.perform(get("/api/posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Noticia de Prueba 1"))
                .andExpect(jsonPath("$[1].title").value("Noticia de Prueba 2"));
    }
}