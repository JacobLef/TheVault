package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.types.CmdResultType;

import java.util.Map;

/**
 * Represents the ability to check whether a specified user exists in the given Model.
 */
public class UserExists extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   */
  protected UserExists(Model m, String[] input) {
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
    boolean exists = this.model.userExists(flags.get("username"), flags.get("password"));
    return this.filledResult(Boolean.class, exists, CmdResultType.BOOLEAN_FLAG);
  }
}
