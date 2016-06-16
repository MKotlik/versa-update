package checkers;

import static checkers.CheckersConstants.*;

import java.util.*;

public class Utility {
    /**
     * Utility code that will be usesd through the package to help evaluate
     * Some code might be duplicated
     *
     */

    public static int otherSide(int side) {
        if (side == RED) return BLUE;
        if (side == BLUE) return RED;
        return NEITHER;
    }

    public static final int[] DIAG = new int[] {-9, -7, 7, 9};

    public static boolean isWalk(List<Integer> move) {
        /* Walk moves have exactly two locations */
        if (move.size() != 2)
            return false;

        /* The source and destination location must be exactly one square
         * apart diagonally */
        int a = move.get(0), b = move.get(1);
        return 
            Math.abs(a / W - b / W) == 1 &&
            Math.abs(a % W - b % W) == 1 ;
    }

    public static boolean isValidMove(int[] bs, int side, Move move) {
        /* The move cannot be null */
        if (move == null)
            return false;

        List<int[]> ops = Utility.convertMoveToPair(move);

        /* The move must have two or more locations */
        if (ops.size() == 0)
            return false;

        /* The checkers piece at source location must belong to side */
        if (bs[move.get(0)] % 4 != side)
            return false;

        if (Utility.isWalk(move))
        {
            /* The move is a walk move */

            /* Cannot perform walk move if jump moves exist */
            if (Utility.isForcedJump(bs, side))
                return false;

            /* Call Utility.canWalk helper */
            int[] op = ops.get(0);
            return Utility.canWalk(bs, op[0], op[1]);
        }
        else
        {
            /* The move is a jump move */

            /* Clone board state to partial board state */
            int[] pbs = bs.clone();

            /* Attempt to perform each jump in the sequence of jumps. If any jumps
             * fail, return false. */
            for (int[] op : ops)
            {
                if ( Utility.canJump(pbs, op[0], op[1]) )
                    Utility.jump(pbs, op[0], op[1]);
                else
                    return false;
            }

            /* Must capture all available pieces in jump sequence */
            if ( Utility.hasJump(pbs, side, move.get(move.size() - 1)) )
                return false;

            return true;
        }
    }

    public static boolean isLoser(int[] bs, int side) {
        /* Player loses if no legal moves may be performed */
        return Utility.getAllPossibleMoves(bs, side).size() == 0;
    }

    public static boolean isForcedJump(int[] bs, int side) {
        for (int a = 0; a < H * W; a++)
            if (hasJump(bs, side, a))
                return true;
        return false;
    }

    public static List<Move> getAllPossibleMoves(int[] bs, int side) {
        if ( Utility.isForcedJump(bs, side) )
            return Utility.findJumpMoves(bs, side);
        else
            return Utility.findWalkMoves(bs, side);
    }


    public static List<Move> findWalkMoves(int[] bs, int side) {
        List<Move> moveList = new ArrayList<Move>();

        for (int a = 0; a < H * W; a++)
        {
            if (bs[a] % 4 != side)
                continue;

            for (int d : Utility.DIAG)
            {
                int b = a + d;
                if ( !Utility.canWalk(bs, a, b) )
                    continue;

                moveList.add(new Move( Arrays.asList(a, b) ));
            }
        }
        return moveList;
    }


    public static List<Move> findJumpMoves(int[] bs, int side) {
        List<Move> moveList = new ArrayList<Move>();
        for (int a = 0; a < H * W; a++)
        {
            if (bs[a] % 4 != side)
                continue;

            MultipleMove pmove = new MultipleMove();
            pmove.add(a);

            Utility.findJumpMovesHelper(bs, pmove, moveList);
        }
        return moveList;
    }

    public static void findJumpMovesHelper(int[] pbs, MultipleMove pmove, List<Move> moveList) {
        int a = pmove.get(pmove.size() - 1);
        boolean canJumpAgain = false;

        for (int d : Utility.DIAG)
        {
            int b = a + 2 * d;
            if ( !Utility.canJump(pbs, a, b) )
                continue;

            canJumpAgain = true;

            pmove.add(b);

            Stack<Integer> rv = Utility.jump(pbs, a, b);
            Utility.findJumpMovesHelper(pbs, pmove, moveList);
            Utility.revert(pbs, rv);

            pmove.remove(pmove.size() - 1);
        }
        
        if (!canJumpAgain && pmove.size() >= 2)
            moveList.add( new Move(pmove) );
    }


    public static boolean canWalk(int[] pbs, int src, int dst) {
        int a = src, b = dst;

        /* Return false if src and dst are out of bounds */
        if (a < 0 || a > H * W ||
            b < 0 || b > H * W  )
            return false;

        /* src and dst must be one square apart on a diagonal */
        if ( Math.abs(a / W - b / W) != 1 ||
             Math.abs(a % W - b % W) != 1  )
            return false;

        /* RED pawns must move up the board */
        if ( pbs[a] == RED_PAWN && a / W <= b / W)
            return false;

        /* BLUE pawns must move down the board */
        if ( pbs[a] == BLUE_PAWN && a / W >= b / W)
            return false;

        /* The destination square must be empty */
        if (pbs[b] != BLANK)
            return false;

        return true;
    }


