package view;

import model.ReadableModel;

public class CLIView implements View {
  private ReadableModel model;

  public CLIView(ReadableModel model) {
    this.model = model;
  }
}
