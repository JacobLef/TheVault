package unit;

import model.security.PasswordService;
import model.security.PasswordServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is a tester class for PasswordServiceImpl objects.
 */
class PasswordServiceImplTest {
  private PasswordService configService;

  @BeforeEach
  void setUp() {
    this.configService = new PasswordServiceImpl(4);
  }

  @Test
  public void defaultedCostServiceIsTwelve() {
    PasswordService defaultPs = new PasswordServiceImpl();
    String desc = defaultPs.getAlgorithmInfo();

    assertTrue(desc.contains("12"));
    assertTrue(desc.contains("BCrypt"));
  }

  @Test
  public void costFactorIsNotModifiedAfterCreation() {
    String desc = this.configService.getAlgorithmInfo();
    assertTrue(desc.contains("4"));

    PasswordService newService = new PasswordServiceImpl(19);
    desc = newService.getAlgorithmInfo();
    assertTrue(desc.contains("19"));
  }

  @Test
  public void canHaveCostFactorWithinRangeInclusive() {
    List<Integer> costFactors = new ArrayList<>();
    for (int i = 4; i <= 20; i++) {
      costFactors.add(i);
    }

    for (int costFactor : costFactors) {
      assertDoesNotThrow(() -> new PasswordServiceImpl(costFactor));
    }
  }

  @Test
  public void cannotHaveCostFactorNotWithinRangeInclusive() {
    List<Integer> invalidCostFactors = new ArrayList<>();
    for (int i = -5; i < 4; i++) {
      invalidCostFactors.add(i);
    }
    for (int i = 21; i <= 40; i++) {
      invalidCostFactors.add(i);
    }

    for (int costFactor : invalidCostFactors) {
      assertThrows(IllegalArgumentException.class, () -> new PasswordServiceImpl(costFactor));
    }
  }

  @Test
  public void encodeProducesValidBCryptFormat() {
    String hash = configService.encode("testPassword");

    assertTrue(hash.startsWith("$2a$"));
    assertEquals(60, hash.length());
    assertTrue(hash.contains("$04$"));
  }

  @Test
  public void encodeSamePasswordProducesDifferentHashes() {
    String password = "samePassword";
    String hash1 = configService.encode(password);
    String hash2 = configService.encode(password);

    assertNotEquals(hash1, hash2);
    assertTrue(hash1.startsWith("$2a$"));
    assertTrue(hash2.startsWith("$2a$"));
  }

  @Test
  public void encodeThrowsExceptionForNullInput() {
    assertThrows(IllegalArgumentException.class, () -> {
      configService.encode(null);
    });
  }

  @Test
  public void encodeThrowsExceptionForEmptyInput() {
    assertThrows(IllegalArgumentException.class, () -> {
      configService.encode("");
    });
  }

  @Test
  public void encodeHandlesDifferentPasswordLengths() {
    assertDoesNotThrow(() -> configService.encode("a"));
    assertDoesNotThrow(() -> configService.encode("short"));
    assertDoesNotThrow(() -> configService.encode("very_long_password_with_special_chars_123!@#"));

    String shortHash = configService.encode("abc");
    String longHash = configService.encode("very_long_password");

    assertEquals(60, shortHash.length());
    assertEquals(60, longHash.length());
  }

  @Test
  public void encodeUsesCostFactorFromConstructor() {
    PasswordService lowCost = new PasswordServiceImpl(4);
    PasswordService highCost = new PasswordServiceImpl(10);

    String lowCostHash = lowCost.encode("password");
    String highCostHash = highCost.encode("password");

    assertTrue(lowCostHash.contains("$04$"));
    assertTrue(highCostHash.contains("$10$"));
  }

  @Test
  public void encodeWithDifferentInputsProducesDifferentHashes() {
    String hash1 = configService.encode("password1");
    String hash2 = configService.encode("password2");
    String hash3 = configService.encode("password1");

    assertNotEquals(hash1, hash2);
    assertNotEquals(hash1, hash3);
    assertFalse(hash1.contains("password1"));
    assertFalse(hash2.contains("password2"));
  }

  @Test
  public void verifyReturnsTrueForCorrectPassword() {
    String plaintext = "correctPassword";
    String hash = configService.encode(plaintext);

    assertTrue(configService.verify(plaintext, hash));
  }

  @Test
  public void verifyReturnsFalseForIncorrectPassword() {
    String plaintext = "correctPassword";
    String wrongPassword = "wrongPassword";
    String hash = configService.encode(plaintext);

    assertFalse(configService.verify(wrongPassword, hash));
  }

  @Test
  public void verifyReturnsFalseForCompletelyDifferentPasswords() {
    String hash = configService.encode("password123");

    assertFalse(configService.verify("different", hash));
    assertFalse(configService.verify("Password123", hash));
    assertFalse(configService.verify("password124", hash));
    assertFalse(configService.verify("", hash));
  }

  @Test
  public void verifyThrowsExceptionForNullPlaintext() {
    String hash = configService.encode("password");

    assertThrows(IllegalArgumentException.class, () -> {
      configService.verify(null, hash);
    });
  }

  @Test
  public void verifyThrowsExceptionForNullHash() {
    assertThrows(IllegalArgumentException.class, () -> {
      configService.verify("password", null);
    });
  }

  @Test
  public void verifyThrowsExceptionForEmptyHash() {
    assertThrows(IllegalArgumentException.class, () -> {
      configService.verify("password", "");
    });
  }

  @Test
  public void verifyHandlesSpecialCharactersInPassword() {
    String specialPassword = "p@ssw0rd!#$%^&*()";
    String hash = configService.encode(specialPassword);

    assertTrue(configService.verify(specialPassword, hash));
    assertTrue(configService.verify("p@ssw0rd!#$%^&*()", hash));
  }

