package model.commands;

import model.Model;
import model.validation.InputValidator;
import model.validation.ValidationResult;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Represents a generic executable Banking operation, such that it offers functionality to create
 * any arbitrary command and encapsulates common properties and functionality between all Command
 * objects.
 */
public abstract class GenericCommand implements Command {
  protected Model model;
  protected String[] in;
  protected InputValidator iv;
  private static Map<String, BiFunction<Model, String[], Command>> cmds = new HashMap<>();

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
    this.iv = iv;
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
}
