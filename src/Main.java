import controller.Controller;
import model.Model;
import view.View;

import java.io.IOException;

import static java.lang.System.exit;

public class Main {
  public static void main(String[] args) {
    System.out.println("Banking Database server Starting...");

    Model model = new Bank();
    View view = new View(ReadableModel(model));
    Controller controller = new InteractiveController(model, view);

    try {
      controller.go();
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
      exit(0);
    }
  }
}
