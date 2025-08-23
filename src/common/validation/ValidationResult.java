package common.validation;

/**
 *
 */
public interface ValidationResult {
  boolean isError();

  String message();
}
