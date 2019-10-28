# Open Hash Table

An implementation of an open hash table using two methods to resolve collisons:
 - Double hashing
 - Quadratic probing

No effort is made to resize the hashtable, and will exit if the load factor > 0.5 for quadratic probing, or the table is full and an element is attempted to be inserted.
The command spec is in the class WIINP2 and example usage can be seen in the text files.
