package com.beamcard.profile.rest.exception;

import com.beamcard.profile.domain.exception.AwardNotFoundException;
import com.beamcard.profile.domain.exception.InvalidAvatarException;
import com.beamcard.profile.domain.exception.InvalidAwardException;
import com.beamcard.profile.domain.exception.InvalidShowcaseException;
import com.beamcard.profile.domain.exception.LinkNotFoundException;
import com.beamcard.profile.domain.exception.ProfileNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ProfileNotFoundException.class)
    ProblemDetail handleProfileNotFound(ProfileNotFoundException ex) {
        log.debug("Profile not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "profile_not_found", "Profile not found.");
    }

    @ExceptionHandler(LinkNotFoundException.class)
    ProblemDetail handleLinkNotFound(LinkNotFoundException ex) {
        log.debug("Link not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "link_not_found", "Link not found.");
    }

    @ExceptionHandler(InvalidAvatarException.class)
    ProblemDetail handleInvalidAvatar(InvalidAvatarException ex) {
        log.debug("Invalid avatar: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "invalid_avatar", ex.getMessage());
    }

    @ExceptionHandler(AwardNotFoundException.class)
    ProblemDetail handleAwardNotFound(AwardNotFoundException ex) {
        log.debug("Award not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "award_not_found", "Award not found.");
    }

    @ExceptionHandler(InvalidAwardException.class)
    ProblemDetail handleInvalidAward(InvalidAwardException ex) {
        log.debug("Invalid award: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "invalid_award", ex.getMessage());
    }

    @ExceptionHandler(InvalidShowcaseException.class)
    ProblemDetail handleInvalidShowcase(InvalidShowcaseException ex) {
        log.debug("Invalid showcase: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "invalid_showcase", ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), messageOr(fieldError.getDefaultMessage()));
        }
        log.debug("Request body validation failed: {}", errors);
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "validation_failed", "Request validation failed.");
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), messageOr(violation.getMessage()));
        }
        log.debug("Constraint violation: {}", errors);
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "validation_failed", "Request validation failed.");
        body.setProperty("errors", errors);
        return body;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", "An unexpected error occurred.");
    }

    private static String messageOr(String message) {
        return message == null ? "invalid" : message;
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(status.getReasonPhrase());
        pd.setProperty("code", code);
        return pd;
    }
}
