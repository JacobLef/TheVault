package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.types.CmdResultType;

import java.util.Map;

/**
 * Represents the ability to withdraw from a specified account under a specified user, so long as
 * they exist under the given user and model, respectively.
 */
public class Withdraw extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   */
  protected Withdraw(Model m, String[] input) {
    super(m, input);
    this.expectedFlags = Map.of(
        "username", String.class,
        "password", String.class,
        "accountName", String.class,
        "amount", Double.class
    );
  }

  @Override
  public CmdResult execute() {
    Map<String, String> flags = this.flags();
    this.requireFlags(flags, expectedFlags);
    double withdrawn = this.model.withdraw(
        flags.get("username"),
        flags.get("password"),
        flags.get("accountName"),
        Double.parseDouble(flags.getOrDefault("amount", "0"))
    );
    return this.filledResult(Double.class, withdrawn, CmdResultType.BALANCE);
  }
}
