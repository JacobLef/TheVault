package model;
/**
 * Serves as the primary host of data for the banking application, acting as a singular bank.
 *
 * <p>
 *  A Managed Model offers functionality for CRUD operations which can be acted upon on the
 *  data within this Model or regards to the Data to which this Model otherwise has access to.
 *  This includes both CRUD operations on a User and CRUD operations on the accounts which
 *  a User has opened.
 * <p>
 *
 * <p>
 *   A Managed Model offers no functionality that differs from a Model. The purpose of interface
 *   segregation in this case is to distinguish between something which can be managed and
 *   something which Manages those objects that can be Managed.
 * </p>
 */
public interface Managed extends Model { }
