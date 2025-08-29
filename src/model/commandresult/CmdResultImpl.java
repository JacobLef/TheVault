package model.commandresult;

import model.types.CmdResultType;

import java.util.Objects;

/**
 * Implementation of {@link CmdResult} using the Type-Safe Heterogeneous Container pattern.
 * Stores a single property with its type information for safe retrieval via type tokens.
 * Prefers static factory methods over builder pattern for simplicity and type safety.
 */
public class CmdResultImpl implements CmdResult {
  private final Object prop;
  private final Class<?> type;
  private final CmdResultType kind;

  /**
   * Private constructor for internal use.
   */
  private CmdResultImpl(Object prop, Class<?> type, CmdResultType kind) {
    this.prop = prop;
    this.type = type;
    this.kind = kind;
  }

  /**
   * Creates a CmdResult containing the specified property with compile-time type safety.
   * @param type the Class token for the property type
   * @param value the value to store (can be null)
   * @param kind the kind of data which is being stored within this CmdResultImpl.
   * @param <T> the type of the property
   * @return a new CmdResultImpl containing the property
   */
  public static <T> CmdResultImpl of(Class<T> type, T value, CmdResultType kind) {
    Objects.requireNonNull(type, "Type cannot be null");
    return new CmdResultImpl(value, type, kind);
  }

  /**
   * Creates an empty CmdResult with no property.
   * @return a new empty CmdResultImpl
   */
  public static CmdResultImpl empty() {
    return new CmdResultImpl(null, null, CmdResultType.NONE);
  }

  @Override
  public <T> T getProperty(Class<T> externalType) {
    Objects.requireNonNull(externalType, "Type cannot be null");

    if (!hasProperty() || !Objects.equals(type, externalType)) {
      return null;
    }

    return externalType.cast(prop);
  }

  @Override
  public CmdResultType getKind() {
    return this.kind;
  }

  @Override
  public boolean hasProperty() {
    return prop != null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;

    CmdResultImpl that = (CmdResultImpl) obj;
    return Objects.equals(prop, that.prop) && Objects.equals(type, that.type)
        && Objects.equals(kind, that.kind);
  }

  @Override
  public int hashCode() {
    return Objects.hash(prop, type);
  }

  @Override
  public String toString() {
    if (!hasProperty()) {
      return "CmdResultImpl{empty}";
    }
    return String.format("CmdResultImpl{prop=%s, type=%s}", prop, type.getSimpleName());
  }
}