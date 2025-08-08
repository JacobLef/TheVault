package model.security;

/**
 * Provides secure password hashing and verification services. Ensures that user passwords are
 * never stored in plaintext by applying cryptographic hashing algorithms with salt for enhanced
 * security.
 *
 * <p>
 *   All password operations use industry-standard hashing techniques to protect user credentials
 *   from unauthorized access, even in the event of data breaches. The service handles both
 *   password encoding during user registration and password verification during authentication
 *   attempts.
 * </p>
 */
public interface PasswordService {
  /**
   * Securely hashes the given plaintext password using a cryptographic hashing algorithm
   * with automatically generated salt. The resulting hash is safe for database storage.
   *
   * @param plainText The plaintext password to be hashed.
   * @return A salted hash of the password that can be safely stored.
   * @throws IllegalArgumentException if plainText is null or empty.
   * @throws SecurityException if the hashing operation fails.
   */
  String encode(String plainText) throws IllegalArgumentException, SecurityException;

  /**
   * Verifies whether the given plaintext password matches the provided encoded hash.
   * This method safely compares the plaintext against the hash without exposing
   * the original password.
   *
   * @param plainText       The plaintext password to verify.
   * @param encodedPassword The previously hashed password from storage.
   * @return {@code true} if the plaintext matches the encoded password, {@code false} otherwise.
   * @throws IllegalArgumentException if either parameter is null or empty.
   * @throws SecurityException if the verification operation fails.
   */
  boolean verify(
      String plainText,
      String encodedPassword
  ) throws IllegalArgumentException, SecurityException;

  /**
   * Determines whether the given encoded password needs to be rehashed due to
   * security policy changes or algorithm upgrades. This allows for gradual migration
   * to stronger hashing parameters over time.
   *
   * @param encodedPassword The encoded password to evaluate.
   * @return {@code true} if the password should be rehashed with current settings,
   *         {@code false} otherwise
   * @throws IllegalArgumentException if encodedPassword is null or empty.
   */
  boolean needsRehash(String encodedPassword) throws IllegalArgumentException;

  /**
   * Returns information about the hashing algorithm and parameters used by this service.
   * This can be useful for logging, monitoring, or security audits.
   *
   * @return a string describing the current hashing configuration
   */
  String getAlgorithmInfo();
}
