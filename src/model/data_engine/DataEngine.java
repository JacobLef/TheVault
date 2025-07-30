package model.data_engine;

/**
 * Serves as the primary host of Data within the Banking application, such that all data-related
 * actions ultimately are performed on the data stored within a DataEngine object.
 *
 * The DataEngine follows a relational database design, such that it relies on SQL-like tables
 * and queries that are managed by the implementor.
 */
public interface DataEngine {
  DataEngine getInstance();
}

