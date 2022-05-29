package deque;

import org.junit.Test;

import java.util.Comparator;

public class MaxArrayDequeTest {
    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void comparatorTest(){
        Comparator<Integer> cd = TestComparator.getTestComparator();
        MaxArrayDeque<Integer> md1 = new MaxArrayDeque<Integer>(cd);

        for (int i = 0; i < 4; i++) {
            md1.addLast(i);
        }

        System.out.println(md1.max());
        System.out.println("test");
        System.out.println("test");
        for (int k = 0; k < 8; k++) {
            System.out.println(md1.get(k));
        }

    }
}
