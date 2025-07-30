package unit;

import model.Bank;
import model.Model;
import model.data_engine.DataBase;
import model.types.AccountStatus;
import model.types.AccountType;
import model.user.BankAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This is a tester class for Bank objects.
 */
class BankTest {
  private Model<BankAccount> modifiableBank;
  private Model<BankAccount> setBank;

  @BeforeEach
  void setUp() {
    this.modifiableBank = new Bank(DataBase::getInstance(), "Modifiable Test Bank");
    this.setBank = new Bank(DataBase::getInstance(), "Set Test Bank");
    this.setBank.makeAccount("First Test Account", AccountType.CheckingAccount, 1000.0);
  }

  @Test
  public void canMakeAccountWithCheckingAndSavings() {
    String accountName  = "TestAccount";
    double amount = 100.0;
    AccountType type = AccountType.CheckingAccount;

    List<BankAccount> currentAccounts = modifiableBank.getAccountsFor(accountName);

    assertEquals(0, currentAccounts.size());

    modifiableBank.makeAccount(accountName, type, amount);
    currentAccounts = modifiableBank.getAccountsFor(accountName);
    assertEquals(1, currentAccounts.size());

    BankAccount bankAccount = currentAccounts.getFirst();
    assertEquals(amount, bankAccount.balance());
    assertEquals(type, bankAccount.type());
    assertEquals(AccountStatus.Free, bankAccount.status());

    double secondAmount = 0.0;
    AccountType secondType = AccountType.SavingsAccount;

    modifiableBank.makeAccount(accountName, secondType, secondAmount);
    currentAccounts = modifiableBank.getAccountsFor(accountName);
    BankAccount first = currentAccounts.getFirst();
    BankAccount second = currentAccounts.get(1);

    assertEquals(first.balance(), bankAccount.balance());
    assertEquals(first.type(), bankAccount.type());
    assertEquals(first.status(), bankAccount.status());

    assertEquals(0.0, second.balance());
    assertEquals(AccountType.SavingsAccount, secondType);
    assertEquals(AccountStatus.Free, second.status());
  }

  @Test
  public void canHoldMoreThanOneUser() {
    String firstName = "TestAccount";
    AccountType firstType = AccountType.CheckingAccount;
    double firstAmount = 100.0;

    String secondName = "TestAccount2";
    AccountType secondType = AccountType.CheckingAccount;
    double secondAmount = 10.0;

    // No accounts to begin with
    assertEquals(0, modifiableBank.getAccountsFor(firstName).size());
    assertEquals(0, modifiableBank.getAccountsFor(secondName).size());

    modifiableBank.makeAccount(firstName, firstType, firstAmount);
    modifiableBank.makeAccount(secondName, secondType, secondAmount);

    BankAccount firstAccount = modifiableBank.getAccountsFor(firstName).getFirst();
    BankAccount secondAccount = modifiableBank.getAccountsFor(secondName).getFirst();

    assertEquals(100.0, firstAccount.balance());
    assertEquals(AccountType.CheckingAccount, firstAccount.type());
    assertEquals(AccountStatus.Free, firstAccount.status());

    assertEquals(10.0, secondAccount.balance());
    assertEquals(AccountType.CheckingAccount, secondAccount.type());
    assertEquals(AccountStatus.Free, secondAccount.status());
  }

  @Test
  public void canWithdrawMoreThanOnce() {
    BankAccount firstAccount = modifiableBank.getAccountsFor("First Account").getFirst();


  }
}