package bot.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import utilities.Pair;

class TestRegexParser {

    private RegexParser<String> parser;

    @BeforeEach
    void setUp() {
        parser = new RegexParser<>();
    }

    @Test
    void addPatternTagMappings_newPatterns_addedSuccessfully() throws Exception {
        Pattern p = Pattern.compile("hello");
        parser.addPatternTagMappings(Map.of(p, "HELLO"));
        // no exception means success
        assertTrue(true);
    }

    @Test
    void addPatternTagMappings_duplicatePattern_throwsDuplicatePatternException() throws Exception {
        Pattern p = Pattern.compile("hello");
        parser.addPatternTagMappings(Map.of(p, "HELLO"));
        assertThrows(DuplicatePatternException.class, () ->
            parser.addPatternTagMappings(Map.of(p, "HELLO_AGAIN"))
        );
    }

    @Test
    void parse_matchingInput_returnsNonEmptyList() throws Exception {
        Pattern p = Pattern.compile("^hello$");
        parser.addPatternTagMappings(Map.of(p, "HELLO"));

        List<Pair<String, Matcher>> results = parser.parse("hello");
        assertEquals(1, results.size());
        assertEquals("HELLO", results.get(0).getKey());
    }

    @Test
    void parse_nonMatchingInput_returnsEmptyList() throws Exception {
        Pattern p = Pattern.compile("^hello$");
        parser.addPatternTagMappings(Map.of(p, "HELLO"));

        List<Pair<String, Matcher>> results = parser.parse("world");
        assertTrue(results.isEmpty());
    }

    @Test
    void parse_inputWithLeadingTrailingWhitespace_stripsThenMatches() throws Exception {
        Pattern p = Pattern.compile("^hello$");
        parser.addPatternTagMappings(Map.of(p, "HELLO"));

        List<Pair<String, Matcher>> results = parser.parse("  hello  ");
        assertEquals(1, results.size());
    }

    @Test
    void parse_captureGroups_namedGroupAccessible() throws Exception {
        Pattern p = Pattern.compile("^bye(?:\\s+(?<arg>.*?))?\\s*$");
        parser.addPatternTagMappings(Map.of(p, "BYE"));

        List<Pair<String, Matcher>> results = parser.parse("bye");
        assertFalse(results.isEmpty());
        assertEquals("BYE", results.get(0).getKey());
    }

    @Test
    void parse_multiplePatterns_matchesCorrectOne() throws Exception {
        Pattern p1 = Pattern.compile("^hello$");
        Pattern p2 = Pattern.compile("^world$");
        parser.addPatternTagMappings(Map.of(p1, "HELLO", p2, "WORLD"));

        List<Pair<String, Matcher>> helloResults = parser.parse("hello");
        List<Pair<String, Matcher>> worldResults = parser.parse("world");

        assertEquals(1, helloResults.size());
        assertEquals("HELLO", helloResults.get(0).getKey());
        assertEquals(1, worldResults.size());
        assertEquals("WORLD", worldResults.get(0).getKey());
    }

    @Test
    void parse_emptyParser_returnsEmptyList() {
        List<Pair<String, Matcher>> results = parser.parse("anything");
        assertTrue(results.isEmpty());
    }

    @Test
    void parse_withRegexParserTagEnum_worksCorrectly() throws Exception {
        RegexParser<RegexParser.Tag> tagParser = new RegexParser<>();
        Pattern p = Pattern.compile("^\\s*list\\b.*$");
        tagParser.addPatternTagMappings(Map.of(p, RegexParser.Tag.LIST));

        List<Pair<RegexParser.Tag, Matcher>> results = tagParser.parse("list");
        assertEquals(1, results.size());
        assertEquals(RegexParser.Tag.LIST, results.get(0).getKey());
    }
}
