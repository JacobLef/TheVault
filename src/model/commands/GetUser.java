package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.user.User;

import java.util.Map;

/**
 * Represents the ability to fetch the information associated with a given user, so long as the
 * specified user exists within the given Model.
 */
public class GetUser extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   */
  protected GetUser(Model m, String[] input) {
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
    User user = this.model.getUser(flags.get("username"), flags.get("password"));
    return user == null ? this.emptyResult() : this.filledResult(User.class, user);
  }
}
