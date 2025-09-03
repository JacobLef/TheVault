package controller;

import model.Model;
import model.commandresult.CmdResult;
import model.commands.GenericCommand;
import model.types.CmdResultType;
import view.View;

import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Function;

/**
 * An individual command-based controller such that it expects singular commands from the user,
 * one by one, rather than an entire file of instructions.
 *
 * <p>
 *   Though usually the Controller is responsible for validation of user input, the Model and the
 *   commands stored within the Model package are responsible for the validation of their
 *   respective input. Thus, any validation errors should be assumed to be the fault of the
 *   respective command, except for white-space removal errors.
 * </p>
 */
public class InteractiveController implements Controller {
  private final InputStream in;
  private final Model model;
  private final View view;
  private static Map<CmdResultType, Function<Object, String>> RES_HANDLERS;

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
    RES_HANDLERS = initializeResultHandlers();
  }

  /**
   * Initializes the map of result handlers for different command result types.
   * @return a Map linking CmdResultType to their respective display handlers
   */
  private static Map<CmdResultType, Function<Object, String>> initializeResultHandlers() {
    Map<CmdResultType, Function<Object, String>> handlers = new HashMap<>();

    handlers.put(CmdResultType.BOOLEAN_FLAG, result -> "Result: " + result.toString());
    handlers.put(CmdResultType.USER_INFO, result -> "User Info: " + result.toString());
    handlers.put(CmdResultType.ACCOUNT, result -> "Account: " + result.toString());
    handlers.put(CmdResultType.ACCOUNT_LIST, result -> "Accounts: " + result.toString());
    handlers.put(CmdResultType.BANK_ACCOUNT, result -> "Bank Account: " + result.toString());
    handlers.put(CmdResultType.BANK_ACCOUNT_LIST, result -> "Bank Accounts: " + result.toString());
    handlers.put(CmdResultType.USER_LOGS, result -> "User Logs: " + result.toString());
    handlers.put(CmdResultType.BALANCE, result -> "Balance: " + result.toString());
    handlers.put(CmdResultType.TRANSACTION, result -> "Transaction: " + result.toString());
    handlers.put(CmdResultType.NONE, result -> "Command executed successfully.");

    return handlers;
  }

  @Override
  public void go() {
    Scanner scanner = new Scanner(in);
    this.view.displayMessage("Banking System Ready. Type 'exit' to quit.");

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      if (line.isEmpty()) {
        continue;
      }
      String[] split = line.split("\\s+");

      try {
        if (split[0].equalsIgnoreCase("exit")) {
          this.view.displayMessage(
              "Closing down connection to bank: " + this.model.
          );
          break;
        }
        CmdResult res = GenericCommand.makeCommand(this.model, split, split[0]).execute();
        handleCommandResult(res);
      } catch (IndexOutOfBoundsException e) {
        this.view.displayError(
            "The given command does not have all the required parameters: \n\t" + line
        );
      } catch (NullPointerException e) {
        if (e.getMessage() != null) {
          this.view.displayError(e.getMessage());
        } else {
          this.view.displayError("Invalid command: " + split[0]);
        }
      } catch (IllegalArgumentException e) {
        this.view.displayError(e.getMessage());
      } catch (Exception e) {
        this.view.displayError("An unexpected error occurred: " + e.getMessage());
      }
    }
    scanner.close();
  }

  /**
   * Handles displaying the result of a command execution based on the result type.
   * @param result The result returned from command execution
   */
  private void handleCommandResult(CmdResult result) {
    if (result == null || !result.hasProperty()) {
      this.view.displayMessage("Command executed successfully.");
      return;
    }

    CmdResultType kind = result.getKind();
    Object resultValue = result.getProperty(Object.class);

    Function<Object, String> handler = RES_HANDLERS.getOrDefault(
        kind,
        obj -> obj != null ? "Result: " + obj : "Command executed successfully."
    );

    String message = handler.apply(resultValue);
    this.view.displayMessage(message);
  }
}