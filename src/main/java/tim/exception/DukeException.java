package tim.exception;

/**
 * Represents exceptions specific to the Duke chatbot application.
 * This exception is thrown when an error occurs in processing user commands,
 * loading or saving tasks, or parsing input.
 *
 */
public class DukeException extends Exception{
    /**
     * Constructs a new DukeException with the specified detail message.
     *
     * @param message the detail message describing the error.
     */
    public DukeException(String message) {
        super(message);
    }
}
