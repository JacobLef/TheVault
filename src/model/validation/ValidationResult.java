package model.validation;

/**
 *
 */
public interface ValidationResult {
  boolean isError();
  String message();
}
