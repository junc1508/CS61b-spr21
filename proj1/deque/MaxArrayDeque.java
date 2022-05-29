package deque;
import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque {
    public Comparator comp;



    /** constructor that creates a MaxArrayDeque with the given Comparator. */
    public MaxArrayDeque(Comparator<T> c){
        super();
        comp = c;
    }

    /**  returns the maximum element in the deque as governed
     * by the previously given Comparator.
     * If the MaxArrayDeque is empty, simply return null. */

     public T max(){
        if (isEmpty()){
            return null;
        } else {
            T result = (T) this.get(0);

            for (int j = 0; j < length(); j++){
                if (this.get(j) != null && comp.compare(result,get(j)) < 0){
                    result = (T) get(j);
                }
            }
            return result;
        }
     }




    /** returns the maximum element in the deque
     * as governed by the parameter Comparator c.
     * If the MaxArrayDeque is empty, simply return null.
     */
    public T max(Comparator<T> c){
        if (isEmpty()){
            return null;
        } else {
            T result = (T) get(0);

            for (int j = 0; j < length(); j++){
                if (get(j) != null && c.compare(result,(T) get(j)) < 0){
                    result = (T) get(j);
                }
            }
            return result;
        }

    }

}
