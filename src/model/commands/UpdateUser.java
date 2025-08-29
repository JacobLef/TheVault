package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.types.CmdResultType;
import model.types.UserProperty;
import model.user.User;

import java.util.Map;

/**
 * Represents the ability to update one of the properties (username, email, password) of a
 * specified user, so long as that user exists within the specified Model.
 */
public class UpdateUser extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   */
  protected UpdateUser(Model m, String[] input) {
    super(m, input);
    this.expectedFlags = Map.of(
        "username", String.class,
        "password", String.class,
        "property", UserProperty.class,
        "value", String.class
    );
  }

  @Override
  public CmdResult execute() {
    Map<String, String> flags = this.flags();
    this.requireFlags(flags, expectedFlags);
    User user = this.model.updateUser(
        flags.get("username"),
        flags.get("password"),
        UserProperty.valueOf(flags.get("property")),
        flags.get("value")
    );
    return this.filledResult(User.class, user, CmdResultType.USER_INFO);
  }
}
