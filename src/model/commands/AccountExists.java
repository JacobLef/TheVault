package model.commands;

import model.commandresult.CmdResult;
import model.validation.validator.InputValidator;
import model.Model;

/**
 * Offers functionality for checking whether a specified account is currently active under a
 * given username.
 *
 * @see AccountExistsValidator for the validation logic of the input given to an AccountExists obj.
 */
public class AccountExists extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   * @param iv    The validator to validate this AccountExists object's input.
   */
  public AccountExists(Model m, String[] input, InputValidator iv) {
    super(m, input, iv);
  }

  @Override
  public CmdResult execute() {
    /*
     * Layout:
     * - Assume valid inputs
     * - Can deconstruct the inputs stored within this AccountExists objects however we like, so
     * long as it agrees with the way it was decomposed within the respective InputValidator.
     * Since the way commands are inputted is unanimous across the entire program, this should
     * not be an issue to line up.
     * - Call the respective method on the provided Model with the respective parameters.
     * MUST CHANGE THE RETURN TYPE TO BE A RESULT TYPE THAT IS A BUILDER OBJECT WHICH WRAPS THE
     * ABILITY TO HAVE A RESULTING RETURN TYPE, WHERE YOU CAN CUSTOMIZE IT RELATIVE TO IF THE
     * OPERATION IS A FETCHING OPERATION OR IF IT IS SIMPLY AN EXECUABLE OPERATION
     */
    return;
  }
}
