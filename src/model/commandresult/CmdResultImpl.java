package model.commandresult;

import java.util.Objects;

/**
 * Default implementation of {@link CmdResult} that stores a single property of any type
 * using the Type-Safe Heterogeneous Container pattern. This implementation maintains
 * the stored property and its runtime type information to enable safe retrieval via
 * type tokens, returning {@code null} when no property has been set or when the
 * requested type does not match the stored property type.
 */
public class CmdResultImpl implements CmdResult {
  private final Object prop;
  private final Class<?> type;

  /**
   * Builder class for CmdResultImpl objects.
   */
  public static class Builder {
    private Object prop;
    private Class<?> type;

    /**
     * Constructs a new Builder object with default values of {@code null} for all fields.
     */
    public Builder() {
      this.prop = null;
      this.type = null;
    }

    /**
     * Sets the {@code prop} field of this builder with the respective given prop value.
     * @param prop the new property of this Builder.
     * @return this Builder.
     */
    public Builder withProp(Object prop) {
      this.prop = prop;
      return this;
    }

    /**
     * Set the {@code type} field of this builder with the respective given type value;
     * @param type the new type property of this Builder.
     * @return this Builder.
     */
    public Builder withType(Class<?> type) {
      this.type = type;
      return this;
    }

    /**
     * Constructs a new CmdResult object whose fields mimic those of this Builder.
     * @return the newly created CmdResult object.
     * @throws IllegalStateException if one of the fields is not {@code null} and the other is.
     */
    public CmdResult build() throws IllegalStateException {
      if ((this.prop == null && this.type != null) || (this.prop != null && this.type == null)) {
        throw new IllegalStateException(
          "Cannot build a CmdResult with a null prop and non-null type or with a "
          + "null type and a non-null prop!"
        );
      }
      return new CmdResultImpl(prop, type);
    }
  }

  /**
   * Constructs a new CmdResultImpl with {@code null} fields. Is public so that other classes
   * have a quick way to indicate the absence of any result.
   */
  public CmdResultImpl() {
    this.prop = null;
    this.type = null;
  }

  /**
   * Constructs a new CmdResultImpl object with respect to the given property and type.
   * @param prop the property of this Builder.
   * @param type the internal type of this Builder which must match with the external type given
   *             when the getProperty method is invoked.
   */
  private CmdResultImpl(Object prop, Class<?> type) {
    this.prop = prop;
    this.type = type;
  }

  @Override
  public <T> T getProperty(Class<T> externalType) throws ClassCastException {
    if (!this.hasProperty() || !Objects.equals(type, externalType)) {
      return null;
    }

    return externalType.cast(prop);
  }

  @Override
  public boolean hasProperty() {
    return this.prop == null;
  }
}
