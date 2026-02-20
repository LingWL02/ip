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
 * @param <T> The type of tag to associate with each pattern (e.g., ParserTag enum).
 */
public class RegexParser<T> {

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
