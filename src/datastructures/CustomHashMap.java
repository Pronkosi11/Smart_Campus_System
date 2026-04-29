package datastructures;

/**
 * CustomHashMap - Hash Table Implementation
 * 
 * This class provides a custom implementation of a hash table (similar to Java's HashMap)
 * for the Smart Campus Management System. It demonstrates fundamental data structure concepts
 * while providing the functionality needed by the application.
 * 
 * Key Features:
 * - Generic type support for type safety (keys and values)
 * - Separate chaining collision resolution
 * - Dynamic resizing when load factor is exceeded
 * - O(1) average time complexity for put, get, and remove operations
 * - Iterator support for enhanced for-loops
 * 
 * Key Concepts Demonstrated:
 * - Hash tables and hash functions
 * - Separate chaining for collision resolution
 * - Generic programming with type parameters
 * - Dynamic array resizing
 * - Linked list manipulation
 * - Iterator pattern implementation
 * 
 * This implementation uses separate chaining, where each bucket contains a linked list
 * of entries that hash to the same index. This provides good performance even with
 * hash collisions.
 * 
 * @param <K> The type of keys maintained by this map
 * @param <V> The type of mapped values
 */
public class CustomHashMap<K, V> {
    
    // ========== CONFIGURATION CONSTANTS ==========
    
    /** Default initial capacity (number of buckets) */
    private static final int DEFAULT_CAPACITY = 16;
    
    /** Load factor threshold for resizing (0.75 = 75% full) */
    private static final float LOAD_FACTOR = 0.75f;

    // ========== INTERNAL DATA STRUCTURES ==========
    
    /** 
     * Array of buckets, each bucket is the head of a linked list.
     * Each bucket stores entries that hash to the same index.
     */
    private Entry<K, V>[] buckets;
    
    /** Current number of key-value pairs in the map */
    private int size;

    /**
     * Inner class representing a key-value pair in the hash table.
     * 
     * Each Entry stores:
     * - The key (used for hashing and comparison)
     * - The associated value
     * - Reference to the next entry in the bucket (for linked list)
     * 
     * This forms the linked list nodes for separate chaining collision resolution.
     * 
     * @param <K> Key type
     * @param <V> Value type
     */
    private static class Entry<K, V> {
        /** The key - final because keys never change in a map entry */
        final K key;
        
        /** The associated value */
        V value;
        
        /** Reference to the next entry in the same bucket (linked list) */
        Entry<K, V> next;

        /**
         * Creates a new entry.
         * 
         * @param key The key for this entry
         * @param value The value for this entry
         */
        Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }

    // ========== CONSTRUCTORS ==========
    
    /**
     * Creates a new hash map with default capacity.
     * 
     * This constructor initializes the hash map with 16 buckets,
     * which is a good starting size for most applications.
     */
    @SuppressWarnings("unchecked")
    public CustomHashMap() {
        buckets = (Entry<K, V>[]) new Entry[DEFAULT_CAPACITY];
        size = 0;
    }

    /**
     * Creates a new hash map with specified initial capacity.
     * 
     * This constructor allows customization of the initial bucket count.
     * Larger initial capacity can reduce resizing but uses more memory.
     * 
     * @param initialCapacity The initial number of buckets
     */
    @SuppressWarnings("unchecked")
    public CustomHashMap(int initialCapacity) {
        buckets = (Entry<K, V>[]) new Entry[initialCapacity];
        size = 0;
    }

    // ========== HASH FUNCTION ==========
    
    /**
     * Computes the hash code for a key and maps it to a bucket index.
     * 
     * This method:
     * 1. Gets the key's built-in hash code
     * 2. Takes absolute value to ensure positive index
     * 3. Uses modulo operator to map to bucket array size
     * 
     * A good hash function distributes keys uniformly across buckets.
     * 
     * @param key The key to hash
     * @return The bucket index where this key should be stored
     */
    private int hash(K key) {
        return Math.abs(key.hashCode() % buckets.length);
    }

