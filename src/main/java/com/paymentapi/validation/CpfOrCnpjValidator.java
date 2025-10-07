package com.paymentapi.validation;

import com.paymentapi.validation.constraints.ValidCpfOrCnpj;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementação do validator da anotação @ValidCpfOrCnpj.
 * <p>
 * Este validador detecta automaticamente se o input é CPF
 * ou CNPJ (14 dígitos) e delega para o validador apropriado.
 */
public class CpfOrCnpjValidator implements ConstraintValidator<ValidCpfOrCnpj, String> {

    private final CpfValidator cpfValidator = new CpfValidator();
    private final CnpjValidator cnpjValidator = new CnpjValidator();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        // Sanitização
        String sanitized = value.replaceAll("[^0-9]", "");

        if (sanitized.length() == 11) {
            return cpfValidator.isValid(sanitized, context);
        } else if (sanitized.length() == 14) {
            return cnpjValidator.isValid(sanitized, context);
        }

        return false;
    }
}
