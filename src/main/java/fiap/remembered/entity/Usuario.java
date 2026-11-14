package fiap.remembered.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Todo registro de TB_USUARIO e um idoso. O cuidador vive na API .NET (NET_CUIDADOR). */
@Entity
@Table(name = "TB_USUARIO")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(name = "push_token", length = 200)
    private String pushToken;

    @Column(name = "codigo_pareamento", length = 10)
    private String codigoPareamento;

    @Column(name = "codigo_expira_em")
    private LocalDateTime codigoExpiraEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
    }

    public static Usuario novo(String nome, String email, String senhaHash) {
        Usuario usuario = new Usuario();
        usuario.nome = nome;
        usuario.email = email;
        usuario.senhaHash = senhaHash;
        return usuario;
    }

    public void definirCodigoPareamento(String codigo, LocalDateTime expiraEm) {
        this.codigoPareamento = codigo;
        this.codigoExpiraEm = expiraEm;
    }
}