  @Test
  public void verifyHandlesUnicodeCharacters() {
    String unicodePassword = "пароль123";
    String hash = configService.encode(unicodePassword);

    assertTrue(configService.verify(unicodePassword, hash));
    assertFalse(configService.verify("пароль124", hash));
  }

  @Test
  public void verifyHandlesVeryLongPasswords() {
    String longPassword = "a".repeat(200);
    String hash = configService.encode(longPassword);

    assertTrue(configService.verify(longPassword, hash));
    // Since BCrypt truncates to 72 bytes, this is the same as a 200 count
    assertTrue(configService.verify("a".repeat(199), hash));

    assertFalse(configService.verify("b" + "a".repeat(199), hash));
  }

  @Test
  public void verifyReturnsFalseForMalformedHash() {
    assertFalse(configService.verify("password", "invalid_hash"));
    assertFalse(configService.verify("password", "$2a$invalid"));
    assertFalse(configService.verify("password", "not_bcrypt_format"));
  }

  @Test
  public void verifyWithDifferentCostFactors() {
    PasswordService lowCost = new PasswordServiceImpl(4);
    PasswordService highCost = new PasswordServiceImpl(12);

    String password = "testPassword";
    String lowCostHash = lowCost.encode(password);
    String highCostHash = highCost.encode(password);

    assertTrue(lowCost.verify(password, lowCostHash));
    assertTrue(highCost.verify(password, highCostHash));
    assertTrue(lowCost.verify(password, highCostHash));
    assertTrue(highCost.verify(password, lowCostHash));
  }

  @Test
  public void needsRehashReturnsTrueForLowerCostFactor() {
    PasswordService lowCostService = new PasswordServiceImpl(4);
    PasswordService currentService = new PasswordServiceImpl(8);

    String lowCostHash = lowCostService.encode("password");

    assertTrue(currentService.needsRehash(lowCostHash));
  }

  @Test
  public void needsRehashReturnsFalseForSameCostFactor() {
    String hash = configService.encode("password");

    assertFalse(configService.needsRehash(hash));
  }

  @Test
  public void needsRehashReturnsFalseForHigherCostFactor() {
    PasswordService lowCostService = new PasswordServiceImpl(4);
    PasswordService highCostService = new PasswordServiceImpl(8);

    String highCostHash = highCostService.encode("password");

    assertFalse(lowCostService.needsRehash(highCostHash));
  }

  @Test
  public void needsRehashThrowsExceptionForNullHash() {
    assertThrows(IllegalArgumentException.class, () -> {
      configService.needsRehash(null);
    });
  }

  @Test
  public void needsRehashThrowsExceptionForEmptyHash() {
    assertThrows(IllegalArgumentException.class, () -> {
      configService.needsRehash("");
    });
  }

  @Test
  public void needsRehashReturnsTrueForMalformedHash() {
    assertTrue(configService.needsRehash("invalid_hash"));
    assertTrue(configService.needsRehash("$2a$invalid"));
    assertTrue(configService.needsRehash("not_bcrypt"));
    assertTrue(configService.needsRehash("$2a$"));
    assertTrue(configService.needsRehash("$2a$xx$"));
  }

  @Test
  public void needsRehashHandlesDifferentBCryptVersions() {
    PasswordService highCostService = new PasswordServiceImpl(8);
    assertTrue(highCostService.needsRehash("$2$04$validbcrypthashbutoldversion"));
    assertTrue(highCostService.needsRehash("$2b$04$differentbcryptvariant"));
  }

  @Test
  public void needsRehashWithVariousCostFactors() {
    PasswordService service10 = new PasswordServiceImpl(10);

    assertTrue(service10.needsRehash("$2a$04$lowcosthash"));
    assertTrue(service10.needsRehash("$2a$08$mediumcosthash"));
    assertFalse(service10.needsRehash("$2a$10$samecosthash"));
    assertFalse(service10.needsRehash("$2a$12$highercosthash"));
  }

  @Test
  public void needsRehashHandlesUnparseableCost() {
    assertTrue(configService.needsRehash("$2a$ab$invalidcostfactor"));
    assertTrue(configService.needsRehash("$2a$$nocostfactor"));
    assertTrue(configService.needsRehash("$2a$999$toohighcost"));
  }

  @Test
  public void needsRehashWithEdgeCaseBCryptFormats() {
    assertTrue(configService.needsRehash("$"));
    assertTrue(configService.needsRehash("$$"));
    assertTrue(configService.needsRehash("$2a"));
    assertTrue(configService.needsRehash("$2a$"));
    assertTrue(configService.needsRehash("$2a$03$"));
  }

  @Test
  public void getAlgorithmInfoReturnsCorrectInformation() {
    String info = configService.getAlgorithmInfo();
    assertTrue(info.contains("BCrypt"));
    assertTrue(info.contains("4"));

    PasswordService service12 = new PasswordServiceImpl(12);
    String info12 = service12.getAlgorithmInfo();
    assertTrue(info12.contains("12"));
  }

  @Test
  public void verifyHandlesEmptyStringPassword() {
    String hash = configService.encode("actualPassword");
    assertFalse(configService.verify("", hash));
  }

  @Test
  public void encodeHandlesSpecialCharacters() {
    String specialPassword = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    String hash = configService.encode(specialPassword);

    assertTrue(hash.startsWith("$2a$"));
    assertEquals(60, hash.length());
    assertTrue(configService.verify(specialPassword, hash));
  }
}