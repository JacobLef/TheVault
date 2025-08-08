package model.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Specific implementation of a PasswordService that uses the *bcyrpt* library to encrypt and
 * decrypt, and salt the passwords given to it.
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
      throw new IllegalArgumentException("costFactor must be between 4 and 20");
    }

    this.costFactor = costFactor;
  }

  @Override
  public String encode(String plainText) throws IllegalArgumentException, SecurityException {
    return "";
  }

  @Override
  public boolean verify(
      String plainText,
      String encodedPassword
  ) throws IllegalArgumentException, SecurityException {
    return false;
  }

  @Override
  public boolean needsRehash(String encodedPassword) throws IllegalArgumentException {
    return false;
  }

  @Override
  public String getAlgorithmInfo() {
    return "";
  }
}
