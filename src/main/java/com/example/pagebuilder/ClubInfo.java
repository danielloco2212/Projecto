package com.example.pagebuilder;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ClubInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String logoUrl;
    private String photo1;
    private String photo2;
    private String facebook;
    private String instagram;
    private String twitter;
    private String headerBackgroundColor;
    private String headerTextColor;
    private String backgroundImageUrl;
    private String youtubeLiveUrl;

    @OneToOne(mappedBy = "club")
    @ToString.Exclude
    private User user;
}