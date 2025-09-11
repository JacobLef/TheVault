package view;

import java.util.List;
import java.util.Map;

/**
 * Enhanced view interface for displaying various types of banking information to users.
 * Provides comprehensive output capabilities for CLI-based banking applications including
 * error handling, formatted data display, and user interaction prompts.
 */
public interface View {
  /**
   * Displays an error message to the user.
   * @param message the error message to display
   */
  void displayError(String message);

  /**
   * Displays a general message to the user.
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays a success message with special formatting.
   * @param message the success message to display
   */
  void displaySuccess(String message);

  /**
   * Displays a warning message to the user.
   * @param message the warning message to display
   */
  void displayWarning(String message);

  /**
   * Displays a formatted table of data to the user.
   * @param headers the column headers for the table
   * @param rows the data rows to display
   */
  void displayTable(List<String> headers, List<List<String>> rows);

  /**
   * Displays key-value pairs in a formatted manner.
   * @param data the key-value pairs to display
   */
  void displayKeyValuePairs(Map<String, String> data);

  /**
   * Displays a prompt for user input.
   * @param prompt the prompt message to display
   */
  void displayPrompt(String prompt);

  /**
   * Displays a menu with numbered options.
   * @param title the menu title
   * @param options the list of menu options
   */
  void displayMenu(String title, List<String> options);

  /**
   * Displays a separator line for visual organization.
   */
  void displaySeparator();

  /**
   * Clears the output (if supported by the implementation).
   */
  void clear();

  /**
   * Displays a formatted account summary.
   * @param accountName the name of the account
   * @param balance the current balance
   * @param accountType the type of account
   * @param status the account status
   */
  void displayAccountSummary(String accountName, double balance, String accountType, String status);

  /**
   * Displays a transaction record in a formatted manner.
   * @param fromAccount the source account
   * @param toAccount the destination account
   * @param amount the transaction amount
   * @param type the transaction type
   * @param timestamp the transaction timestamp
   */
  void displayTransaction(String fromAccount, String toAccount, double amount, String type, String timestamp);
}