package model.commands;

import model.commandresult.CommandResult;

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
 * @param <T> The type of request object this command operates on
 */
public interface Command {
  CommandResult execute();
}
