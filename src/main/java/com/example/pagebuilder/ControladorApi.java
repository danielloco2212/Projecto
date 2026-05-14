package com.example.pagebuilder;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.util.List;
 
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ControladorApi {
 
    @Autowired private ClubRepositorio clubRepositorio;
    @Autowired private EquipoRepositorio equipoRepositorio;
    @Autowired private NoticiaRepositorio noticiaRepositorio;
    @Autowired private MiembroRepositorio miembroRepositorio;
    @Autowired private ImagenRepositorio imagenRepositorio;
    @Autowired private UsuarioRepositorio usuarioRepositorio;
    @Autowired private PasswordEncoder passwordEncoder;
 
    @GetMapping("/club")
    public ResponseEntity<ClubInformacion> obtenerInfoClub() {
        var todos = clubRepositorio.findAll();
        return ResponseEntity.of(todos.stream().reduce((a, b) -> b));
    }
 
    @PostMapping("/club")
    public ClubInformacion guardarInfoClub(@RequestBody ClubInformacion info) {
        return clubRepositorio.save(info);
    }
 
    @GetMapping("/posts")
    public List<Noticia> obtenerTodasNoticias() {
        var orden = Sort.by(Sort.Direction.DESC, "id");
        return noticiaRepositorio.findAll(orden);
    }
 
    @GetMapping("/posts/{id}")
    public ResponseEntity<Noticia> obtenerNoticiaPorId(@PathVariable Long id) {
        return noticiaRepositorio.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    @PostMapping("/posts")
    public Noticia crearNoticia(@RequestBody Noticia datos) {
        return noticiaRepositorio.save(datos);
    }
 
    @PutMapping("/posts/{id}")
    public ResponseEntity<Noticia> actualizarNoticia(
            @PathVariable Long id, @RequestBody Noticia datos) {
        return noticiaRepositorio.findById(id).map(n -> {
            n.setTitulo(datos.getTitulo());
            n.setContenido(datos.getContenido());
            n.setUrlImagen(datos.getUrlImagen());
            return ResponseEntity.ok(noticiaRepositorio.save(n));
        }).orElse(ResponseEntity.notFound().build());
    }
 
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> eliminarNoticia(@PathVariable Long id) {
        if (!noticiaRepositorio.existsById(id))
            return ResponseEntity.notFound().build();
        noticiaRepositorio.deleteById(id);
        return ResponseEntity.ok().build();
    }
 
    @GetMapping("/teams")
    public List<Equipo> obtenerTodosEquipos() {
        return equipoRepositorio.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }
 
    @GetMapping("/teams/{id}")
    public ResponseEntity<Equipo> obtenerEquipoPorId(@PathVariable Long id) {
        return equipoRepositorio.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    @PostMapping("/teams")
    public Equipo crearEquipo(@RequestBody Equipo datos) {
        return equipoRepositorio.save(datos);
    }
 
    @PutMapping("/teams/{id}")
    public ResponseEntity<Equipo> actualizarEquipo(
            @PathVariable Long id, @RequestBody Equipo datos) {
        return equipoRepositorio.findById(id).map(e -> {
            e.setNombre(datos.getNombre());
            e.setCategoria(datos.getCategoria());
            return ResponseEntity.ok(equipoRepositorio.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }
 
    @DeleteMapping("/teams/{id}")
    public ResponseEntity<Void> eliminarEquipo(@PathVariable Long id) {
        boolean existe = equipoRepositorio.existsById(id);
        if (!existe) return ResponseEntity.notFound().build();
        equipoRepositorio.deleteById(id);
        return ResponseEntity.ok().build();
    }
 
    @GetMapping("/members")
    public List<Miembro> obtenerMiembros(@RequestParam(required = false) Long idEquipo) {
        if (idEquipo != null)
            return miembroRepositorio.findByEquipo_Id(idEquipo);
        return miembroRepositorio.findAll();
    }
 
    @GetMapping("/members/{id}")
    public ResponseEntity<Miembro> obtenerMiembroPorId(@PathVariable Long id) {
        return miembroRepositorio.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    @PostMapping("/members")
    public ResponseEntity<Miembro> crearMiembro(@RequestBody Miembro datos) {
        var idEquipo = datos.getIdEquipo();
        if (idEquipo != null)
            equipoRepositorio.findById(idEquipo).ifPresent(datos::setEquipo);
        return ResponseEntity.ok(miembroRepositorio.save(datos));
    }
 
    @PutMapping("/members/{id}")
    public ResponseEntity<Miembro> actualizarMiembro(
            @PathVariable Long id, @RequestBody Miembro datos) {
        return miembroRepositorio.findById(id).map(m -> {
            m.setNombre(datos.getNombre());
            m.setRol(datos.getRol());
            m.setUrlFoto(datos.getUrlFoto());
            if (datos.getIdEquipo() != null)
                equipoRepositorio.findById(datos.getIdEquipo()).ifPresent(m::setEquipo);
            return ResponseEntity.ok(miembroRepositorio.save(m));
        }).orElse(ResponseEntity.notFound().build());
    }
 
    @DeleteMapping("/members/{id}")
    public ResponseEntity<Void> eliminarMiembro(@PathVariable Long id) {
        if (!miembroRepositorio.existsById(id))
            return ResponseEntity.notFound().build();
        miembroRepositorio.deleteById(id);
        return ResponseEntity.ok().build();
    }
 
    @GetMapping("/gallery")
    public List<Imagen> obtenerImagenes() {
        return imagenRepositorio.findAll();
    }
 
    @PostMapping("/gallery")
    public Imagen subirImagen(@RequestBody Imagen imagen) {
        return imagenRepositorio.save(imagen);
    }
 
    @DeleteMapping("/gallery/{id}")
    public ResponseEntity<Void> eliminarImagen(@PathVariable Long id) {
        if (!imagenRepositorio.existsById(id))
            return ResponseEntity.notFound().build();
        imagenRepositorio.deleteById(id);
        return ResponseEntity.ok().build();
    }
 
    @PostMapping("/register")
    public ResponseEntity<Usuario> registrar(@RequestBody Usuario usuario) {
        var yaExiste = usuarioRepositorio.findByUsername(usuario.getUsername()).isPresent();
        if (yaExiste) return ResponseEntity.status(409).build();
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return ResponseEntity.ok(usuarioRepositorio.save(usuario));
    }
 
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody Usuario usuario) {
        var clave = usuario.getPassword();
        return usuarioRepositorio.findByUsername(usuario.getUsername())
                .filter(u -> passwordEncoder.matches(clave, u.getPassword()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }
}