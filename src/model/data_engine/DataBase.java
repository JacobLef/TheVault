package model.data_engine;

public class DataBase implements DataEngine {
  private static DataBase instance;

  private DataBase() {}

  /**
   * Gets the one instance of this DataBase such that only one object can ever exist (singleton).
   * @return the one DataBase instance stored within this DataBase.
   */
  public static DataBase getInstance() {
    if (instance == null) {
      instance = new DataBase();
    }

    return instance;
  }
}