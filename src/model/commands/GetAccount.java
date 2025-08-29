package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.types.CmdResultType;
import model.user.BankAccount;

import java.util.Map;

/**
 * Represents the ability to fetch all the information associated with a given account under a
 * specified user, so long as both exist within the given User and Model, respectively.
 */
public class GetAccount extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   */
  protected GetAccount(Model m, String[] input) {
    super(m, input);
    this.expectedFlags = Map.of(
        "username", String.class,
        "password", String.class,
        "accountName", String.class
    );
  }

  @Override
  public CmdResult execute() {
    Map<String, String> flags = this.flags();
    this.requireFlags(flags, expectedFlags);
    BankAccount bc = this.model.getAccountFor(
        flags.get("username"),
        flags.get("password"),
        flags.get("accountName")
    );
    return this.filledResult(BankAccount.class, bc, CmdResultType.BANK_ACCOUNT);
  }
}
