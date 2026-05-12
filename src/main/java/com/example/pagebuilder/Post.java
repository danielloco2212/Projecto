package com.example.pagebuilder;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;
    private String imageUrl;
}