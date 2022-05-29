package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> {

    private int size;
    private T[] items;
    private int nextFirst;
    private int nextLast;

    /**
     * constructor that create an empty deque of size 8.
     * Circular array
     */
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 3;
        nextLast = 4;
    }


    /**
     * Resize the underlying array to the target capacity.
     */
    private void resize(int capacity) {
        if (capacity > items.length) {
            T[] a = (T[]) new Object[capacity];
            System.arraycopy(items, 0, a, size / 2, size);
            items = a;
            nextFirst = size / 2 - 1;
            nextLast = size * 3 / 2;
        } else {
            T[] a = (T[]) new Object[capacity];
            if (nextFirst == items.length - 1){
                nextFirst = 0;
            }
            if (nextLast == 0){
                nextLast = items.length - 1;
            }
            if (nextFirst < nextLast){
                System.arraycopy(items, nextFirst + 1, a, 0, size);

            } else {
                System.arraycopy(items, nextFirst + 1, a, 0,items.length - 1 - nextFirst);
                System.arraycopy(items, 0, a, items.length - 1 - nextFirst,nextLast);

            }
            items = a;
            nextFirst = 0;
            nextLast = size - 1;
        }
    }

    @Override
    /**
     * Adds an item of type T to the front of the deque,
     * if the front item is at position 0, add to last.
     * You can assume that item is never null.
     */
    public void addFirst(T x) {
        /** if the deque is full, first is at position size/2 */
        if (size == items.length) {
            resize(size * 2);

        }
        items[nextFirst] = x;
        size = size + 1;
        if (nextFirst > 0) {
            nextFirst = nextFirst - 1;
        } else {
            //position 0 move to end of the deque
            nextFirst = items.length - 1;
        }

    }

    @Override
    /**
     * Adds an item of type T to the back of the deque.
     * If the last item is at length - 1, add to last.
     * You can assume that item is never null.
     */
    public void addLast(T x) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextLast] = x;
        size = size + 1;
        if (nextLast < items.length - 1) {
            nextLast = nextLast + 1;
        } else {
            //position length - 1 move to position 0
            nextLast = 0;
        }

    }



    @Override
    /**
     * Returns the number of items in the deque.
     */
    public int size() {
        return size;
    }

    /**
     * Prints the items in the deque from first to last,
     * separated by a space. Once all the items have been printed, print out a new line.
     */
    public void printDeque() {

    }

    @Override
    /** Removes and returns the item at the front of the deque.
     * if size < length/4, resize to length/4
     * if nextFirst is last, remove first.
     * If no such item exists, returns null. */
    public T removeFirst(){
        if (isEmpty()){
            return null;
        }
        T first;
        if (size < items.length/4){
            resize(items.length/4);
        }
        if (nextFirst == items.length - 1){
            first = items[0];
            items[0] = null;
            nextFirst = 0;
            size = size - 1;
        } else {
            first = items[nextFirst + 1];
            items[nextFirst + 1] = null;
            nextFirst = nextFirst + 1;
            size = size - 1;
        }
        return first;
    }

    @Override
    /**Removes and returns the item at the back of the deque.
     * If no such item exists, returns null.
     * sentinel.prev is last */
    public T removeLast(){
        if (isEmpty()){
            return null;
        }
        T last;
        if (size < items.length/4){
            resize(items.length/4);
        }
        if (nextLast == 0){
            last = items[items.length - 1];
            items[items.length - 1] = null;
            nextLast = items.length - 1;
            size = size - 1;
        } else {
            last = items[nextLast - 1];
            items[nextLast - 1] = null;
            nextLast = nextLast - 1;
            size = size - 1;
        }
        return last;

    }

    @Override
    /**Gets the item at the given index, where 0 is the front,
     * 1 is the next item, and so forth. If no such item exists,
     * returns null. Must not alter the deque! */
    public T get(int index){
        return items[index];
    }

    public int length(){
        return items.length;
    }



}
