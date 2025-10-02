package com.paymentapi.validation;

import com.paymentapi.validation.constraints.ValidCnpj;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementação do validator da anotação @ValidCnpj.
 * Aceita tanto inputs formatados (00.000.000/0000-00) quanto
 * não formatados (00000000000000)
 */
public class CnpjValidator implements ConstraintValidator<ValidCnpj, String> {

    private static final int[] FIRST_CHECK_WEIGHTS = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] SECOND_CHECK_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // null/empty é com o @NotNull/@NotBlank
        }

        String cnpj = value.replaceAll("[^0-9]", "");

        if (cnpj.length() != 14) {
            return false;
        }

        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * FIRST_CHECK_WEIGHTS[i];
        }
        int remainder = sum % 11;
        int firstCheckDigit = (remainder < 2) ? 0 : (11 - remainder);

        if (Character.getNumericValue(cnpj.charAt(12)) != firstCheckDigit) {
            return false;
        }

        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * SECOND_CHECK_WEIGHTS[i];
        }
        remainder = sum % 11;
        int secondCheckDigit = (remainder < 2) ? 0 : (11 - remainder);

        return Character.getNumericValue(cnpj.charAt(13)) == secondCheckDigit;
    }
}
