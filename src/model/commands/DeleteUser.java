package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.types.CmdResultType;
import model.user.UserLog;

import java.util.Map;

/**
 * Represents the ability to delete a specified user from the Model with which this DeleteUser
 * object communicates with, so long as that Model contains the specified user properties.
 *
 */
public class DeleteUser extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   */
  protected DeleteUser(Model m, String[] input) {
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
    UserLog log = this.model.deleteUser(
        flags.get("username"),
        flags.get("password")
    );
    return this.filledResult(UserLog.class, log, CmdResultType.USER_LOGS);
  }
}
