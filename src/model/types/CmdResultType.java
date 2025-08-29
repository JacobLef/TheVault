package model.types;

/**
 * Represents the type of the result of a command operation which we can have.
 */
public enum CmdResultType {
  NONE,
  ACCOUNT,
  ACCOUNT_LIST,
  BANK_ACCOUNT,
  BANK_ACCOUNT_LIST,
  USER_INFO,
  USER_LOGS,
  BALANCE,
  BOOLEAN_FLAG,
  TRANSACTION,
}
