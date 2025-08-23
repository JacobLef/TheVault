package controller;

import model.Managed;
import model.Model;
import model.commands.*;
import view.View;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

/**
 * An individual command-based controller such that it expects singular commands from the user,
 * one by one, rather than an entire file of instructions.
 *
 * <p>
 *  The interactive controller is entirely responsible for the validation of user inputs, and so
 *  it can be assumed that any input it passes along to the given Model or View is valid.
 * </p>
 */
public class InteractiveController implements Controller {
  private final InputStream in;
  private final Model model;
  private final View view;

  /**
   * Constructs a new InteractiveController with respect to the given parameters.
   * @param in    The respective {@code InputStream} from which this InteractiveController
   *              reads information from.
   * @param model The model which is to be communicated with for all functionality.
   * @param view  The view which is to be communicated with when information needs to be displayed
   *              to the user.
   */
  public InteractiveController(InputStream in, Model model, View view) {
    this.in = in;
    this.view = view;
    this.model = model;
  }

  @Override
  public void go() {
    Scanner scanner = new Scanner(in);
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] split = line.split("\\s");
      try {
        if (split[0].trim().equalsIgnoreCase("exit")) {
          this.view.displayMessage("Closing down connection to bank: " + this.bank.getRecords());
          break;
        }
        GenericCommand.makeCommand(this.model, split, split[0]).execute();
      } catch (IndexOutOfBoundsException e) {
        this.view.displayError(
            "The given command does not have all the required parameters: \n\t" + line
        );
      } catch (NullPointerException | IllegalArgumentException e) {
        this.view.displayError(e);
      }
    }
    scanner.close();
  }
}
