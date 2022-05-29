package deque;

import java.util.Comparator;
//cannot use <T> because T cannot be used in static.
public class TestComparator {
    private static class testComparator implements Comparator<Integer> {
        public int compare(Integer a, Integer b) {
            if (a != null && b != null){
                return a - b;
            } else if (a == null && b != null){
                return -1;
            } else if (a != null && b == null){
                return 1;
            } else {
                return 0;
            }
        }
    }

    public static Comparator<Integer> getTestComparator() {
        return new testComparator();
    }
}
