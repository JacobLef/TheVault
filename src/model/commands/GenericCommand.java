package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.commandresult.CmdResultImpl;
import model.flag_parser.FlagParser;
import model.flag_parser.FlagParserImpl;
import model.validation.result.ValidationResult;
import model.validation.validator.InputValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Represents a generic executable Banking operation, such that it offers functionality to create
 * any arbitrary command and encapsulates common properties and functionality between all Command
 * objects.
 *
 * <p>
 *   Regarding common functionality, a GenericCommand allows its child classes to create an
 *   empty result or a filled result with either all {@code null} fields or those which are
 *   specified by the user. The reason for these methods being provided by a GenericCommand base
 *   class is to lower the amount of inclusions in all other Command files and to make it easier
 *   for others to add new Commands (follow the same pattern of implementation).
 * </p>
 */
public abstract class GenericCommand implements Command {
  protected final Model model;
  protected final String[] in;
  protected final FlagParser flagParser;
  private static final Map<String, BiFunction<Model, String[], Command>> cmds = new HashMap<>();

  static {
    cmds.put(
        "accountexists",
        (m, input) -> new AccountExists(m, input, new AccountExistsValidator())
    );
    cmds.put(
        "createaccount",
        (m, input) -> new CreateAccount(m, input, new CreateAccountValidator())
    );
    cmds.put(
        "createuser",
        (m, input) -> new CreateUser(m, input, new CreateUserValidator())
    );
    cmds.put(
        "deleteaccount",
        (m, input) -> new DeleteAccount(m, input, new DeleteAccountValidator())
    );
    cmds.put(
        "deleteuser",
        (m, input) -> new DeleteUser(m, input, new DeleteUserValidator())
    );
    cmds.put(
        "deposit",
        (m, input) -> new Deposit(m, input, new DepositValidator())
    );
    cmds.put(
        "getaccounts",
        (m, input) -> new GetAccounts(m, input, new GetAccountsValidator())
    );
    cmds.put(
        "getuser",
        (m, input) -> new GetUser(m, input, new GetUserValidator())
    );
    cmds.put(
        "transfer",
        (m, input) -> new Transfer(m, input, new TransferValidator())
    );
    cmds.put(
        "updateuser",
        (m, input) -> new UpdateUser(m, input, new UpdateValidator())
    );
    cmds.put(
        "withdraw",
        (m, input) -> new Withdraw(m, input, WithdrawValidator())
    );
  }

  /**
   * Factory method to create a command based on the command name provided, and with respect to
   * the given model and input.
   * @param m the model with which the created command should communicate with.
   * @param in the input from the user to be used within the created command object.
   * @param cmdName the name of the command, as a String, which is to be created. The format of
   *                command names is expected to be all lowercase, no special characters.
   *                Ex: CreateUser -> createuser; Withdraw -> withdraw
   * @return {@code null} if the given command name is not a valid command name or the
   *         respective Command object.
   */
  public static Command makeCommand(Model m, String[] in, String cmdName) {
    return cmds.get(cmdName).apply(m, in);
  }

  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   * @param m The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   * @param iv The respective input validator for this GenericCommand. It is reliant upon the
   *           user for the InputValidator and the respective Command to match as this will not
   *           be cross-checked.
   */
  protected GenericCommand(Model m, String[] input, InputValidator iv) {
    ValidationResult res = iv.validate(input);
    if (res.isError()) {
      throw new IllegalArgumentException(res.message());
    }
    this.model = m;
    this.in = input;
    this.flagParser = new FlagParserImpl();
  }

  /**
   * Parses the input stored within this GenericCommand into its respective (flag, value) pairs.
   * @return a Map of the (flag, value) pairs where all information is represented by a String.
   */
  protected Map<String, String> flags() {
    return this.flagParser.parseFlags(in);
  }

  /**
   * Constructs an Empty CmdResult object to signify that no information is returned by the
   * method invoked on this GenericCommand.
   * @return a CmdResultImpl object will all {@code null} fields.
   */
  protected CmdResult emptyResult() {
    return new CmdResultImpl();
  }

  /**
   * Constructs a new CmdResult object with respect to the given parameters.
   * @param type the type of the value returned by this GenericCommand.
   * @param prop the property returned by this GenericCommand.
   * @return the respective CmdResult whose fields reflect those given.
   */
  protected CmdResult filledResult(Class<?> type, Object prop) {
    return new CmdResultImpl.Builder().withType(Boolean.class).withProp(prop).build();
  }

  /**
   * Requires that the given flag mapping of (flag, value) contains ALL the given flags.
   * @param given    the flags which were parsed from the user's input. These flags can be
   *                 assumed to all have matching values.
   * @param required the flags which must be present in the given list of actual flags extracted
   *                 from the user's input.
   * @throws IllegalArgumentException if there is any mismatch between the two flag lists.
   */
  protected abstract void requireFlags(
      List<String> given,
      List<String> required
  ) throws IllegalArgumentException;
}
