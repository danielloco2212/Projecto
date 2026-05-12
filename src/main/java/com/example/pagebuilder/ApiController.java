package com.example.pagebuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    @Autowired private ClubRepository clubRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ImageRepository imageRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- CLUB INFO ---
    @GetMapping("/club")
    public ResponseEntity<ClubInfo> getClubInfo() {
        return ResponseEntity.of(clubRepository.findAll().stream().findFirst());
    }

    @PostMapping("/club")
    public ClubInfo saveClubInfo(@RequestBody ClubInfo info) {
        return clubRepository.save(info);
    }

    // --- NOTICIAS (POSTS) ---
    @GetMapping("/posts")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @PostMapping("/posts")
    public Post createPost(@RequestBody Post post) {
        return postRepository.save(post);
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post post) {
        return postRepository.findById(id).map(existing -> {
            existing.setTitle(post.getTitle());
            existing.setContent(post.getContent());
            existing.setImageUrl(post.getImageUrl());
            return ResponseEntity.ok(postRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ✅ AÑADIDO: Eliminar noticia
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (!postRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        postRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- EQUIPOS (TEAMS) ---
    @GetMapping("/teams")
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @PostMapping("/teams")
    public Team createTeam(@RequestBody Team team) {
        return teamRepository.save(team);
    }

    // ✅ AÑADIDO: Eliminar equipo
    @DeleteMapping("/teams/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        if (!teamRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        teamRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- MIEMBROS ---
    @GetMapping("/members")
    public List<Member> getMembers(@RequestParam(required = false) Long teamId) {
        if (teamId != null) {
            return memberRepository.findByTeam_Id(teamId);
        }
        return memberRepository.findAll();
    }

    @PostMapping("/members")
    public ResponseEntity<Member> createMember(@RequestBody Member member) {
        if (member.getTeamId() != null) {
            teamRepository.findById(member.getTeamId()).ifPresent(member::setTeam);
        }
        return ResponseEntity.ok(memberRepository.save(member));
    }

    @PutMapping("/members/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member member) {
        return memberRepository.findById(id).map(existing -> {
            existing.setName(member.getName());
            existing.setRole(member.getRole());
            existing.setPhotoUrl(member.getPhotoUrl());
            if (member.getTeamId() != null) {
                teamRepository.findById(member.getTeamId()).ifPresent(existing::setTeam);
            }
            return ResponseEntity.ok(memberRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ✅ AÑADIDO: Eliminar miembro
    @DeleteMapping("/members/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        if (!memberRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        memberRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- GALERÍA ---
    @GetMapping("/gallery")
    public List<Image> getImages() {
        return imageRepository.findAll();
    }

    @PostMapping("/gallery")
    public Image uploadImage(@RequestBody Image image) {
        return imageRepository.save(image);
    }

    // ✅ AÑADIDO: Eliminar imagen
    @DeleteMapping("/gallery/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        if (!imageRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        imageRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- USUARIOS ---
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(409).build();
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        return userRepository.findByUsername(user.getUsername())
                .filter(u -> passwordEncoder.matches(user.getPassword(), u.getPassword()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }
}