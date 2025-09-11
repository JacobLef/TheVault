package controller;

import model.Model;
import model.commandresult.CmdResult;
import model.commands.GenericCommand;
import model.types.CmdResultType;
import model.user.BankAccount;
import model.user.Transaction;
import model.user.User;
import view.View;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * An individual command-based controller that expects singular commands from the user,
 * one by one, rather than an entire file of instructions. Utilizes an enhanced view
 * interface to provide formatted output for banking operations.
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
  private final String promptPrefix;

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
    this.promptPrefix = "$TheVault";
  }

  @Override
  public void go() {
    Scanner scanner = new Scanner(in);

    displayWelcomeScreen();
    this.view.displayPrompt(promptPrefix + " >");

    while (scanner.hasNextLine()) {
      this.view.displayPrompt(promptPrefix + " >");
      String line = scanner.nextLine().trim();

      if (line.isEmpty()) {
        continue;
      }

      String[] split = line.split("\\s+");

      try {
        if (split[0].equalsIgnoreCase("exit")) {
          this.view.displayMessage(
              "Closing connection to bank: " + this.model.getActiveBank() + "..."
          );
          this.view.displaySuccess("Goodbye!");
          break;
        }

        if (split[0].equalsIgnoreCase("help")) {
          displayHelpMenu();
          continue;
        }

        if (split[0].equalsIgnoreCase("menu")) {
          displayWelcomeScreen();
          continue;
        }

        CmdResult res = GenericCommand.makeCommand(this.model, split, split[0]).execute();
        handleCommandResult(res);

      } catch (IndexOutOfBoundsException e) {
        this.view.displayError("Missing required parameters for command: " + split[0]);
        this.view.displayMessage("Type 'help' to see command syntax.");
      } catch (NullPointerException e) {
        if (e.getMessage() != null) {
          this.view.displayError(e.getMessage());
        } else {
          this.view.displayError("Invalid command: " + split[0]);
        }
      } catch (IllegalArgumentException e) {
        this.view.displayError(e.getMessage());
      } catch (Exception e) {
        this.view.displayError("Unexpected error: " + e.getMessage());
      }
    }
    scanner.close();
  }

  /**
   * Displays the welcome screen with main menu options for the banking system.
   */
  private void displayWelcomeScreen() {
    this.view.clear();
    this.view.displayMenu("Banking System - Main Menu", List.of(
        "User Management",
        "Account Operations",
        "Transaction Services",
        "Account Information",
        "System Commands"
    ));

    this.view.displayMessage("Type 'help' for detailed commands or 'menu' to redisplay this menu.");
    this.view.displaySeparator();
  }

  /**
   * Displays a comprehensive help menu with all available commands organized by category.
   */
  private void displayHelpMenu() {
    this.view.displayMenu("User Management Commands", List.of(
        "createuser <username> <password> <email>",
        "updateuser <username> <password> <property> <newvalue>",
        "deleteuser <username> <password>",
        "getuser <username> <password>"
    ));

    this.view.displayMenu("Account Operations", List.of(
        "createaccount <username> <password> <accountname> <type> [initialbalance]",
        "deleteaccount <username> <password> <accountname>",
        "deposit <username> <password> <accountname> <amount>",
        "withdraw <username> <password> <accountname> <amount>"
    ));

    this.view.displayMenu("Transaction Services", List.of(
        "transfer <fromuser> <touser> <frompass> <topass> <fromaccount> <toaccount> <amount>"
    ));

    this.view.displayMenu("Account Information", List.of(
        "balance <username> <password> <accountname>",
        "accounts <username> <password>",
        "account <username> <password> <accountname>"
    ));

    this.view.displayMenu("System Commands", List.of(
        "help - show this help menu",
        "menu - return to main menu",
        "exit - quit the application"
    ));
  }

  /**
   * Handles displaying the result of a command execution based on the result type.
   * Uses the enhanced view capabilities to provide formatted output for different data types.
   * @param result The result returned from command execution
   */
  private void handleCommandResult(CmdResult result) {
    if (result == null || !result.hasProperty()) {
      this.view.displaySuccess("Command executed successfully.");
      return;
    }

    CmdResultType kind = result.getKind();
    Object resultValue = result.getProperty(Object.class);

    switch (kind) {
      case BOOLEAN_FLAG -> {
        boolean flag = (Boolean) resultValue;
        if (flag) {
          this.view.displaySuccess("Operation completed successfully.");
        } else {
          this.view.displayWarning("Operation completed with warnings.");
        }
      }
      case USER_INFO -> {
        if (resultValue instanceof User user) {
          Map<String, String> userInfo = Map.of(
              "Username", user.username(),
              "Email", user.email(),
              "Created", user.createdAt() != null ? user.createdAt().toString() : "N/A"
          );
          this.view.displayKeyValuePairs(userInfo);
        } else {
          this.view.displayMessage("User: " + resultValue.toString());
        }
      }
      case ACCOUNT, BANK_ACCOUNT -> {
        if (resultValue instanceof BankAccount account) {
          this.view.displayAccountSummary(
              account.accountName(),
              account.balance(),
              account.type().toString(),
              account.status().toString()
          );
        } else {
          this.view.displayMessage("Account: " + resultValue.toString());
        }
      }
      case ACCOUNT_LIST, BANK_ACCOUNT_LIST -> {
        if (resultValue instanceof List<?> accounts) {
          displayAccountList(accounts);
        } else {
          this.view.displayMessage("Accounts: " + resultValue.toString());
        }
      }
      case BALANCE -> {
        double balance = (Double) resultValue;
        this.view.displayMessage("Current Balance: $" + String.format("%.2f", balance));
      }
      case TRANSACTION -> {
        if (resultValue instanceof Transaction transaction) {
          this.view.displayTransaction(
              transaction.fromAccountName(),
              transaction.toAccountName(),
              transaction.amount(),
              transaction.type().toString(),
              transaction.createdAt() != null ? transaction.createdAt().toString() : "N/A"
          );
        } else {
          this.view.displayMessage("Transaction: " + resultValue.toString());
        }
      }
      case USER_LOGS -> {
        this.view.displayMessage("User deletion completed. All associated data removed.");
      }
      case NONE -> this.view.displaySuccess("Command executed successfully.");
      default -> this.view.displayMessage("Result: " + resultValue);
    }
  }

  /**
   * Displays a list of accounts in a formatted table.
   * @param accounts the list of accounts to display
   */
  private void displayAccountList(List<?> accounts) {
    if (accounts.isEmpty()) {
      this.view.displayMessage("No accounts found.");
      return;
    }

    List<String> headers = List.of("Account Name", "Balance", "Type", "Status");
    List<List<String>> rows = accounts.stream()
        .filter(BankAccount.class::isInstance)
        .map(BankAccount.class::cast)
        .map(account -> List.of(
            account.accountName(),
            String.format("$%.2f", account.balance()),
            account.type().toString(),
            account.status().toString()
        ))
        .toList();

    this.view.displayTable(headers, rows);
  }
}