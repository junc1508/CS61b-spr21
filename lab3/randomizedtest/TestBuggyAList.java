package randomizedtest;

import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        /** adds the same value to both the correct and buggy AList implementations,
         * then checks that the results of three subsequent removeLast calls are the same
         */
        AListNoResizing<Integer> lst1 = new AListNoResizing<>();
        BuggyAList<Integer> lst2 = new BuggyAList<>();
        int[] numbers = {1, 2, 3, 4, 5, 6};
        int len = numbers.length;
        for (int i = 0; i < len; i++) {
            lst1.addLast(numbers[i]);
            lst2.addLast(numbers[i]);
        }
        assertEquals(lst1.removeLast(), lst2.removeLast()); //remove is performed, assert 6 == 6
        assertEquals(lst1.removeLast(), lst2.removeLast()); //remove is performed, assert 5 == 5
        assertEquals(lst1.removeLast(), lst2.removeLast()); //remove is performed, assert 4 == 4
    }
    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> B = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                B.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int sizeL = L.size();
                int sizeB = B.size();
            } else if (operationNumber == 2 & L.size() > 0){
                Integer integer = L.removeLast();
                Integer integerB = B.removeLast();
                assertEquals(integer,integerB);
            }
        }
    }
}
