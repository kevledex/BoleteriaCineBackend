package com.itsqmet.boleteriacinebackend.controller;

import com.itsqmet.boleteriacinebackend.model.Usuario;
import com.itsqmet.boleteriacinebackend.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired
  private UsuarioService usuarioService;

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private RememberMeServices rememberMeServices;

  @PostMapping("/registro")
  public ResponseEntity<?> registrar(
    @Valid @RequestBody Usuario usuario,
    BindingResult result
  ) {
    if (result.hasErrors()) {
      Map<String, String> errores = new HashMap<>();
      result
        .getFieldErrors()
        .forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    return usuarioService
      .registrar(usuario)
      .map(error ->
        ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", error))
      )
      .orElse(
        ResponseEntity.status(HttpStatus.CREATED).body(
          Map.of("mensaje", "Usuario registrado correctamente")
        )
      );
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
    @RequestBody Map<String, String> credenciales,
    HttpServletRequest request,
    HttpServletResponse response
  ) {
    String email = credenciales.get("email");
    String password = credenciales.get("password");

    try {
      Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(email, password)
      );

      SecurityContextHolder.getContext().setAuthentication(authentication);

      HttpSessionSecurityContextRepository contextRepository =
        new HttpSessionSecurityContextRepository();
      contextRepository.saveContext(
        SecurityContextHolder.getContext(),
        request,
        response
      );
      rememberMeServices.loginSuccess(request, response, authentication);

      List<String> roles = authentication
        .getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

      Usuario usuario = usuarioService
        .obtenerPorEmail(email)
        .orElseThrow(() ->
          new IllegalStateException("El usuario autenticado no existe")
        );

      return ResponseEntity.ok(
        new LoginRespuesta(
          "Login exitoso",
          usuario.getEmail(),
          usuario.getId(),
          usuario.getNombre(),
          usuario.getEmail(),
          usuario.getTelefono(),
          usuario.getRol(),
          roles
        )
      );
    } catch (AuthenticationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
        Map.of("error", "Email o contraseña incorrectos")
      );
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(
    HttpServletRequest request,
    HttpServletResponse response
  ) {
    if (rememberMeServices instanceof LogoutHandler handler) {
      handler.logout(
        request,
        response,
        SecurityContextHolder.getContext().getAuthentication()
      );
    }

    HttpSession session = request.getSession(false);

    if (session != null) {
      session.invalidate();
    }

    SecurityContextHolder.clearContext();

    return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
  }

  @GetMapping("/perfil")
  public ResponseEntity<?> perfil() {
    Authentication authentication =
      SecurityContextHolder.getContext().getAuthentication();

    List<String> roles = authentication
      .getAuthorities()
      .stream()
      .map(GrantedAuthority::getAuthority)
      .toList();

    return usuarioService
      .obtenerPorEmail(authentication.getName())
      .map(usuario ->
        ResponseEntity.ok(
          (Object) new PerfilRespuesta(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getTelefono(),
            usuario.getRol(),
            usuario.getEmail(),
            roles
          )
        )
      )
      .orElse(
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
          Map.of("error", "Usuario no encontrado")
        )
      );
  }

  private record LoginRespuesta(
    String mensaje,
    String usuario,
    Long id,
    String nombre,
    String email,
    String telefono,
    String rol,
    List<String> roles
  ) {}

  private record PerfilRespuesta(
    Long id,
    String nombre,
    String email,
    String telefono,
    String rol,
    String usuarioActual,
    List<String> roles
  ) {}
}
