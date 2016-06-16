package checkers;

import java.util.*;


public class MultipleMove extends ArrayList<Integer> {
    public MultipleMove(Collection<Integer> m) { super(m); }
    public MultipleMove() { ensureCapacity(8); }

    public String toString()
    {
        return Utility.reportMove(this);
    }
}
