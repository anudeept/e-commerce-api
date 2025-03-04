package com.ecommerce.exception;

import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    log.error("Validation error: {}", ex.getMessage());
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<Map<String, String>> handleDuplicateKeyException(DuplicateKeyException ex) {
    log.error("Duplicate key error: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", "A record with this unique field already exists");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(
      DataIntegrityViolationException ex) {
    log.error("Data integrity violation: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", "Database integrity constraint violated");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(MongoTimeoutException.class)
  public ResponseEntity<Map<String, String>> handleMongoTimeoutException(MongoTimeoutException ex) {
    log.error("Mongo timeout error: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", "Database operation timed out. Please try again later");
    return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(error);
  }

  @ExceptionHandler(MongoException.class)
  public ResponseEntity<Map<String, String>> handleMongoException(MongoException ex) {
    log.error("Mongo exception: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", "Database operation failed. Please try again later");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  @ExceptionHandler(UncategorizedMongoDbException.class)
  public ResponseEntity<Map<String, String>> handleUncategorizedMongoDbException(
      UncategorizedMongoDbException ex) {
    log.error("Uncategorized MongoDB exception: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", "An unexpected database error occurred");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> handleResponseStatusException(
      ResponseStatusException ex) {
    log.error("Response status exception: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getReason());
    return ResponseEntity.status(ex.getStatusCode()).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
    log.error("Generic exception: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", "An unexpected error occurred");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