    // ========== CORE OPERATIONS ==========
    
    /**
     * Associates the specified value with the specified key in this map.
     * 
     * This method implements the core put operation:
     * 1. Validates that key is not null
     * 2. Checks if resizing is needed (based on load factor)
     * 3. Computes the bucket index using hash function
     * 4. Searches for existing key in the bucket
     * 5. Updates existing entry or adds new entry
     * 
     * Time Complexity: O(1) average, O(n) worst case (all keys in one bucket)
     * 
     * @param key The key with which to associate the value
     * @param value The value to associate with the key
     * @throws IllegalArgumentException if key is null
     */
    public void put(K key, V value) {
        // Validate input
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Check if we need to resize (when load factor threshold is exceeded)
        if (size >= buckets.length * LOAD_FACTOR) {
            resize();
        }

        // Find the appropriate bucket for this key
        int index = hash(key);
        Entry<K, V> entry = buckets[index];

        // Search for existing key in the bucket's linked list
        while (entry != null) {
            if (entry.key.equals(key)) {
                // Key found - update the value and return
                entry.value = value;
                return;
            }
            entry = entry.next;  // Move to next entry in the bucket
        }

        // Key not found - add new entry at the beginning of the bucket
        Entry<K, V> newEntry = new Entry<>(key, value);
        newEntry.next = buckets[index];  // Link to existing entries
        buckets[index] = newEntry;       // Make new entry the bucket head
        size++;                          // Increment size counter
    }

    /**
     * Returns the value to which the specified key is mapped.
     * 
     * This method implements the core get operation:
     * 1. Validates that key is not null
     * 2. Computes the bucket index using hash function
     * 3. Searches for the key in the bucket's linked list
     * 4. Returns the value if found, null otherwise
     * 
     * Time Complexity: O(1) average, O(n) worst case
     * 
     * @param key The key whose associated value is to be returned
     * @return The value to which the key is mapped, or null if key not found
     * @throws IllegalArgumentException if key is null
     */
    public V get(K key) {
        // Validate input
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Find the appropriate bucket for this key
        int index = hash(key);
        Entry<K, V> entry = buckets[index];

        // Search for the key in the bucket's linked list
        while (entry != null) {
            if (entry.key.equals(key)) {
                return entry.value;  // Key found - return the value
            }
            entry = entry.next;    // Move to next entry in the bucket
        }

        // Key not found
        return null;
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     * 
     * This method implements the core remove operation:
     * 1. Validates that key is not null
     * 2. Computes the bucket index using hash function
     * 3. Searches for the key, keeping track of previous entry
     * 4. Removes the entry by updating links
     * 5. Decrements size and returns the removed value
     * 
     * Time Complexity: O(1) average, O(n) worst case
     * 
     * @param key The key whose mapping is to be removed
     * @return The previous value associated with key, or null if key not found
     * @throws IllegalArgumentException if key is null
     */
    public V remove(K key) {
        // Validate input
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Find the appropriate bucket for this key
        int index = hash(key);
        Entry<K, V> entry = buckets[index];
        Entry<K, V> prev = null;  // Keep track of previous entry for link updates

        // Search for the key in the bucket's linked list
        while (entry != null) {
            if (entry.key.equals(key)) {
                // Key found - remove it by updating links
                if (prev == null) {
                    // Entry to remove is the bucket head
                    buckets[index] = entry.next;
                } else {
                    // Entry to remove is in the middle or end
                    prev.next = entry.next;
                }
                size--;  // Decrement size counter
                return entry.value;  // Return the removed value
            }
            prev = entry;      // Update previous entry reference
            entry = entry.next; // Move to next entry in the bucket
        }

        // Key not found
        return null;
    }

    // ========== UTILITY METHODS ==========
    
    /**
     * Returns true if this map contains a mapping for the specified key.
     * 
     * @param key The key whose presence in this map is to be tested
     * @return true if this map contains a mapping for the key
     */
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /**
     * Returns true if this map maps one or more keys to the specified value.
     * 
     * This method requires iterating through all entries, making it O(n) complexity.
     * 
     * @param value The value whose presence in this map is to be tested
     * @return true if this map maps one or more keys to the value
     */
    public boolean containsValue(V value) {
        // Iterate through all buckets
        for (Entry<K, V> bucket : buckets) {
            Entry<K, V> entry = bucket;
            
            // Search through the bucket's linked list
            while (entry != null) {
                if (entry.value.equals(value)) {
                    return true;  // Value found
                }
                entry = entry.next;
            }
        }
        return false;  // Value not found in any bucket
    }

    /**
     * Resizes the hash table when the load factor threshold is exceeded.
     * 
     * This method doubles the bucket array size and rehashes all entries:
     * 1. Creates a new bucket array with double capacity
     * 2. Resets size to 0 (will be recalculated during rehashing)
     * 3. Re-inserts all entries into the new array
     * 4. Entries will be distributed across more buckets, reducing collisions
     * 
     * This operation is O(n) but happens infrequently (when load factor > 0.75).
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        // Store reference to old buckets
        Entry<K, V>[] oldBuckets = buckets;
        
        // Create new bucket array with double capacity
        buckets = (Entry<K, V>[]) new Entry[oldBuckets.length * 2];
        size = 0;  // Reset size (will be recalculated)

        // Re-insert all entries into the new bucket array
        for (Entry<K, V> bucket : oldBuckets) {
            Entry<K, V> entry = bucket;
            while (entry != null) {
                put(entry.key, entry.value);  // Re-insert entry
                entry = entry.next;
            }
        }
    }

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return The number of key-value mappings
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if this map contains no key-value mappings.
     * 
     * @return true if this map is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all of the mappings from this map.
     * 
     * This method clears all buckets by setting them to null
     * and resets the size counter to zero.
     */
    public void clear() {
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = null;  // Clear each bucket
        }
        size = 0;  // Reset size counter
    }

