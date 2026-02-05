package app.parser;

/**
 * Exception thrown when attempting to register a regex pattern that already exists
 * in the parser's pattern mappings.
 * This prevents duplicate pattern registrations which could cause ambiguous parsing behavior.
 */
public class DuplicatePatternException extends Exception {

    /**
     * Constructs a new DuplicatePatternException with the specified detail message.
     *
     * @param message The detail message explaining which pattern was duplicated.
     */
    public DuplicatePatternException(String message) {
        super(message);
    }
}
