package fiap.remembered.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marca endpoints com {@code {idosoId}} na rota: so quem esta vinculado a esse idoso passa. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@PreAuthorize("@vinculoGuard.pode(authentication, #idosoId)")
public @interface AcessoAoIdoso {
}
