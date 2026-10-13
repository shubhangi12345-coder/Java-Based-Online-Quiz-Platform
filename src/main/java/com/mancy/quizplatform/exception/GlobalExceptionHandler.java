package com.mancy.quizplatform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Turns our own exceptions into friendly error pages.
 * Access-denied problems are NOT handled here: Spring Security shows the /access-denied page for them.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        return page("Not found", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ModelAndView handleBusinessRule(BusinessRuleException ex) {
        return page("Action not allowed", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // e.g. /participant/quizzes?difficulty=BANANA
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleBadParameter(MethodArgumentTypeMismatchException ex) {
        return page("Invalid request", "One of the values in the address is not valid.", HttpStatus.BAD_REQUEST);
    }

    private ModelAndView page(String title, String message, HttpStatus status) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("errorTitle", title);
        mav.addObject("errorMessage", message);
        mav.addObject("errorCode", status.value());
        return mav;
    }
}
