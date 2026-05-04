package com.barclays.takehomecodingtest.controllers;

import com.barclays.takehomecodingtest.service.AccountAssociatedWithUserException;
import com.barclays.takehomecodingtest.service.AccountNotFoundException;
import com.barclays.takehomecodingtest.service.AuthenticationFailedException;
import com.barclays.takehomecodingtest.service.InsufficientFundsException;
import com.barclays.takehomecodingtest.service.InvalidTransactionException;
import com.barclays.takehomecodingtest.service.UnauthorizedAccessException;
import com.barclays.takehomecodingtest.service.UserAlreadyExistsException;
import com.barclays.takehomecodingtest.service.UserNotFoundException;
import com.barclays.takehomecodingtest.dto.BadRequestErrorResponse;
import com.barclays.takehomecodingtest.dto.ErrorResponse;
import com.barclays.takehomecodingtest.dto.ValidationErrorDetail;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ErrorResponse> handleServletRequestBindingException(ServletRequestBindingException ex) {
        logger.error("ServletRequestBindingException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BadRequestErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<ValidationErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new ValidationErrorDetail(
                        err.getField(),
                        err.getDefaultMessage(),
                        err.getCode()))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BadRequestErrorResponse("Validation failed", details));
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<ErrorResponse> handleHttpStatusCodeException(HttpStatusCodeException ex) {
        logger.error("HttpStatusCodeException: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse(ex.getResponseBodyAsString()));
    }

    @ExceptionHandler(AccountAssociatedWithUserException.class)
    public ResponseEntity<ErrorResponse> handleAccountAssociatedWithUserException(
            AccountAssociatedWithUserException ex) {
        logger.error("AccountAssociatedWithUserException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        logger.error("UserNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        logger.error("UnauthorizedAccessException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailedException(AuthenticationFailedException ex) {
        logger.error("AuthenticationFailedException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageReadableException(HttpMessageNotReadableException ex) {
        logger.error("HttpMessageNotReadableException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        logger.error("UserAlreadyExistsException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Username already exists"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.error("DataIntegrityViolationException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                "Data constraint violation: could not save changes. Please check your input is sufficiently unique and try again"));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex) {
        logger.error("AccountNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({ InsufficientFundsException.class, InvalidTransactionException.class })
    public ResponseEntity<BadRequestErrorResponse> handleTransactionExceptions(RuntimeException ex) {
        logger.error("Transaction error: {}", ex.getMessage());
        List<ValidationErrorDetail> details = List.of(new ValidationErrorDetail(
                "amount",
                ex.getMessage(),
                ex.getClass().getSimpleName()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BadRequestErrorResponse("Transaction failed", details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred"));
    }
}