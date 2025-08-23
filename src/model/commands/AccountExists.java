package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.validation.validator.InputValidator;

import java.util.Map;

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
    Map<String, String> flags = flags();
    boolean exists = this.model.accountExists(flags.get("username"), flags.get("accountName"));
    return this.filledResult(Boolean.class, exists);
  }
}
