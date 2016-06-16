package checkers;

import java.util.*;

public class Move extends AbstractList<Integer>
{
    private List<Integer> m;

    public Move() { this.m = new ArrayList<Integer>(); }
    public Move(Collection<Integer> m) { this.m = new ArrayList<Integer>(m); }

    public Integer get(int index) { return m.get(index); }
    public int size() { return m.size(); }

    public String toString()
    {
        return Utility.reportMove(this);
    }
}
