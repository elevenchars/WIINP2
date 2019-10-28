import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * CS 3345 Project 2
 * William Ingarfield
 * October 25, 2019
 */

class Node {
    private String key;
    private long value;
    public boolean deleted;

    /**
     * Parameter constructor
     * @param key String key to be stored.
     * @param value long value to be stored.
     */
    public Node(String key, long value) {
        this.key = key;
        this.value = value;
        this.deleted = false;
    }

    // accessor methods

    public String getKey() {
        return key;
    }

    public long getValue() {
        return value;
    }
}

class HashTable {
    private int size;
    private char mode;
    private Node[] table;
    private int r;

    // metrics
    private int records = 0;
    private int inserts = 0;
    private int insertProbes = 0;

    private int searches = 0;
    private int searchProbes;

    private int failedSearches = 0;
    private int failedSearchProbes = 0;

    /**
     * Static hash function as defined in the project spec.
     * @param key String to hash
     * @param tableSize size of the hashtable
     * @return index in range [0, tableSize)
     */
    public static int hash(String key, int tableSize) {
        int hash = 0;
        for(int i = 0; i < key.length(); i++) {
            hash = (hash * 31 + key.charAt(i)) & 0xFFFFFFFF; // mod 2^32
        }
        hash = hash & 0xFFFFFFFF;
        hash = Integer.remainderUnsigned(hash, tableSize);
        return hash;
    }

    /**
     * Double hash function for double hashing scheme
     * @param h output from static hash function
     * @param r prime number r < tableSize
     * @return new hash
     */
    public static int hash2(int h, int r) {
        return r - (h % r);
    }

    /**
     * HashTable class to store String:long key-value pairs. I decided to change the method signature for code clarity.
     * @param size prime size of the table.
     * @param mode 'Q' for quadratic probing, 'D' for double hashing.
     * @param r Prime r < size for double hashing
     */
    public HashTable(int size, char mode, int r) {
        this.size = size;
        this.mode = mode;
        this.r = r;
        table = new Node[size];
    }

    /**
     * Insert k:v pair into the table.
     * @param key String key for the table
     * @param value long value to be paired
     * @return true if successful
     */
    public boolean insert(String key, long value) {
        if(mode == 'Q') {
            return quadInsert(key, value);
        } else {
            return doubleInsert(key, value);
        }
    }

    // Helper method for quadratic insert
    private boolean quadInsert(String key, long value) {
        if((double)records / size >= 0.5) {
            System.out.println("Table Overflow");
            System.exit(1);
        }

        int h = hash(key, size);
        int index;
        for(int i = 0; i < size/2; i++) {
            insertProbes++;
            index = h + i*i; // h + i^2
            index %= size;
            if(table[index] != null && table[index].getKey().equals(key) && !table[index].deleted) {
                return false;
            }
            if(table[index] == null || table[index].deleted) {
                table[index] = new Node(key, value);
                inserts++;
                records++;
                return true;
            }
        }
        System.out.println("Table Overflow");
        System.exit(1);
        return false;
    }

    // Helper method for double insert.
    private boolean doubleInsert(String key, long value) {
        int h = hash(key, size);
        int index = h;
        for(int i = 0; i < size/2; i++) {
            insertProbes++;
            if(table[index] != null && table[index].getKey().equals(key) && !table[index].deleted) {
                return false;
            }
            if(table[index] == null || table[index].deleted) {
                table[index] = new Node(key, value);
                inserts++;
                records++;
                return true;
            }
            index += hash2(h, r);
            index = index % size;
        }
        System.out.println("Table Overflow");
        System.exit(1);
        return false;
    }

    /**
     * Delete a key from the table.
     * @param key Key to be deleted from the table
     * @return True if the key is deleted. False if it doesnt occur
     */
    public boolean delete(String key) {
        if(mode == 'Q') {
            return quadDelete(key);
        } else {
            return doubleDelete(key);
        }
    }

    // quad probe helper
    private boolean quadDelete(String key) {
        int h = hash(key, size);
        int index;
        for(int i = 0; i < size/2; i++) {
            index = h + i*i;
            index %= size;
            if(table[index] != null && table[index].getKey().equals(key) && !table[index].deleted) {
                records--;
                table[index].deleted = true;
                return true;
            }
        }
        return false;
    }

    // double probe helper
    private boolean doubleDelete(String key) {
        int h = hash(key, size);
        int index = h;
        for(int i = 0; i < size/2; i++) {

            if(table[index] != null && table[index].getKey().equals(key) && !table[index].deleted) {
                records--;
                table[index].deleted = true;
                return true;
            }
            index += hash2(h, r);
            index %= size;
        }
        return false;
    }

    /**
     * Search for a key and return its value
     * @param key String to search for.
     * @return Long value, -1 if it is not found.
     */
    public long search(String key) {
        if(mode == 'Q') {
            return quadSearch(key);
        } else {
            return doubleSearch(key);
        }
    }