    public static boolean canJump(int[] pbs, int src, int dst) {
        int a = src, b = dst;

        /* Return false if src and dst are out of bounds */
        if (a < 0 || a >= H * W ||
            b < 0 || b >= H * W  )
            return false;

        /* a and b must be two squares apart on a diagonal */
        if ( Math.abs(a / W - b / W) != 2 ||
             Math.abs(a % W - b % W) != 2  )
            return false;

        /* Source location must be nonempty */
        if (pbs[a] == BLANK)
            return false;

        /* Midpoint square must contain opponent's checkers piece piece */
        int c = (a + b) / 2;
        if ( pbs[c] % 4 == pbs[a] % 4 )
            return false;

        /* Destination location must be empty */
        if (pbs[b] != BLANK)
            return false;

        /* Midpoint square must not be empty */
        if (pbs[c] == BLANK)
            return false;
        
        /* RED pawns must move up the board */
        if ( pbs[a] == RED_PAWN && a / W <= b / W)
            return false;

        /* BLUE pawns must move down the board */
        if ( pbs[a] == BLUE_PAWN && a / W >= b / W)
            return false;

        return true;
    }

    public static boolean hasJump(int[] pbs, int side, int src) {
        int a = src;

        /* The checkers piece at source location must belong to side */
        if (pbs[a] % 4 != side)
            return false;

        /* Test jumps along each of four diagonals */
        for (int d : Utility.DIAG)
            if (Utility.canJump(pbs, a, a + 2 * d))
                return true;

        return false;
    }

    public static Stack<Integer> execute(int[] bs, Move move) {
        Stack<Integer> rv;

        if (Utility.isWalk(move))
            rv = Utility.walk(bs, move.get(0), move.get(1));
        else
        {
            rv = new Stack<Integer>();

            int a = move.get(0), b;
            for (int i = 1; i < move.size(); i++)
            {
                b = move.get(i);
                rv.addAll( Utility.jump(bs, a, b) );
                a = b;
            }
        }

        /* Crown kings that may have been created */
        rv.addAll( Utility.crownKings(bs) );

        return rv;
    }

    public static Stack<Integer> walk(int[] pbs, int src, int dst) {
        int a = src, b = dst;

        Stack<Integer> rv = new Stack<Integer>();

        rv.push(a);   rv.push(pbs[a]);
        rv.push(b);   rv.push(pbs[b]);

        pbs[b] = pbs[a]; 
        pbs[a] = BLANK; 

        return rv;
    }

    public static Stack<Integer> jump(int[] pbs, int src, int dst) {
        int a = src, b = dst;
        int c = (a + b) / 2;

        Stack<Integer> rv = new Stack<Integer>();

        rv.push(a);   rv.push(pbs[a]);
        rv.push(b);   rv.push(pbs[b]);
        rv.push(c);   rv.push(pbs[c]);

        pbs[b] = pbs[a];
        pbs[a] = BLANK;
        pbs[c] = BLANK;

        return rv;
    }

    public static Stack<Integer> crownKings(int[] pbs) {
        Stack<Integer> rv = new Stack<Integer>();

        /* Crown red pawns on top row */
        for (int j = 0 * W + 1; j < 1 * W; j += 2)
            if (pbs[j] == RED_PAWN)
            {
                rv.push(j);   rv.push(pbs[j]);
                pbs[j] = RED_KING;
            }

        /* Crown black pawns on bottom row */
        for (int j = 7 * W; j < H * W; j += 2)
            if (pbs[j] == BLUE_PAWN)
            {
                rv.push(j);   rv.push(pbs[j]);
                pbs[j] = BLUE_KING;
            }

        return rv;
    }

    public static void revert(int[] pbs, Stack<Integer> rv) {
        while (!rv.empty())
        {
            int y = rv.pop(), x = rv.pop();

            pbs[x] = y;
        }
    }

    public static boolean equalsBoardState(int[] A, int[] B)
    {
        for (int i = 0; i < H * W; i++)
                if (A[i] != B[i])
                    return false;

        return true;
    }

    public static List<int[]> convertMoveToPair(List<Integer> move)
    {
        List<int[]> ops = new LinkedList<int[]>();
        for (int i = 0; i < move.size() - 1; i++)
            ops.add( new int[]{ move.get(i), move.get(i+1) } );
        return ops;
    }


    public static String reportBoardState(int[] bs)
    {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < H * W; i++)
        {
            switch (bs[i])
            {
                case RED_PAWN: s.append('r'); break;
                case RED_KING: s.append('R'); break;
                case BLUE_PAWN: s.append('b'); break;
                case BLUE_KING: s.append('B'); break;
                default:       s.append('-'); break;
            }
            s.append(' ');
            if ((i + 1) % W == 0)
                s.append('\n');
        }
        return s.toString();
    }

    public static String reportSide(int side)
    {
        switch(side)
        {
            case RED: return "RED";
            case BLUE: return "BLUE";
            case NEITHER: 
            default:
                return "NEITHER";
        }
    }

    public static String reportLocation(int loc)
    {
        if (loc < 0 || loc >= W * H)
            return "??";

        String row = "" + (8 - loc / W);
        String col = "" + (char)('a' + (loc % W));
        return col + row;
    }

    public static String reportMove(List<Integer> move)
    {
        if (move == null)
            return "null";

        StringBuffer s = new StringBuffer();
        for (int i = 0; i < move.size(); i++)
        {
            s.append(Utility.reportLocation(move.get(i)));
            if (i != move.size() - 1)
                s.append("-");
        }
        return s.toString();
    }


    public static final int INITIAL_SIDE = RED;

    public static final int[] INITIAL_BOARDSTATE;
    static
    {
        INITIAL_BOARDSTATE = new int[H * W];
        for (int i = 0; i < H * W; i++)
            INITIAL_BOARDSTATE[i] = BLANK;
         
        int[] L = new int[] {  1,  3,  5,  7,  8, 10, 12, 14, 17, 19, 21, 23 };
        for ( int i : L )
            INITIAL_BOARDSTATE[i] = BLUE_PAWN;

        for ( int i : L )
            INITIAL_BOARDSTATE[W * H - 1 - i] = RED_PAWN;

    };
}
