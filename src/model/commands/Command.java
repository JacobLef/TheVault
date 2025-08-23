package model.commands;

/**
 * Represents an executable banking operation that encapsulates both the business logic
 * and input validation for a specific command. Commands follow the Command Pattern to
 * decouple the request for an operation from the object that performs it, enabling
 * flexible dispatch, consistent error handling, and clean separation between
 * user interface concerns and business logic.
 *
 * <p>
 *  Each Command implementation is responsible for validating the provided request data using its
 *  associated InputValidator, executing the core business operation if validation passes,
 *  returning a standardized CommandResult indicating success or failure, and ensuring proper
 *  error handling and logging throughout the operation. This design allows the controller layer
 *  to dispatch operations uniformly without needing to know the specific validation rules or
 *  execution logic for each banking operation, promoting maintainability and extensibility.
 * </p>
 *
 */
public interface Command {
  /**
   * Execute this Command such that the Model now reflects the changes requested by this Commands
   * functionality. This method assumes that the information stored within this Command is valid
   * and thus does not check for input validity before performing executions.
   */
  void execute();
}
