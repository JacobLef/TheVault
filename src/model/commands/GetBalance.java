package model.commands;

import model.Model;
import model.commandresult.CmdResult;
import model.types.CmdResultType;

import java.util.Map;

/**
 * Represents the ability to fetch the balance of a specified user and an account of theirs, so
 * long as the credentials provided are correct.
 */
public class GetBalance extends GenericCommand {
  /**
   * Constructs a new GetBalance object with respect to the given parameters and initializes the
   * mapping of what flags this class expects to be given within the input and their value's
   * respective types.
   * @param m the model with which this command object should communicate with.
   * @param input the tokenized input from the user.
   */
  public GetBalance(Model m, String[] input) {
    super(m, input);
    this.expectedFlags = Map.of(
        "username", String.class,
        "password", String.class,
        "accountName", String.class
    );
  }

  @Override
  public CmdResult execute() {
    Map<String, String> flags = this.flags();
    this.requireFlags(flags, this.expectedFlags);
    double bal = this.model.getBalance(
        flags.get("username"),
        flags.get("password"),
        flags.get("accountName")
    );
    return this.filledResult(Double.class, bal, CmdResultType.BALANCE);
  }

}
