package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.types.CmdResultType;
import model.user.BankAccount;

import java.util.List;
import java.util.Map;

/**
 * Represents the ability to fetch all information with regard to all accounts under a specified
 * user, so long as the specified User exists within the given Model.
 *
 */
public class GetAccounts extends GenericCommand{
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   */
  protected GetAccounts(Model m, String[] input) {
    super(m, input);
    this.expectedFlags = Map.of(
        "username", String.class,
        "password", String.class
    );
  }

  @Override
  public CmdResult execute() {
    Map<String, String> flags = this.flags();
    this.requireFlags(flags, expectedFlags);
    List<BankAccount> accList = this.model.getAccountsFor(
        flags.get("username"),
        flags.get("password")
    );
    return this.filledResult(List.class, accList, CmdResultType.BANK_ACCOUNT_LIST);
  }
}
