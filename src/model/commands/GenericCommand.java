package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.commandresult.CmdResultImpl;
import model.flag_parser.FlagParser;
import model.flag_parser.FlagParserImpl;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
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
  protected Map<String, Class<?>> expectedFlags;
  private static final Map<String, BiFunction<Model, String[], Command>> cmds = new HashMap<>();

  static {
    cmds.put(
        "accountexists",
        (m, input) -> new AccountExists(m, input)
    );
    cmds.put(
        "createaccount",
        (m, input) -> new CreateAccount(m, input)
    );
    cmds.put(
        "createuser",
        (m, input) -> new CreateUser(m, input)
    );
    cmds.put(
        "deleteaccount",
        (m, input) -> new DeleteAccount(m, input)
    );
    cmds.put(
        "deleteuser",
        (m, input) -> new DeleteUser(m, input)
    );
    cmds.put(
        "deposit",
        (m, input) -> new Deposit(m, input)
    );
    cmds.put(
        "getaccounts",
        (m, input) -> new GetAccounts(m, input)
    );
    cmds.put(
        "getuser",
        (m, input) -> new GetUser(m, input)
    );
    cmds.put(
        "transfer",
        (m, input) -> new Transfer(m, input)
    );
    cmds.put(
        "updateuser",
        (m, input) -> new UpdateUser(m, input)
    );
    cmds.put(
        "withdraw",
        (m, input) -> new Withdraw(m, input)
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
    return cmds.get(cmdName.toLowerCase()).apply(m, in);
  }

  /**
   * Constructs a new Generic Command with respect to the given Bank and the given input by the
   * user.
   * @param m The Model with which this GenericCommand will communicate with.
   * @param input The input from the user to be parsed, validated, deconstructed, and passed to
   *              the given model for further functionality.
   */
  protected GenericCommand(Model m, String[] input) {
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
    return new CmdResultImpl.Builder().withType(type).withProp(prop).build();
  }

  /**
   * Requires that the given flag mapping of (flag, value) contains ALL the given flags.
   * @param given    the flags which were parsed from the user's input. These flags can be
   *                 assumed to all have matching values.
   * @param required the flags which must be present in the given list of actual flags extracted
   *                 from the user's input.
   * @throws IllegalArgumentException if there is any mismatch between the two flag lists.
   */
  protected void requireFlags(
      Map<String, String> given,
      Map<String, Class<?>> required
  ) throws IllegalArgumentException {
    for (Map.Entry<String, Class<?>> entry : required.entrySet()) {
      String key = entry.getKey();
      Class<?> type = entry.getValue();

      String givenValue = given.getOrDefault(key, null);
      if (givenValue == null) {
        throw new IllegalArgumentException(
            "Command expected flag of " + key + " and was not given it!"
        );
      }

      try {
        if (type != String.class) {
          type.getMethod("valueOf", String.class).invoke(null, givenValue);
        }
      } catch (ClassCastException | InvocationTargetException e) {
        throw new IllegalArgumentException(
            "Command expected the flag of " + key + " to have a type of " + type
            + " but was given the following corresponding value: " + givenValue
        );
      } catch (NoSuchMethodException | IllegalAccessException ignored) { }
    }
  }
}
