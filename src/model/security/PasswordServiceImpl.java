package model.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Specific implementation of a PasswordService that uses the *bcyrpt* library to encrypt, salt, and
 * decrypt the passwords given to it.
 */
public class PasswordServiceImpl implements PasswordService {
  private static final int DEFAULT_COST_FACTOR = 12;
  private final int costFactor;

  /**
   * Constructs a new PasswordServiceImpl with the costFactor set to the currently set defaulted
   * cost factor, within this PasswordServiceImpl.
   */
  public PasswordServiceImpl() {
    this(DEFAULT_COST_FACTOR);
  }

  /**
   * Constructs a new PasswordServiceImpl with respect to the given costFactor, so long as it is
   * within the appropriate range for a cost factor.
   * @param costFactor  the cost factor of this PasswordServiceImpl.
   * @throws IllegalStateException if the given costFactor is not in the range: 4 <= c <= 20
   */
  public PasswordServiceImpl(int costFactor) throws IllegalArgumentException {
    if (costFactor < 4 || costFactor > 20) {
      throw new IllegalArgumentException("Cost factor must be between 4 and 20, inclusive.");
    }

    this.costFactor = costFactor;
  }

  @Override
  public String encode(String plainText) throws IllegalArgumentException, SecurityException {
    if (plainText == null) {
      throw new IllegalArgumentException("Plaintext must not be null.");
    }

    if (plainText.isEmpty()) {
      throw new IllegalArgumentException("Plaintext must not be empty.");
    }

    return BCrypt.hashpw(plainText, BCrypt.gensalt(this.costFactor));
  }

  @Override
  public boolean verify(
      String plainText,
      String encodedPassword
  ) throws IllegalArgumentException, SecurityException {
    if (plainText == null || encodedPassword == null || encodedPassword.isEmpty()) {
      throw new IllegalArgumentException("Password and encoded password cannot be null or empty");
    }

    return BCrypt.checkpw(plainText, encodedPassword);
  }

  @Override
  public boolean needsRehash(String encodedPassword) throws IllegalArgumentException {
    if (encodedPassword == null || encodedPassword.isEmpty()) {
      throw new IllegalArgumentException("Encoded password cannot be null or empty");
    }

    String[] parts = encodedPassword.split("\\$");
    if (parts.length >= 3) {
      try {
        int currentCost = Integer.parseInt(parts[2]);
        return currentCost < this.costFactor;
      } catch (NumberFormatException e) {
        return true;
      }
    }
    return true;
  }

  @Override
  public String getAlgorithmInfo() {
    return "BCrypt algorithm with cost factor: " + this.costFactor;
  }
}
