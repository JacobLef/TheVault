package model.validation.result;

/**
 * C-like struct with an error identifier and the error message, if the given {@code isError} is
 * {@code true}.
 * @param isError is this ValidationResultImpl reflective of an erroneous validation attempt?
 * @param message the error message associated with this ValidationResultImpl.
 */
public record ValidationResultImpl(boolean isError, String message) {
  public ValidationResultImpl {
    if (!isError && !message.isEmpty()) {
      message = "";
    }
  }
}
