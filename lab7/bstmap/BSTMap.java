package bstmap;

import java.util.Set;
import java.util.Iterator;

public class BSTMap <K extends Comparable<K>, V> implements Map61B<K, V> {
    private Node root;   //root of BSTMap
    private int size;           //number of nodes in subtree;

    //Node class recommended to be private
    private class Node {
        private K key;              //sorted by key of BSTMap
        private V val;              //associated value of BSTMap
        private Node left, right;   //left and right subtree;


        //constructor
        public Node(K key, V val){
            this.key = key;
            this.val = val;
        }


    }
    /**
     * Initializes an empty symbol table.
     */
    public BSTMap() {
        this.clear();
    }


    /** Removes all of the mappings from this map. */

    @Override
    public void clear() {

        size = 0;    //reset the size value
        root = null;      //disconnect all the mapping
    }

    /** Returns true if this map contains a mapping for the specified key.
     * use get function instead of writing the iteration again */
    @Override
    public boolean containsKey(K key){
        if (key == null){
            throw new IllegalArgumentException("argument to contains() is null");
        } else {
            return get(key)!= null;
        }
    }

    /** Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key){
        return get(root, key);
    }

    private V get(Node x, K key){
        if (key == null){
            throw new IllegalArgumentException("null key");
            }
        if (x == null){
            return null;
        }
        int cmp = key.compareTo(x.key);
        if (cmp < 0){
            return get(x.left, key);
        } else if (cmp >0){
            return get(x.right, key);
        } else {
            return (V) x.val;
        }
    }

    /** Returns the number of key-value mappings in this map. */
    @Override
    public int size(){
        return size(root);
    }

    private int size(Node x){
        if (x == null){
            return 0;
        } else {
            return size;
        }
    }

    /** Associates the specified value with the specified key in this map.
     * if the key is null, exception  */
    @Override
    public void put(K key, V value){
        if (key == null){
            throw new IllegalArgumentException("null key for put()");
        }
        if (value == null){
            remove(key);
        }
        else {
            root = put(root, key, value); //the private method returns a value
        }
    }

    /** Associates the specified value with the specified key in this map.
     * if the key does not exist, add new key, val pair  */
    private Node put(Node x, K key, V value){
        if (key == null){
            throw new IllegalArgumentException("null key for put()");
        }
        if (value == null){
            throw new IllegalArgumentException("null value for put()");
        }
        else if (x == null){
            size ++;
            return new Node(key, value);
        }
        int cmp = key.compareTo((K) x.key);
        if (cmp < 0){
            x.left = put(x.left,key,value);
        } else if (cmp > 0){
            x.right = put(x.right, key, value);
        } else {
            x.val = value;
            size ++;
        }
        return x;
    }

    /** Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException. */
    @Override
    public Set<K> keySet(){
        throw new UnsupportedOperationException("no keySet method.");
    }

    /** Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException.
     *  you should return null if the argument key does not exist in the BSTMap.
     *  Otherwise, delete the key-value pair (key, value) and return value.*/
    @Override
    public V remove(K key){
        throw new UnsupportedOperationException("no remove method.");
    }

    /** Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    @Override
    public V remove(K key, V value){
        throw new UnsupportedOperationException("no remove method.");
    }

    /** iterator method needs to return an iterator object
     * in order transverse: left - root - right */
    @Override
    public Iterator<K> iterator() {
        return new BSTMapIterator;
    }

    /** iterator object need to have next() and hasNext(). */
    private class BSTMapIterator implements Iterator<K> {
        private int pos;  //current position, use to check next
        //constructor
        public BSTMapIterator(){
            pos = 0;     //start from 0;
        }
        public Boolean hasNext(){
            return pos < size;
        }

        public K next(){
            pos += 1;


        }


    }

    /** prints out your BSTMap in order of increasing Key - inorder transverse
     * left - root - right. */
    public void printInOrder(Node x){
        if (x == null){      //base case
            return;
        }
        printInOrder(x.left);
        System.out.println(x.key);
        printInOrder(x.right);
    }

}
