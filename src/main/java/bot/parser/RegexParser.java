package bot.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utilities.Pair;

/**
 * A generic regex-based parser that maps input strings to tagged patterns.
 * This parser allows registration of multiple regex patterns, each associated with a tag,
 * and can parse input strings to identify matching patterns along with their capture groups.
 *
 * <p>The canonical tag type is {@link RegexParser.Tag}, which enumerates all supported
 * bot commands. Callers may still parameterise the parser with any type {@code <T>}.
 *
 * @param <T> The type of tag to associate with each pattern.
 */
public class RegexParser<T> {

    /**
     * Enumeration of all command types the bot parser can recognise.
     * Encapsulated here because the tag set is intrinsic to the parser's domain.
     */
    public enum Tag {

        /** Command to exit the application. */
        BYE,

        /** Command to list all tasks. */
        LIST,

        /** Command to mark a task as completed. */
        MARK,

        /** Command to unmark a task (mark as not completed). */
        UNMARK,

        /** Command to add a new Todo task. */
        TODO,

        /** Command to add a new Deadline task. */
        DEADLINE,

        /** Command to add a new Event task. */
        EVENT,

        /** Command to delete a task. */
        DELETE,

        /** Command to search tasks by keyword. */
        FIND,

        /** Command to display a cheer/motivational message. */
        CHEER,

        /** Command to add tags to a task. */
        TAG,

        /** Command to remove tags from a task. */
        UNTAG,
    }

    /** Map storing the association between regex patterns and their corresponding tags. */
    private final Map<Pattern, T> patternTagMappings = new HashMap<Pattern, T>();

    /**
     * Registers multiple pattern-tag mappings with the parser.
     * Each pattern is associated with a tag that will be returned when input matches that pattern.
     *
     * @param patternTagMappings A map of regex patterns to their associated tags.
     * @throws DuplicatePatternException If any pattern already exists in the parser's mappings.
     */
    public void addPatternTagMappings(Map<Pattern, T> patternTagMappings) throws DuplicatePatternException {
        assert patternTagMappings != null : "Pattern-tag mappings cannot be null";
        for (Map.Entry<Pattern, T> patternTagMapping : patternTagMappings.entrySet()) {
            Pattern pattern = patternTagMapping.getKey();
            T tag = patternTagMapping.getValue();

            assert pattern != null : "Pattern cannot be null";
            assert tag != null : "Tag cannot be null";

            if (this.patternTagMappings.containsKey(pattern)) {
                throw new DuplicatePatternException(
                        "Attempted to add Pattern %s which already exists".formatted(pattern.toString())
                );
            }
            this.patternTagMappings.put(pattern, tag);
        }
    }

    /**
     * Parses the input string against all registered patterns and returns matching results.
     * Each result contains the associated tag and the Matcher object with captured groups.
     *
     * @param inputString The input string to parse.
     * @return A list of pairs containing the matched tag and corresponding Matcher.
     *         Returns an empty list if no patterns match.
     *         May return multiple pairs if the input matches multiple patterns.
     */
    public List<Pair<T, Matcher>> parse(String inputString) {
        assert inputString != null : "Input string cannot be null";
        String normalizedString = inputString.strip();
        ArrayList<Pair<T, Matcher>> results = new ArrayList<Pair<T, Matcher>>();
        for (Map.Entry<Pattern, T> patternTagMapping : this.patternTagMappings.entrySet()) {
            Pattern pattern = patternTagMapping.getKey();
            T tag = patternTagMapping.getValue();

            Matcher matcher = pattern.matcher(normalizedString.strip()); // Normalize

            if (matcher.matches()) {
                results.add(new Pair<T, Matcher>(tag, matcher));
            }
        }
        return results;
    }

}
