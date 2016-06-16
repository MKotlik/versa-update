package checkers.ui;

import checkers.MultipleMove;
import checkers.Utility;

import java.util.*;

public class PMoveUtility
{
    public static int INVALID = 0;

    public static boolean isValidPartialMove(int[] bs, int side, MultipleMove pmove)
    {
        if (pmove.size() > 0 && bs[pmove.get(0)] % 4 != side)
            return false;

        List<int[]> ops = Utility.convertMoveToPair(pmove);

        if (Utility.isWalk(pmove))
        {
            /* The partial move is a complete walk move */
            if (Utility.isForcedJump(bs, side))
                return false;

            int[] op = ops.get(0);
            return Utility.canWalk(bs, op[0], op[1]);
        }
        else
        {
            /* The partial move is a partial jump move, a move with one 
             * location, or a move with zero locations. */
            int[] pbs = bs.clone();

            for (int[] op : ops)
            {
                if (!Utility.canJump(pbs, op[0], op[1]))
                    return false;

                Utility.jump(pbs, op[0], op[1]);
            }
            return true;
        }
    }

    public static void executePartialMove(int[] bs, MultipleMove pmove)
    {
        List<int[]> ops = Utility.convertMoveToPair(pmove);

        if (Utility.isWalk(pmove))
        {
            int[] op = ops.get(0);
            Utility.walk(bs, op[0], op[1]);
        }
        else
        {
            for (int[] op : ops)
                Utility.jump(bs, op[0], op[1]);
        }
    }
}
