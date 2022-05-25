
package deque;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class ArrayDequeTest {
    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {


        ArrayDeque<String> ad1 = new ArrayDeque<String>();

        assertTrue("A newly initialized ADeque should be empty", ad1.isEmpty());
        ad1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, ad1.size());
        assertFalse("lld1 should now contain 1 item", ad1.isEmpty());

        ad1.addLast("middle");
        assertEquals(2, ad1.size());

        ad1.addLast("back");
        assertEquals(3, ad1.size());

        System.out.println("Printing out deque: ");
        ad1.printDeque();
    }

    @Test
    /* test the addFirst, addLast and resize */
    public void addTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 15; i++) {
            ad1.addFirst(i);
        }

        for (int j = 15; j < 32; j++) {
            ad1.addLast(j);
        }
        for (int k = 0; k < 32; k++) {
            System.out.println(ad1.get(k));
        }
    }

    @Test
    /* test the removeFirst, removeLast and resize */
    public void removeTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 50; i++) {
            ad1.addFirst(i);
        }

        for (int j = 0; j < 12; j++) {
            ad1.removeLast();
        }

        for (int l = 0; l < 30; l++) {
            ad1.removeFirst();
        }

        for (int k = 0; k < 16; k++) {
            System.out.println(ad1.get(k));
        }
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            ad1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            ad1.removeFirst();
        }

        for (double j = 500000; j < 1000000; j++) {
            ad1.removeLast();
        }
        System.out.println(ad1.isEmpty());
    }


    /**@Test
    /** test the iterator function.
    public void iteratorTest(){
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 10; i++) {
            ad1.addLast(i);
        }
        for(int i : ad1) {
            System.out.println(i);
        }
    } */

    }

