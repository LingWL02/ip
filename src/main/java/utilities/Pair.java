package utilities;

/**
 * A generic container class that holds a pair of related objects.
 * This class provides an immutable key-value pair structure.
 *
 * @param <K> The type of the key (first element).
 * @param <V> The type of the value (second element).
 */
public class Pair<K, V> {

    /** The key (first element) of the pair. */
    private final K key;

    /** The value (second element) of the pair. */
    private final V value;

    /**
     * Constructs a new Pair with the specified key and value.
     *
     * @param key   The key (first element) of the pair.
     * @param value The value (second element) of the pair.
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key (first element) of this pair.
     *
     * @return The key of the pair.
     */
    public K getKey() {
        return this.key;
    }

    /**
     * Returns the value (second element) of this pair.
     *
     * @return The value of the pair.
     */
    public V getValue() {
        return this.value;
    }
}
