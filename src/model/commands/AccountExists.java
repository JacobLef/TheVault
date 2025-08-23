package model.commands;

import common.validation.InputValidator;
import common.validation.ValidationResult;
import model.Bank;
import model.commandresult.CommandResult;

/**
 * Offers functionality for checking whether a specified account is currently active under a
 * given username. An AccountExists object, when executed, will return its respective
 * AccountExistsResult record class.
 *
 * @see AccountExistsResult record class for the structured return tytpe of this class.
 * @see AccountExistsValidator for the validation logic of the input given to an AccountExists obj.
 */
public class AccountExists implements Command {
  private final Bank bank;
  private final String[] input ;

  /**
   * Constructs a new AccountExists object with respect to the given Bank and input by the user.
   * Validates the provided input, delegating the validation logic to the provided validator and
   * throwing any errors that occur, if they do occur.
   * @param bank  The bank with which this AccountExists object will communicate with.
   * @param input The input from the user.
   * @param iv    The validator for this respective object.
   */
  public AccountExists(
      Bank bank,
      String[] input,
      InputValidator iv
  ) throws IllegalArgumentException {
    ValidationResult res = iv.validate(input);
    if (res.isError()) {
      throw new IllegalArgumentException(res.message());
    }

    this.bank = bank;
    this.input = input;
    this.execute();
  }

  @Override
  public CommandResult execute() {
    return null;
  }
}
