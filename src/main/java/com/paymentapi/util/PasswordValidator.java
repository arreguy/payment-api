package com.paymentapi.util;

import com.paymentapi.exception.InvalidPasswordException;
import java.util.regex.Pattern;

/**
 * Utilitário para validação de complexidade de senhas.
 *
 * <p>Requisitos de senha:
 * <ul>
 *   <li>Mínimo 8 caracteres</li>
 *   <li>Máximo 100 caracteres</li>
 *   <li>Pelo menos uma letra maiúscula (A-Z)</li>
 *   <li>Pelo menos uma letra minúscula (a-z)</li>
 *   <li>Pelo menos um dígito (0-9)</li>
 * </ul>
 *
 */
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");

    /**
     * Construtor privado para prevenir a instanciação da utility
     */
    private PasswordValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Valida se a senha atende a todos os requisitos de complexidade.
     * Lança InvalidPasswordException com mensagem específica se alguma regra for violada.
     *
     * @param password senha a ser validada
     * @throws InvalidPasswordException se senha não atender aos requisitos
     */
    public static void validatePassword(String password) {
        if (password == null) {
            throw new InvalidPasswordException("Senha não pode ser nula");
        }

        if (password.length() < MIN_LENGTH) {
            throw new InvalidPasswordException(
                String.format("Senha deve ter no mínimo %d caracteres", MIN_LENGTH)
            );
        }

        if (password.length() > MAX_LENGTH) {
            throw new InvalidPasswordException(
                String.format("Senha deve ter no máximo %d caracteres", MAX_LENGTH)
            );
        }

        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            throw new InvalidPasswordException(
                "Senha deve conter pelo menos uma letra maiúscula"
            );
        }

        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            throw new InvalidPasswordException(
                "Senha deve conter pelo menos uma letra minúscula"
            );
        }

        if (!DIGIT_PATTERN.matcher(password).matches()) {
            throw new InvalidPasswordException(
                "Senha deve conter pelo menos um dígito"
            );
        }
    }
}
