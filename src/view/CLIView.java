package view;

import model.ReadableModel;

import java.io.IOException;

/**
 * Represents functionality to display information to the user with regards to the command-lind
 * interface.
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

    }
  }

  @Override
  public void displayMessage(String s) {
    try {
      this.out.append(s);
    } catch (IOException e) {

    }
  }
}
