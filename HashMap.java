
public class HashMap<K,V> {

    private static final int  INITIAL_SIZE = 1<<4; //16
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    @SuppressWarnings("rawtypes")
    Entry[] hashTable;


    HashMap(){
        hashTable= new Entry[INITIAL_SIZE];
    }

    HashMap(int capacity) {
        int tableSize = tableSizeFor(capacity);
        hashTable= new Entry[tableSize];
    }

    /*
     These lines successively copy the highest set bit to all lower bits. 
     The idea is to “fill in” all the bits below the most significant 1-bit. 
     By the end, n is a pattern of 1s that extends from the most significant 1-bit of 
     the original n down to the least significant bit.
     Example Walkthrough
Suppose cap = 13. Then:

n = cap - 1 = 12 (binary 0000 1100).
After each line:

n |= n >>> 1;
n >>> 1 is 0000 0110 (6 in decimal).
OR-ing (0000 1100 | 0000 0110) = 0000 1110 (14 in decimal).
n |= n >>> 2;
n >>> 2 is 0000 0011 (3 decimal).
OR-ing (0000 1110 | 0000 0011) = 0000 1111 (15 decimal).
n |= n >>> 4;
n >>> 4 is 0000 0000 (0).
OR-ing with 0 doesn’t change n, so still 0000 1111 (15).
n |= n >>> 8; and n |= n >>> 16;
Still remain 0000 1111.
Now n is 1111 (binary), which is 15 in decimal. 
Finally, we do n + 1 = 16. That’s the next power of two greater than or equal to the original cap = 13.
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


    class Entry<K,V>{

        K key;
        V value;
        @SuppressWarnings("rawtypes")
        Entry next;

        Entry(K k, V v) {
            key = k;
            value = v;
        }


        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }


    public void put(K key, V value) {

        int hashCode = key.hashCode() % hashTable.length;
        Entry node = hashTable[hashCode];

        if(node == null) {

            @SuppressWarnings("rawtypes")
            Entry newNode = new Entry(key, value);
            hashTable[hashCode] = newNode;
        } else {
            Entry previousNode = node;
            while (node != null) {

                if (node.key == key) {
                    node.value = value;
                    return;
                }
                previousNode = node;
                node = node.next;
            }
            Entry newNode = new Entry(key,value);
            previousNode.next = newNode;
        }
    }


    public V get(K key) {

        int hashCode = key.hashCode() % hashTable.length;
        Entry node = hashTable[hashCode];

        while(node != null) {
            if(node.key.equals(key)) {
                return (V)node.value;
            }
            node = node.next;
        }
        return null;
    }

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


    }
}
