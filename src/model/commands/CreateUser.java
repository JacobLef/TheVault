package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.types.CmdResultType;
import model.user.User;

import java.util.Map;

/**
 * Represents an encapsulation of the data and functionality needed to create a new, unique User
 * within the Model. The created user must be unique amongst all users inside the same Managed
 * model.
 */
public class CreateUser extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
  */
  protected CreateUser(Model m, String[] input) {
    super(m, input);
    this.expectedFlags = Map.of(
        "username", String.class,
        "password", String.class,
        "email", String.class
    );
  }

  @Override
  public CmdResult execute() {
    Map<String, String> flags = this.flags();
    this.requireFlags(flags, expectedFlags);
    User user = this.model.createUser(
        flags.get("username"),
        flags.get("password"),
        flags.get("email")
    );
    return this.filledResult(User.class, user, CmdResultType.USER_INFO);
  }
}
