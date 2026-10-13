package com.mancy.quizplatform.exception;

/** Something that was asked for (quiz, question, user ...) does not exist. Shown as a 404 page. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
