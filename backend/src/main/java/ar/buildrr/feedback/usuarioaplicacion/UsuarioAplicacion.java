package ar.buildrr.feedback.usuarioaplicacion;

import ar.buildrr.feedback.ticket.entity.Producto;
import jakarta.persistence.*;
import lombok.*;

/**
 * Qué aplicaciones puede ver/cargar un usuario y con qué rol — ADMIN gestiona
 * el ciclo de vida de los tickets de ese producto, CLIENTE solo crea/ve los
 * propios. Es el rol dentro de la tiquetera, no el rol de su organización
 * en SGO (ese viene en el JWT y acá no importa).
 */
@Entity
@Table(name = "usuario_aplicacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAplicacion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String username;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Producto producto;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RolAplicacion rol;
}
