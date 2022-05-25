package deque;

import java.util.Iterator;
import java.util.LinkedList;

public class LinkedListDeque<T> implements Iterable<T> {

    /* nested LinkedList class.*/
    public class LinkedList {
        public T item;
        public LinkedList next;
        public LinkedList prev;

        public LinkedList(T i, LinkedList p, LinkedList n) {
            item = i;
            next = n;
            prev = p;
        }

        /**Helper function for getRecursive
         * it is recursive here, and called by getRecursive */
        public T get(int index) {
            if (index == 0){
                return this.item;
            }
            return next.get(index - 1);
        }
    }

    private LinkedList sentinel;
    private int size;

    /** constructor that create an empty deque.
     * Circular sentinel
     * sentinel.next => sentinel
     * sentinel.prev => sentinel */
    public LinkedListDeque() {
        sentinel = new LinkedList(null, null,null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;

    }

    /** constructor that takes in item to create deque.
     * sentinel.next => new LinkedList
     * new LinkedList.next => sentinel
     * new.LinkedList.prev => sentinel
     * sentinel.prev => sentinel.next */
    public LinkedListDeque(T x){
        sentinel  = new LinkedList(null, null,null);
        sentinel.next = new LinkedList(x, sentinel, sentinel);
        sentinel.prev = sentinel.next;
        size = 1;
    }



    /** Adds an item of type T to the front of the deque, after sentinel
     * You can assume that item is never null.*/
    public void addFirst(T x){
        sentinel.next = new LinkedList(x, sentinel, sentinel.next);
        //links new.next with original sentinel.next.prev
        sentinel.next.next.prev = sentinel.next;
        size = size + 1;
    }

    /** Adds an item of type T to the back of the deque.
     *  You can assume that item is never null.
     *  sentinel.prev is last */
    public void addLast(T x){
        sentinel.prev = new LinkedList(x,sentinel.prev,sentinel);
        sentinel.prev.prev.next = sentinel.prev;
        size = size + 1;

    }

    /** Returns true if deque is empty, false otherwise. */
    public boolean isEmpty(){
        if (size == 0){
            return true;
        }
        return false;
    }

    /** Returns the number of items in the deque. */
    public int size(){
        return size;
    }

    /** Prints the items in the deque from first to last,
     * separated by a space. Once all the items have been printed, print out a new line.*/
    public void printDeque(){
        LinkedList p = sentinel;
        for (int i = 0; i < size; i ++){
            p = p.next;
            System.out.print(p.item + " ");
        }
        System.out.print("\n");
    }

    /** Removes and returns the item at the front of the deque.
     * If no such item exists, returns null. */
    public T removeFirst(){
        //empty list
        if (isEmpty()){
            return null;
        }
        T result;
        result = sentinel.next.item;
        sentinel.next = sentinel.next.next;
        sentinel.next.next.prev = sentinel;
        size = size - 1;
        return result;
    }

    /**Removes and returns the item at the back of the deque.
     * If no such item exists, returns null.
     * sentinel.prev is last */
    public T removeLast(){
        //empty list
        if (isEmpty()){
            return null;
        }
        T result;
        result = sentinel.prev.item;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        size = size - 1;
        return result;
    }

    /**Gets the item at the given index, where 0 is the front,
     * 1 is the next item, and so forth. If no such item exists,
     * returns null. Must not alter the deque! */
    public T get(int index){
        if (size == 0 || index >= size){
            return null;
        }
        T result;
        LinkedList p = sentinel;
        for (int i = 0; i <= index; i ++){
            p = p.next;
        }
        result = p.item;
        return result;
    }

    /**Same as get, but uses recursion. */
    public T getRecursive(int index){
        if (size == 0 || index >= size){
            return null;
        }
        return sentinel.next.get(index);
    }

    /**The Deque objects we’ll make are iterable (i.e. Iterable<T>)
     * so we must provide this method to return an iterator.
     * implement iterable<T> for the LLDeque class
     * implement iterator<T> for the iterator
     * hasNext() and next() method*/
    public Iterator<T> iterator(){
        return new LLDequeIterator();
    }
    private class LLDequeIterator implements Iterator<T>{
        private int pos;
        //constructor for iterator
        public LLDequeIterator() {
            pos = 0;
        }

        @Override
        public boolean hasNext() {
            return pos < size;
        }

        @Override
        public T next() {
            T returnItem = get(pos);
            pos = pos + 1;
            return returnItem;
        }
    }

    /** Returns whether or not the parameter o is equal to the Deque.
     * o is considered equal if it is a Deque and if it contains the same contents
     * (as goverened by the generic T’s equals method) in the same order */
    @Override
    public boolean equals(Object o){
        if (o == null){
            return false;
        }if (this == o) {
            return true;
        } if (this.getClass() != o.getClass()){
            return false; //if object o is not a deque
        }
        LinkedListDeque<T> other = (LinkedListDeque<T>) o;
        if (this.size() != other.size()){
            return false;
        } for (int i = 0; i < size; i++){
            if(this.get(i) != other.get(i)){
                return false;
            }
        }
        return true;
    }
}
