package controller;

import model.Model;
import view.View;

import java.io.InputStream;

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

  }
}
