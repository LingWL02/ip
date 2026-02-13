package bot.cheerleader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class Cheerleader {
    private final Random randomNumberGenerator = new Random();
    private final File file;
    private int size;


    public Cheerleader(String filePath) {
        this.file = new File(filePath);
        this.size = 0;
    }


    public void load() throws IOException {
        if (!this.file.getParentFile().exists()) {
            this.file.getParentFile().mkdirs();
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
