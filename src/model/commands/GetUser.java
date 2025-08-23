package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.user.User;
import model.validation.validator.InputValidator;

import java.util.Map;

/**
 * Represents the ability to fetch the information associated with a given user, so long as the
 * specified user exists within the given Model. The GetUser objects rely entirely on the
 * GetUserValidator class for all validation logic.
 *
 * @see GetUserValidator for validation logic.
 */
public class GetUser extends GenericCommand {
  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   *
   * @param m     The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   * @param iv    The respective input validator for this GenericCommand. It is reliant upon the
   *              user for the InputValidator and the respective Command to match as this will not
   *              be cross-checked.
   */
  protected GetUser(Model m, String[] input, InputValidator iv) {
    super(m, input, iv);
  }

  @Override
  public CmdResult execute() {
    Map<String, String> flags = this.flags();
    User user = this.model.getUser(flags.get("username"), flags.get("password"));
    if (user == null) {
      return this.emptyResult();
    }
    return this.filledResult(User.class, user);
  }
}
