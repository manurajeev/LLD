package LowLevelDesign.DesignDataStructures;

public class HashMap<K, V> {

    private static final int INITIAL_SIZE = 1 << 4; // 16
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    
    private float loadFactor;       // e.g. 0.75f
    private int size;               // number of key-value pairs actually stored
    private int threshold;          // when size >= threshold, resize
    
    private Entry<K, V>[] hashTable;

    /**
     * Default constructor with default capacity 16 and default load factor 0.75
     */
    public HashMap() {
        this(INITIAL_SIZE, 0.75f);
    }

    /**
     * Constructor that takes initial capacity and a load factor
     */
    @SuppressWarnings("unchecked")
    public HashMap(int capacity, float loadFactor) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + capacity);
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        int tableSize = tableSizeFor(capacity);
        this.hashTable = new Entry[tableSize];
        this.loadFactor = loadFactor;
        this.threshold = (int) (tableSize * loadFactor);
        this.size = 0;
    }

    /**
     * Constructor that takes only initial capacity; load factor = 0.75
     */
    public HashMap(int capacity) {
        this(capacity, 0.75f);
    }

    /**
     * Ensures capacity is a power of two and not greater than MAXIMUM_CAPACITY
     */
    final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * A singly-linked list node for storing key-value pairs
     */
    class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;

        Entry(K k, V v) {
            this.key = k;
            this.value = v;
        }
    }

    /**
     * Inserts or updates a key-value pair into the map
     */
    public void put(K key, V value) {
        // 1. Check if resizing is needed BEFORE adding new entry
        if (size >= threshold) {
            resize();
        }

        // 2. Compute index
        int hashCode = indexFor(key, hashTable.length);

        // 3. Traverse or insert
        Entry<K, V> node = hashTable[hashCode];
        if (node == null) {
            hashTable[hashCode] = new Entry<>(key, value);
            size++;
        } else {
            Entry<K, V> prevNode = null;
            while (node != null) {
                // Use equals() for key comparison
                if (node.key.equals(key)) {
                    // Update existing key
                    node.value = value;
                    return;
                }
                prevNode = node;
                node = node.next;
            }
            // key doesn't exist, append new entry
            prevNode.next = new Entry<>(key, value);
            size++;
        }
    }

    /**
     * Retrieves the value associated with the given key
     */
    public V get(K key) {
        int hashCode = indexFor(key, hashTable.length);
        Entry<K, V> node = hashTable[hashCode];

        while (node != null) {
            if (node.key.equals(key)) {
                return node.value;
            }
            node = node.next;
        }
        return null;
    }

    /**
     * A simple utility that ensures a non-negative, in-bounds index for the hash table.
     * Here we do: (key.hashCode() & 0x7fffffff) % length
     */
    private int indexFor(K key, int length) {
        if (key == null) {
            // If you want to allow null keys, you might fix an index for them, e.g. 0
            return 0;
        }
        return (key.hashCode() & 0x7fffffff) % length;
    }

    /**
     * Doubles the capacity of the hash table and rehashes all entries
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        int oldCapacity = hashTable.length;
        int newCapacity = oldCapacity << 1; // double

        // cannot exceed maximum capacity
        if (oldCapacity == MAXIMUM_CAPACITY) {
            // If we are already at max capacity, don't resize further
            return;
        }
        if (newCapacity > MAXIMUM_CAPACITY) {
            newCapacity = MAXIMUM_CAPACITY;
        }

        Entry<K, V>[] oldTable = hashTable;
        Entry<K, V>[] newTable = new Entry[newCapacity];

        // Rehash all entries
        for (int i = 0; i < oldTable.length; i++) {
            Entry<K, V> node = oldTable[i];
            while (node != null) {
                // save reference to next, so we don't lose track
                Entry<K, V> nextNode = node.next;

                // re-compute index for new table
                int newIndex = indexFor(node.key, newCapacity);
                // insert at head of newIndex's chain
                node.next = newTable[newIndex];
                newTable[newIndex] = node;

                // move on
                node = nextNode;
            }
        }

        // update references
        hashTable = newTable;
        // recalculate threshold
        threshold = (int) (newCapacity * loadFactor);
    }

    /**
     * Returns the current number of key-value pairs
     */
    public int size() {
        return size;
    }

    /**
     * Simple testing
     */
    public static void main(String args[]) {
        HashMap<Integer, String> map = new HashMap<>(7);

        map.put(1, "hi");
        map.put(2, "my");
        map.put(3, "name");
        map.put(4, "is");
        map.put(5, "Shrayansh");
        map.put(6, "how");
        map.put(7, "are");
        map.put(8, "you");
        map.put(9, "friends");
        map.put(10, "?");

        String value = map.get(8);
        System.out.println(value);

        // Print size
        System.out.println("Size is " + map.size());
    }
}
