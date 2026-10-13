package com.mancy.quizplatform.exception;

/**
 * The request was understood but breaks a rule of the application
 * (for example "a quiz needs at least 3 questions"). The message is safe to show to the user.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
