package controller;

import model.Model;
import view.View;

public class InteractiveController implements Controller {
  private final Model model;
  private final View view;

  public InteractiveController(Model model, View view) {
    this.view = view;
    this.model = model;
  }
}
