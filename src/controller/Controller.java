package controller;
import java.io.IOException;

/**
 * Serves as the entry point for the execution of the banking application, verifying user input
 * before dispatching to either the Model for backend functionality or to the View for displaying
 * information back to the user.
 *
 * <p>
 * Controller should focus solely on the validation of data and thus this interface offers very
 * limited public functionality except for starting the program.
 * <p>
 */
public interface Controller {
  /**
   * Begins execution of the banking application and continues to execute the program until
   * the user asks to stop.
   * @throws IOException if there are any issues with parsing the information retrieved from the
   *         user.
   */
  void go() throws IOException;
}
