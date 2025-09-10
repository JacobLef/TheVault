package view;

import model.ReadableModel;

import java.io.IOException;

/**
 * Represents functionality to display information to the user regarding the command-lind
 * interface. Offers the ability to display both errors and messages to the user, without
 * crashing the program.
 */
public class CLIView implements View {
  private ReadableModel model;
  public Appendable out;

  public CLIView(ReadableModel model, Appendable out) {
    this.model = model;
    this.out = out;
  }

  @Override
  public void displayError(String s) {
    try {
      this.out.append("Error: ").append(s);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public void displayMessage(String s) {
    try {
      this.out.append(s);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
