package com.ead.authuser.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameConstraintImpl implements ConstraintValidator<UsernameConstraint, String> {

    @Override
    public void initialize(final UsernameConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    /**
     *
     * @param username é o nome do usuário que é passado na criação
     * @param constraintValidatorContext contexto
     * @return valor booleano indicando se o valor passado é válido ou não
     *
     * Na validação não é possível:
     * <ul>
     *     <li>Passar username como NULL</li>
     *     <li>Passar username como VAZIO</li>
     *     <li>Passar username com espaço em branco - ex.: "joão santos"</li>
     * </ul>
     *
     * Forma simplificada de verificação (equivalente ao código atual):
     * return username != null && !username.trim().isEmpty() && !username.contains(" ");
     */
    @Override
    public boolean isValid(
            final String username,
            final ConstraintValidatorContext constraintValidatorContext
    ) {
        if (username == null || username.trim().isEmpty() || username.contains(" ")) {
            return false;
        }
        return true;
    }

}
