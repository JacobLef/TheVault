package view;

import model.ReadableModel;

public class CLIView implements View {
  private ReadableModel model;
  public Appendable out;

  public CLIView(ReadableModel model, Appendable out) {
    this.model = model;
    this.out = out;
  }

  @Override
  public void displayError(String s) {

  }

  @Override
  public void displayMessage(String s) {

  }
}
