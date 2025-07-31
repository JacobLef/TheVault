package unit;

import model.Bank;
import model.Managed;
import model.data_engine.DataBase;
import model.types.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This is a tester class for Bank objects.
 */
class BankTest {
  private Managed modifiableBank;
  private Managed setBank;

  @BeforeEach
  void setUp() {
    this.modifiableBank = new Bank(DataBase::getInstance(), "Modifiable Test Bank");
    this.setBank = new Bank(DataBase::getInstance(), "Set Test Bank");
    this.setBank.makeAccount("First Test Account", AccountType.CheckingAccount, 1000.0);
  }
}