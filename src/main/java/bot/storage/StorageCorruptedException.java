package bot.storage;

import java.io.IOException;

/**
 * Exception thrown when the storage file contains malformed or unrecognisable content.
 * This can occur if the file was manually edited, partially written, or contains
 * entries from an incompatible version of the application.
 */
public class StorageCorruptedException extends IOException {

    /**
     * Constructs a new StorageCorruptedException with the specified detail message.
     *
     * @param message A description of what corruption was detected.
     */
    public StorageCorruptedException(String message) {
        super(message);
    }
}
