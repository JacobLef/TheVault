package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.user.BankAccount;
import model.validation.validator.InputValidator;

import java.util.Map;

/**
 * Represents the ability to fetch all the information associated with a given account under a
 * specified user, so long as both exist within the given User and Model, respectively. GetAccount
 * objects rely entirely on the GetAccountValidator class for all input validation.
 *
 * @see GetAccountValidator for all validation logic.
 */
public class GetAccount extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   * @param iv    The respective input validator for this GenericCommand. It is reliant upon the
   *              user for the InputValidator and the respective Command to match as this will not
   *              be cross-checked.
   */
  protected GetAccount(Model m, String[] input, InputValidator iv) {
    super(m, input, iv);
  }

  @Override
  public CmdResult execute() {
    Map<String, String> flags = this.flags();
    BankAccount bc = this.model.getAccountFor(
        flags.get("username"),
        flags.get("password"),
        flags.get("accountName")
    );
    return this.filledResult(BankAccount.class, bc);
  }
}
