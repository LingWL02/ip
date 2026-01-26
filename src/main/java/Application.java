import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

public class Application {
    private static final PatternMapping[] patternMappings = {
            new PatternMapping(Pattern.compile("^bye", Pattern.CASE_INSENSITIVE), ParsedType.TERMINATE)
    };

    private final String name;
    private final String separator;

    private final Scanner appScanner = new Scanner(System.in);
    private final List<String> taskList = new ArrayList<String>();

    public Application(String name, String separator) {
        this.name = name;
        this.separator = separator;
    }

    public void run() {
        System.out.printf(
            "%s\nHello! I'm %s!\nWhat can I do for you?\n%s\n\n",this.separator, this.name, this.separator
        );
        while(true) {
            String userInput = this.appScanner.nextLine();
            if (userInput.equals("bye")) break;

            System.out.printf("%s\n%s\n\n", userInput, this.separator);
        }
        System.out.printf("%s\n\nBye. Hope to see you again soon!\n%s", this.separator, this.separator);
    }

    private ParsedResponse parseUserInput(String userInput) {
        userInput = userInput.trim();  // Normalize string
        return new ParsedResponse(ParsedType.TERMINATE, "");
    }

}

enum ParsedType {
    TERMINATE
}

class ParsedResponse {
    private final ParsedType type;
    private final String message;

    public ParsedResponse(ParsedType type, String message) {
        this.type = type;
        this.message = message;
    }

    public ParsedType getType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }
}

class PatternMapping extends Pair<Pattern, ParsedType> {
    public PatternMapping(Pattern pattern, ParsedType parsedType) {
        super(pattern, parsedType);
    }
}

