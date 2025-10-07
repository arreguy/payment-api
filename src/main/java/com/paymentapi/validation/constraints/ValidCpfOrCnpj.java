package com.paymentapi.validation.constraints;

import com.paymentapi.validation.CpfOrCnpjValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação de validation customizada para CPF ou CNPJ.
 * <p>
 * Esta anotação valida tanto CPFs (11 dígitos) quanto CNPJs (14 dígitos)
 * IMPORTANTE: Sempre usar @NotNull em conjunto
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CpfOrCnpjValidator.class)
@Documented
public @interface ValidCpfOrCnpj {
    String message() default "CPF ou CNPJ inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
