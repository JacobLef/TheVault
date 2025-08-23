package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.user.BankAccount;
import model.validation.validator.InputValidator;

import java.util.List;
import java.util.Map;

/**
 * Represents the ability to fetch all information with regard to all accounts under a specified
 * user, so long as the specified User exists within the given Model. GetAccounts objects rely
 * entirely on the GetAccountsValidator for input validation.
 *
 * @see GetAccountsValidator for validation logic.
 */
public class GetAccounts extends GenericCommand{
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
  protected GetAccounts(Model m, String[] input, InputValidator iv) {
    super(m, input, iv);
  }

  @Override
  public CmdResult execute() {
    Map<String, String> flags = this.flags();
    List<BankAccount> accList = this.model.getAccountsFor(
        flags.get("username"),
        flags.get("password")
    );
    return this.filledResult(accList.getClass(), accList);
  }
}