    //quad helper
    private long quadSearch(String key) {
        int h = hash(key, size);
        int index;
        int probes = 0;
        for(int i = 0; i < size/2; i++) {
            probes++;
            index = h + i*i;
            index = index % size;
            if(table[index] == null) {
                break;
            }
            if(table[index] != null && table[index].getKey().equals(key) && !table[index].deleted) {
                searches++;
                searchProbes += probes;
                return table[index].getValue();
            }
        }
        failedSearches++;
        failedSearchProbes += probes;
        return -1;
    }

    // double helper
    private long doubleSearch(String key) {
        int h = hash(key, size);
        int index = h;
        int probes = 0;
        for(int i = 0; i < size/2; i++) {
            probes++;
            if(table[index] == null) {
                break;
            }
            if(table[index] != null && table[index].getKey().equals(key) && !table[index].deleted) {
                searches++;
                searchProbes += probes;
                return table[index].getValue();
            }
            index += hash2(h, r);
            index = index % size;
        }
        failedSearches++;
        failedSearchProbes += probes;
        return -1;
    }

    /**
     * Clear the table and reset stats.
     */
    public void clearTable() {
        for(int i = 0; i < size; i++) {
            if(table[i] != null) { // if present
                table[i].deleted = true;
            }
        }
        records = 0;
        inserts = 0;
        insertProbes = 0;
        searches = 0;
        searchProbes = 0;
        failedSearches = 0;
        failedSearchProbes = 0;
    }

    /**
     * Return the number of records in the table
     * @return integer number of records in the table.
     */
    public int size() {
        return records;
    }

    /**
     * Statistics about the table
     * @return various stats in defined in the document
     */
    @Override
    public String toString() {
        return inserts + " " + insertProbes + " " + searches + " " + searchProbes + " " + failedSearches + " " + failedSearchProbes;
    }
}

public class WIINP2 {
    public static void main(String[] args) throws FileNotFoundException {

        Scanner input = new Scanner(new File("hash1.txt"));
        String[] command;
        char mode = input.nextLine().charAt(0);
        int size = input.nextInt();
        int r = input.nextInt(); // r=0 when mode = Q
        input.nextLine(); // this is to move the scanner to the next line (begin parsing commands)

        // command arguments
        long val;
        boolean result;

        HashTable h = new HashTable(size, mode, r);

        while(input.hasNext()) {
            // since each command is prefixed by a single character, followed by 0 or more space separated parameters
            command = input.nextLine().split(" ");

            // parse commands
            switch(command[0]) {
                case "I":
                    // insert record with key k and value v.
                    // Print "Key k inserted" or "Key k already exists"
                    result = h.insert(command[1], Long.parseLong(command[2]));
                    if(result) {
                        System.out.println("Key " + command[1] + " inserted");
                    } else {
                        System.out.println("Key " + command[1] + " already exists");
                    }
                    break;
                case "J":
                    // Insert record with key k and value v and print nothing
                    h.insert(command[1], Long.parseLong(command[2]));
                    break;
                case "D":
                    // delete record with key k.
                    // Print "Key k deleted" or "Key k doesn’t exist"
                    result = h.delete(command[1]);
                    if(result) {
                        System.out.println("Key " + command[1] + " deleted");
                    } else {
                        System.out.println("Key " + command[1] + " doesn't exist");
                    }
                    break;
                case "F":
                    // delete record with key k and print nothing
                    result = h.delete(command[1]);
                    break;
                case "S":
                    // search for key k and print "Key k found, record = v" or "Key k doesn’t exist"
                    val = h.search(command[1]);
                    if(val != -1) {
                        System.out.println("Key " + command[1] + " found, record = " + val);
                    } else {
                        System.out.println("Key " + command[1] + " doesn't exist");
                    }
                    break;
                case "T":
                    // search for key k and print nothing
                    h.search(command[1]);
                    break;
                case "P":
                    // print "Number of records in table = #####"
                    System.out.println("Number of records in table = " + h.size());
                    break;
                case "C":
                    // delete all hash table entries.
                    h.clearTable();
                    break;
                case "Q":
                    // print the following six integers in the order given below,
                    // space separated on a line:
                    // total number of successful inserts,
                    // total number of probes on all successful inserts,
                    // total number of successful searches,
                    // total number of probes on successful searches,
                    // total number of unsuccessful searches,
                    // total number of probes on unsuccessful searches,
                    // all of these quantities should be measured since the program began,
                    // or since the last C command was read
                    System.out.println(h);
                    break;
                case "H":
                    // print the string k, then a space, and then hash(k,tablesize) on a line
                    System.out.println(command[1] + " " + HashTable.hash(command[1], size));
                    break;
                case "E":
                    // End of input file
                    System.exit(0);

            }
        }
    }
}
