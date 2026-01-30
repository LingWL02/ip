package app.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import utilities.Pair;

public class RegexParser<T> {
    private final Map<Pattern, T> patternTagMappings = new HashMap<Pattern, T>();

    public void addPatternTagMappings (Map<Pattern, T> patternTagMappings) throws DuplicatePatternException{
        for (Map.Entry<Pattern, T> patternTagMapping : patternTagMappings.entrySet()) {
            Pattern pattern = patternTagMapping.getKey();
            T tag = patternTagMapping.getValue();

            if (this.patternTagMappings.containsKey(pattern)) {
                throw new DuplicatePatternException(
                        "Attempted to add Pattern %s which already exists".formatted(pattern.toString())
                );
            }
            this.patternTagMappings.put(pattern, tag);
        }
    }

    public List<Pair<T, Matcher>> parse (String inputString) {
        ArrayList<Pair<T, Matcher>> results = new ArrayList<Pair<T, Matcher>>();
        for (Map.Entry<Pattern, T> patternTagMapping : this.patternTagMappings.entrySet()) {
            Pattern pattern = patternTagMapping.getKey();
            T tag = patternTagMapping.getValue();

            Matcher matcher = pattern.matcher(inputString.trim());  // Trim string to normalize

            if (matcher.matches()) {
                results.add(new Pair<T, Matcher>(tag, matcher));
            }
        }
        return results;
    }

}
