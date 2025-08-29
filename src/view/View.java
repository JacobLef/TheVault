package view;

/**
 * Encapsulates all the information relevant to displaying information to the user, offering
 * functionality to display both errors and messages to the user without halting the application.
 */
public interface View {
  /**
   * Displays the given error message to the user, indicating that an error had occurred.
   * @param s the error message to be displayed to the user.
   */
  void displayError(String s);

  /**
   * Displays the given message to the user, with the assumption it is not an erroneous message.
   * @param s the message to be displayed to the user.
   */
  void displayMessage(String s);
}
