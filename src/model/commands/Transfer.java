package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.types.CmdResultType;
import model.user.Transaction;

import java.util.Map;

/**
 * Represents the ability to Transfer information from one account to another account, either
 * under the same user, same account, or different users.
 */
public class Transfer extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   */
  protected Transfer(Model m, String[] input) {
    super(m, input);
    this.expectedFlags = Map.of(
        "fromUsername", String.class,
        "toUsername", String.class,
        "fromPassword", String.class,
        "toPassword", String.class,
        "fromAccountName", String.class,
        "toAccountName", String.class,
        "amount", Double.class
    );
  }

  @Override
  public CmdResult execute() {
    Map<String, String> flags = this.flags();
    this.requireFlags(flags, expectedFlags);
    Transaction transaction = this.model.transfer(
        flags.get("fromUsername"),
        flags.get("toUsername"),
        flags.get("fromPassword"),
        flags.get("toPassword"),
        flags.get("fromAccountName"),
        flags.get("toAccountName"),
        Double.parseDouble(flags.getOrDefault("amount", "0"))
    );
    return this.filledResult(Transaction.class, transaction, CmdResultType.TRANSACTION);
  }
}
