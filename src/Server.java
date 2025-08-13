import controller.Controller;
import controller.InteractiveController;
import model.Bank;
import model.Model;
import model.data_engine.DataBase;
import model.security.PasswordServiceImpl;
import view.CLIView;
import view.View;

import java.io.IOException;

import static java.lang.System.exit;

public class Server {
  public static void main(String[] args) {
    System.out.println("Banking Database server Starting...");

    Model model = new Bank(
        DataBase.getInstance(),
        new PasswordServiceImpl(),
        "Bank One",
        1984
    );

    View view = new CLIView(model, System.out);
    Controller controller = new InteractiveController(System.in, model, view);

    try {
      controller.go();
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
      exit(1);
    }
  }
}
