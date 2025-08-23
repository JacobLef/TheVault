package model.validation.result;

/**
 *
 */
public interface ValidationResult {
  boolean isError();
  String message();
}
