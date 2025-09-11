package view;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * CLI implementation of the enhanced view interface providing comprehensive
 * output formatting for banking operations and user interactions.
 */
public class CLIView implements View {
  private final Appendable out;

  public CLIView(Appendable out) {
    this.out = out;
  }

  @Override
  public void displayError(String message) {
    safeAppend("ERROR: " + message + System.lineSeparator());
  }

  @Override
  public void displayMessage(String message) {
    safeAppend(message + System.lineSeparator());
  }

  @Override
  public void displaySuccess(String message) {
    safeAppend("SUCCESS: " + message + System.lineSeparator());
  }

  @Override
  public void displayWarning(String message) {
    safeAppend("WARNING: " + message + System.lineSeparator());
  }

  @Override
  public void displayTable(List<String> headers, List<List<String>> rows) {
    if (headers.isEmpty()) return;

    // Calculate column widths
    int[] columnWidths = new int[headers.size()];
    for (int i = 0; i < headers.size(); i++) {
      columnWidths[i] = headers.get(i).length();
    }

    for (List<String> row : rows) {
      for (int i = 0; i < Math.min(row.size(), columnWidths.length); i++) {
        columnWidths[i] = Math.max(columnWidths[i], row.get(i).length());
      }
    }

    // Display header
    displayTableRow(headers, columnWidths);
    displayTableSeparator(columnWidths);

    // Display data rows
    for (List<String> row : rows) {
      displayTableRow(row, columnWidths);
    }
    safeAppend(System.lineSeparator());
  }


  @Override
  public void displayKeyValuePairs(Map<String, String> data) {
    int maxKeyLength = data.keySet().stream().mapToInt(String::length).max().orElse(0);

    for (Map.Entry<String, String> entry : data.entrySet()) {
      String formattedKey = String.format("%-" + maxKeyLength + "s", entry.getKey());
      safeAppend(formattedKey + ": " + entry.getValue() + System.lineSeparator());
    }
    safeAppend(System.lineSeparator());
  }

  @Override
  public void displayPrompt(String prompt) {
    safeAppend(prompt + ": ");
  }

  @Override
  public void displayMenu(String title, List<String> options) {
    safeAppend(title + System.lineSeparator());
    displaySeparator();

    for (int i = 0; i < options.size(); i++) {
      safeAppend((i + 1) + ". " + options.get(i) + System.lineSeparator());
    }
    safeAppend(System.lineSeparator());
  }

  @Override
  public void displaySeparator() {
    safeAppend("----------------------------------------" + System.lineSeparator());
  }

  @Override
  public void clear() {
    // For CLI, we'll just add some newlines to simulate clearing
    safeAppend(System.lineSeparator().repeat(50));
  }

  @Override
  public void displayAccountSummary(String accountName, double balance, String accountType, String status) {
    safeAppend("Account Summary:" + System.lineSeparator());
    safeAppend("  Name: " + accountName + System.lineSeparator());
    safeAppend("  Balance: $" + String.format("%.2f", balance) + System.lineSeparator());
    safeAppend("  Type: " + accountType + System.lineSeparator());
    safeAppend("  Status: " + status + System.lineSeparator());
    safeAppend(System.lineSeparator());
  }

  @Override
  public void displayTransaction(String fromAccount, String toAccount, double amount, String type, String timestamp) {
    safeAppend("Transaction:" + System.lineSeparator());
    safeAppend("  From: " + fromAccount + System.lineSeparator());
    safeAppend("  To: " + toAccount + System.lineSeparator());
    safeAppend("  Amount: $" + String.format("%.2f", amount) + System.lineSeparator());
    safeAppend("  Type: " + type + System.lineSeparator());
    safeAppend("  Time: " + timestamp + System.lineSeparator());
    safeAppend(System.lineSeparator());
  }

  /**
   * Helper method to safely append text to the output stream with proper error handling.
   * @param text the text to append
   */
  private void safeAppend(String text) {
    try {
      out.append(text);
    } catch (IOException e) {
      System.err.println("Failed to write to output: " + e.getMessage());
    }
  }

  /**
   * Displays a table row with proper column formatting.
   * @param data the row data to display
   * @param columnWidths the width of each column for alignment
   */
  private void displayTableRow(List<String> data, int[] columnWidths) {
    StringBuilder row = new StringBuilder("| ");

    for (int i = 0; i < data.size() && i < columnWidths.length; i++) {
      String cell = String.format("%-" + columnWidths[i] + "s", data.get(i));
      row.append(cell).append(" | ");
    }

    safeAppend(row.toString() + System.lineSeparator());
  }

  /**
   * Displays a separator line for table formatting.
   * @param columnWidths the width of each column
   */
  private void displayTableSeparator(int[] columnWidths) {
    StringBuilder separator = new StringBuilder("|");

    for (int width : columnWidths) {
      separator.append("-".repeat(width + 2)).append("|");
    }

    safeAppend(separator.toString() + System.lineSeparator());
  }
}