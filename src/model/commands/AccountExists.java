package model.commands;

import model.Model;
import model.commandresult.CmdResult;

import java.util.Map;

/**
 * Offers functionality for checking whether a specified account is currently active under a
 * given username.
 *
 */
public class AccountExists extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   */
  public AccountExists(Model m, String[] input) {
    super(m, input);
    this.expectedFlags = Map.of(
        "username", String.class,
        "accountName", String.class
    );
  }

  @Override
  public CmdResult execute() {
    Map<String, String> flags = flags();
    this.requireFlags(flags, expectedFlags);
    boolean exists = this.model.accountExists(flags.get("username"), flags.get("accountName"));
    return this.filledResult(Boolean.class, exists);
  }
}
