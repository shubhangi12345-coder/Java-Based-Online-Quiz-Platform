package com.mancy.quizplatform.exception;

/** Someone tried to use an email address that already has an account. */
public class DuplicateEmailException extends BusinessRuleException {

    public DuplicateEmailException(String email) {
        super("An account with the email " + email + " already exists.");
    }
}
