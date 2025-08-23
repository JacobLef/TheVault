package controller;

import model.Managed;
import model.Model;
import model.commands.*;
import view.View;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

/**
 * An individual command-based controller such that it expects singular commands from the user,
 * one by one, rather than an entire file of instructions.
 *
 * <p>
 *  The interactive controller is entirely responsible for the validation of user inputs, and so
 *  it can be assumed that any input it passes along to the given Model or View is valid.
 * </p>
 */
public class InteractiveController implements Controller {
  private final InputStream in;
  private final Model model;
  private final Managed bank;
  private final View view;
  private final Map<String, Function<String[], ?>> cmds;

  /**
   * Constructs a new InteractiveController with respect to the given parameters.
   * @param in    The respective {@code InputStream} from which this InteractiveController
   *              reads information from.
   * @param model The model which is to be communicated with for all functionality.
   * @param view  The view which is to be communicated with when information needs to be displayed
   *              to the user.
   */
  public InteractiveController(InputStream in, Model model, View view) {
    this.in = in;
    this.view = view;
    this.model = model;
    this.bank = this.model.getCurrentlyActiveBank();

    this.cmds = new HashMap<>();
    cmds.put(
        "accountexists",
        (input) -> new AccountExists(bank, input, new AccountExistsValidator())
    );
    cmds.put(
        "createaccount",
        (input) -> new CreateAccount(bank, input, new CreateAccountValidator())
    );
    cmds.put(
        "createuser",
        (input) -> new CreateUser(bank, input, new CreateUserValidator())
    );
    cmds.put(
        "deleteaccount",
        (input) -> new DeleteAccount(bank, input, new DeleteAccountValidator())
    );
    cmds.put(
        "deleteuser",
        (input) -> new DeleteUser(bank, input, new DeleteUserValidator())
    );
    cmds.put(
        "deposit",
        (input) -> new Deposit(bank, input, new DepositValidator())
    );
    cmds.put(
        "getaccounts",
        (input) -> new GetAccounts(bank, input, new GetAccountsValidator())
    );
    cmds.put(
        "getuser",
        (input) -> new GetUser(bank, input, new GetUserValidator())
    );
    cmds.put(
        "transfer",
        (input) -> new Transfer(bank, input, new TransferValidator())
    );
    cmds.put(
        "updateuser",
        (input) -> new UpdateUser(bank, input, new UpdateValidator())
    );
    cmds.put(
        "withdraw",
        (input) -> new Withdraw(bank, input, WithdrawValidator())
    );
  }

  @Override
  public void go() {
    Scanner scanner = new Scanner(in);
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] split = line.split("\\s");
      try {
        if (split[0].trim().equalsIgnoreCase("exit")) {
          this.view.displayMessage("Closing down connection to bank: " + this.bank.getRecords());
          break;
        }
        cmds.get(split[0].trim().toLowerCase()).apply(split);
      } catch (IndexOutOfBoundsException e) {
        this.view.displayError(
            "The given command does not have all the required parameters: \n\t" + line
        );
      } catch (NullPointerException | IllegalArgumentException e) {
        this.view.displayError(e);
      }
    }
    scanner.close();
  }
}
