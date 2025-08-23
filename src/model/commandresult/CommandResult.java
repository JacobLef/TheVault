package model.commandresult;

/**
 * Represents the outcome of executing a banking command, providing a standardized
 * way to communicate success or failure information back to the calling layer.
 *
 * <p>
 *  CommandResult encapsulates the execution status, any error messages or validation
 *  failures, and optional success data, allowing the controller and view layers to
 *  handle command outcomes uniformly regardless of the specific operation performed.
 * <p>
 *  This design promotes consistent error handling and user feedback across all
 *  banking operations while maintaining loose coupling between the command execution
 *  layer and the presentation layer.
 * </p>
 */
public interface CommandResult {
}