    /**
     * Returns a CustomArrayList view of the keys contained in this map.
     * 
     * This method iterates through all buckets and collects all keys
     * into a CustomArrayList for easy iteration and manipulation.
     * 
     * @return CustomArrayList containing all keys in this map
     */
    public CustomArrayList<K> keySet() {
        CustomArrayList<K> keys = new CustomArrayList<>();
        
        // Iterate through all buckets
        for (Entry<K, V> bucket : buckets) {
            Entry<K, V> entry = bucket;
            
            // Collect all keys from this bucket's linked list
            while (entry != null) {
                keys.add(entry.key);
                entry = entry.next;
            }
        }
        return keys;
    }

    /**
     * Returns a CustomArrayList view of the values contained in this map.
     * 
     * This method iterates through all buckets and collects all values
     * into a CustomArrayList for easy iteration and manipulation.
     * 
     * @return CustomArrayList containing all values in this map
     */
    public CustomArrayList<V> values() {
        CustomArrayList<V> values = new CustomArrayList<>();
        
        // Iterate through all buckets
        for (Entry<K, V> bucket : buckets) {
            Entry<K, V> entry = bucket;
            
            // Collect all values from this bucket's linked list
            while (entry != null) {
                values.add(entry.value);
                entry = entry.next;
            }
        }
        return values;
    }

    /**
     * Returns a string representation of this map.
     * 
     * This method creates a string in the format "{key1=value1, key2=value2, ...}"
     * which is useful for debugging and display purposes.
     * 
     * @return String representation of the map
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}";  // Empty map representation
        }
        
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;  // Track first entry for comma placement
        
        // Iterate through all buckets and entries
        for (Entry<K, V> bucket : buckets) {
            Entry<K, V> entry = bucket;
            while (entry != null) {
                if (!first) {
                    sb.append(", ");  // Add comma separator (not for first entry)
                }
                sb.append(entry.key).append("=").append(entry.value);
                first = false;  // Subsequent entries need commas
                entry = entry.next;
            }
        }
        sb.append("}");
        return sb.toString();
    }
}