package tim.exception;

/**
 * Represents exceptions specific to the Duke chatbot application.
 * This exception is thrown when an error occurs in processing user commands,
 * loading or saving tasks, or parsing input.
 *
 */
public class TimException extends Exception {
    /**
     * Constructs a new DukeException with the specified detail message.
     *
     * @param message the detail message describing the error.
     */
    public TimException(String message) {
        super(message);
    }

    /**
     * Constructs a new TimException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause of this exception.
     */
    public TimException(String message, Throwable cause) {
        super(message, cause);
    }
}
