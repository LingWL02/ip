package bot.cheerleader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * A cheerleader that provides random cheerful messages from a file.
 */
public class Cheerleader {
    private final Random randomNumberGenerator = new Random();
    private final File file;
    private int size;


    /**
     * Constructs a Cheerleader with the specified file path.
     *
     * @param filePath the path to the file containing cheer messages
     */
    public Cheerleader(String filePath) {
        this.file = new File(filePath);
        this.size = 0;
    }


    /**
     * Loads the cheer messages from the file and counts them.
     *
     * @throws IOException if an I/O error occurs
     */
    public void load() throws IOException {
        File parentDir = this.file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (!this.file.createNewFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    this.size++;
                }
            }
        }
    }


    /**
     * Returns a random cheer message from the loaded file.
     *
     * @return a random cheer message, or a default message if no cheers are available
     * @throws IOException if an I/O error occurs
     */
    public String cheer() throws IOException {
        if (this.size == 0) {
            return "No cheer for you :(";
        }
        int index = this.randomNumberGenerator.nextInt(this.size);
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            String line;
            int currentIndex = 0;
            while ((line = reader.readLine()) != null) {
                if (currentIndex == index) {
                    return line;
                }
                currentIndex++;
            }
        }
        return "Cheerleader is malfunctioning...";
    }
}
