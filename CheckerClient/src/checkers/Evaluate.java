package checkers;

import java.util.*;

/*
 *Excuse the duplicate code from Utility
 */
public class Evaluate {
    public int[] D;

    public int side;

    protected Stack<Integer> rv;

    protected static final int RV_INITIAL_CAPACITY = 500;

    protected boolean forcedJump;

    protected boolean forcedJumpKnown;

    public Evaluate(int[] D, int side)
    {
        this.D = D.clone();
        this.side = side;

        rv = new Stack<Integer>();
        rv.ensureCapacity(RV_INITIAL_CAPACITY);

        forcedJumpKnown = false;
    }

    public Evaluate(Evaluate bs)
    {
        this(bs.D, bs.side);
    }

    public boolean isValidMove(Move move)
    {
        return Utility.isValidMove(D, side, move);
    }

    public boolean isLoser()
    {
        return getAllPossibleMoves().size() == 0;
    }

    public List<Move> getAllPossibleMoves()
    {
        if (isForcedJump())
            return findJumpMoves();
        else
            return findWalkMoves();
    }

    protected List<Move> findWalkMoves()
    {
        List<Move> moveList = new ArrayList<Move>();

        for (int a = 1; a < 64; a += 2 + (a%16==7?-1:0) + (a%16==14?1:0) )
        {
            if (D[a] % 4 != side)
                continue;

            for (int d : Utility.DIAG)
            {
                int b = a + d;
                if ( !canWalk(a, b) )
                    continue;

                moveList.add(new Move( Arrays.asList(a, b) ));
            }
        }

        return moveList;
    }

    protected List<Move> findJumpMoves()
    {
        List<Move> moveList = new ArrayList<Move>();
        for (int a = 1; a < 64; a += 2 + (a%16==7?-1:0) + (a%16==14?1:0) )
        {
            if (D[a] % 4 != side)
                continue;

            MultipleMove pmove = new MultipleMove();
            pmove.add(a);

            findJumpMovesHelper(moveList, pmove);
        }
        return moveList;
    }

    protected void findJumpMovesHelper(List<Move> moveList, MultipleMove pmove)
    {
        int a = pmove.get(pmove.size() - 1);
        boolean canJumpAgain = false;

        for (int d : Utility.DIAG)
        {
            int b = a + 2 * d;
            if ( !canJump(a, b) )
                continue;

            canJumpAgain = true;

            pmove.add(b);

            int rvTar = rv.size();
            jump(a, b);
            findJumpMovesHelper(moveList, pmove);
            revert(rvTar);

            pmove.remove(pmove.size() - 1);
        }
        
        if (!canJumpAgain && pmove.size() >= 2)
            moveList.add( new Move(pmove) );
    }

    public boolean canWalk(int src, int dst)
    {
        return Utility.canWalk(D, src, dst);
    }

    public boolean canJump(int src, int dst)
    {
        return Utility.canJump(D, src, dst);
    }

    public boolean hasJump(int src)
    {
        int a = src;

        /* The checkers piece at source location must belong to side */
        if (D[a] % 4 != side)
            return false;

        /* Test jumps along each of four diagonals */
        for (int d : Utility.DIAG)
            if (canJump(a, a + 2 * d))
                return true;

        return false;
    }

    public boolean isForcedJump()
    {
        if (!forcedJumpKnown)
        {
            forcedJumpKnown = true;
            forcedJump = false;
            for (int a = 1; a < 64; a += 2 + (a%16==7?-1:0) + (a%16==14?1:0) )
                if (hasJump(a))
                {
                    forcedJump = true;
                    break;
                }
        }

        return forcedJump;
    }

    protected static int SQ_FORCED_JUMP = CheckersConstants.W * CheckersConstants.H;

    protected static int SQ_MOVE_RVTAR = CheckersConstants.W * CheckersConstants.H + 1;

    public void execute(Move move)
    {
        int rvTar = rv.size();

        if (forcedJumpKnown)
        {
            rv.push(SQ_FORCED_JUMP);
            rv.push(isForcedJump() == true ? 1 : 0);
        }

        if (Utility.isWalk(move))
            walk(move.get(0), move.get(1));
        else
        {
            int a = move.get(0), b;
            for (int i = 1; i < move.size(); i++)
            {
                b = move.get(i);
                jump(a, b);
                a = b;
            }
        }

        /* Crown kings that may have been created */
        crownKings();

        /* Swap sides */
        side = (side + 1) % 2;

        /* new state: forcedJump not known */
        forcedJumpKnown = false;

        rv.push(SQ_MOVE_RVTAR);
        rv.push(rvTar);
    }

    protected void walk(int src, int dst)
    {
        int a = src, b = dst;

        rv.push(a);   rv.push(D[a]);
        rv.push(b);   rv.push(D[b]);

        D[b] = D[a]; 
        D[a] = CheckersConstants.BLANK;
    }

    protected void jump(int src, int dst)
    {
        int a = src, b = dst;
        int c = (a + b) / 2;

        rv.push(a);   rv.push(D[a]);
        rv.push(b);   rv.push(D[b]);
        rv.push(c);   rv.push(D[c]);

        D[b] = D[a];
        D[a] = CheckersConstants.BLANK;
        D[c] = CheckersConstants.BLANK;
    }

    protected void crownKings()
    {
        /* Crown red pawns on top row */
        for (int j = 0 * CheckersConstants.W + 1; j < 1 * CheckersConstants.W; j += 2)
            if (D[j] == CheckersConstants.RED_PAWN)
            {
                rv.push(j);   rv.push(D[j]);
                D[j] = CheckersConstants.RED_KING;
            }

        /* Crown black pawns on bottom row */
        for (int j = 7 * CheckersConstants.W; j < CheckersConstants.H * CheckersConstants.W; j += 2)
            if (D[j] == CheckersConstants.BLUE_PAWN)
            {
                rv.push(j);   rv.push(D[j]);
                D[j] = CheckersConstants.BLUE_KING;
            }
    }

    protected void revert(int rvTar)
    {
        while (rv.size() > rvTar)
        {
            int y = rv.pop(), x = rv.pop();

            /* Virtual location for forcedJump */
            if (x == SQ_FORCED_JUMP)
            {
                forcedJump = y == 1;
                forcedJumpKnown = true;
            }
            /* Virtual location for rvTar (do nothing; handled by revert(void) */
            else if (x == SQ_MOVE_RVTAR)
                ;
            else
                D[x] = y;
        }
    }

    public void revert()
    {
        /* Clear forcedJumpKnown; this is later set in revert(int) */
        forcedJumpKnown = false;

        /* rvTar must be on top of stack */
        int rvTar = rv.pop(), x = rv.pop();

        if (x != SQ_MOVE_RVTAR)
            throw new IllegalStateException("Top of stack is not a move!");

        /* revert(int) does remaining processing */
        revert(rvTar);

        /* Swap turns */
        side = (side + 1) % 2;
    }

    public String toString()
    {
        return Utility.reportBoardState(D);
    }

    public boolean equals(Object o)
    {
        if ((Object)this == o)
            return true;

        int[] oD = ((Evaluate)o).D;
        
        return Utility.equalsBoardState(D, oD);
    }
}
