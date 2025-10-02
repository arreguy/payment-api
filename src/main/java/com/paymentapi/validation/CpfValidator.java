package com.paymentapi.validation;

import com.paymentapi.validation.constraints.ValidCpf;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementação do validator da anotação @ValidCpf.
 * Aceita tanto inputs formatados (000.000.000-00) quanto
 * não formatados (00000000000)
 */
public class CpfValidator implements ConstraintValidator<ValidCpf, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // null/empty é com o @NotNull/@NotBlank
        }

        String cpf = value.replaceAll("[^0-9]", "");

        if (cpf.length() != 11) {
            return false;
        }

        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int remainder = sum % 11;
        int firstCheckDigit = (remainder < 2) ? 0 : (11 - remainder);

        if (Character.getNumericValue(cpf.charAt(9)) != firstCheckDigit) {
            return false;
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        remainder = sum % 11;
        int secondCheckDigit = (remainder < 2) ? 0 : (11 - remainder);

        return Character.getNumericValue(cpf.charAt(10)) == secondCheckDigit;
    }
}
