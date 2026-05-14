package com.example.pagebuilder;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "club_informacion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder // Lombok para getters, setters, constructores y builder
public class ClubInformacion { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 
    private String nombre; 
    @JsonProperty("urlLogo")
    private String urlLogo;
    private String foto1; 
    private String foto2; 
    private String facebook;
    private String instagram; 
    private String twitter; 
    private String colorFondoEncabezado; 
    private String colorTextoEncabezado; 
    private String urlImagenFondo; 
    @JsonProperty("urlYoutubeDirecto")
    private String urlYoutubeDirecto; 

    @OneToOne(mappedBy = "club")
    @ToString.Exclude
    @JsonIgnore
    private Usuario usuario; 
    
    @JsonIgnore
    public String getName() { return nombre; }
    @JsonIgnore
    public String getLogoUrl() { return urlLogo; }
    @JsonIgnore
    public String getPhoto1() { return foto1; }
    @JsonIgnore
    public String getPhoto2() { return foto2; }
    @JsonIgnore
    public String getHeaderBackgroundColor() { return colorFondoEncabezado; }
    @JsonIgnore
    public String getHeaderTextColor() { return colorTextoEncabezado; }
    @JsonIgnore
    public String getBackgroundImageUrl() { return urlImagenFondo; }
    @JsonIgnore
    public String getYoutubeLiveUrl() { return urlYoutubeDirecto; }

    public void setName(String name) { this.nombre = name; }
    public void setLogoUrl(String logoUrl) { this.urlLogo = logoUrl; }
    public void setHeaderBackgroundColor(String color) { this.colorFondoEncabezado = color; }
    public void setHeaderTextColor(String color) { this.colorTextoEncabezado = color; }
    public void setBackgroundImageUrl(String url) { this.urlImagenFondo = url; }
    public void setYoutubeLiveUrl(String url) { this.urlYoutubeDirecto = url; }
}