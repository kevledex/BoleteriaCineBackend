package com.itsqmet.boleteriacinebackend.service;

import com.itsqmet.boleteriacinebackend.model.Usuario;
import com.itsqmet.boleteriacinebackend.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  public List<Usuario> obtenerTodos() {
    return usuarioRepository.findAll();
  }

  public Optional<Usuario> obtenerPorId(Long id) {
    return usuarioRepository.findById(id);
  }

  public Optional<Usuario> obtenerPorEmail(String email) {
    return usuarioRepository.findByEmail(email);
  }

  public Optional<String> registrar(Usuario usuario) {
    if (usuarioRepository.existsByEmail(usuario.getEmail())) {
      return Optional.of("El email ya está registrado");
    }

    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
    usuario.setRol("CLIENTE");
    usuario.setActivo(true);
    usuario.setFechaRegistro(LocalDateTime.now());

    usuarioRepository.save(usuario);
    return Optional.empty();
  }

  public Optional<Usuario> actualizar(Long id, Usuario usuarioActualizado) {
    return usuarioRepository.findById(id).map(usuario -> {
      usuario.setNombre(usuarioActualizado.getNombre());
      usuario.setTelefono(usuarioActualizado.getTelefono());
      return usuarioRepository.save(usuario);
    });
  }

  public boolean eliminar(Long id) {
    if (usuarioRepository.existsById(id)) {
      usuarioRepository.deleteById(id);
      return true;
    }
    return false;
  }
}
