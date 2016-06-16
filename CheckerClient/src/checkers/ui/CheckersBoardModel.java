package checkers.ui;

import checkers.CheckersConstants;
import checkers.Move;
import checkers.MultipleMove;
import checkers.Utility;

import java.util.*;

import java.awt.event.*;
import javax.swing.event.*;

public class CheckersBoardModel 
{
    protected int enable;
    protected int[] bs, pbs;
    protected int[] hint;
    protected MultipleMove pmove;
    protected Map<Integer, Integer> removedPieces;

    public CheckersBoardModel()
    {
        this(Utility.INITIAL_BOARDSTATE);
    }

    public CheckersBoardModel(int[] bs)
    {
        this.bs = bs.clone();
        this.enable = CheckersConstants.NEITHER;

        this.pmove = new MultipleMove();
        this.pbs = bs.clone();

        this.hint = new int[CheckersConstants.W * CheckersConstants.H];
        this.removedPieces = new HashMap<Integer, Integer>();
    }

    public boolean isSelection()
    {
        return pmove.size() > 0;
    }

    public void boardPressed()
    {
        fireActionPerformed();
    }

    public void squarePressed(int b)
    {
        appendPartialMove(b);
    }

    public void appendPartialMove(int b)
    {
        pmove.add(b);
        if (!PMoveUtility.isValidPartialMove(bs, enable, pmove))
        {
            pmove.remove(pmove.size() - 1);
            if (pmove.size() > 0 && pmove.get(pmove.size() - 1) == b)
            {
                pmove.remove(pmove.size() - 1);
            }
            else if (bs[b] % 4 == enable)
            {
                pmove.clear();
                pmove.add(b);
            }
            else if (pmove.contains(b))
            {
                pmove.subList(pmove.lastIndexOf(b) + 1, pmove.size()).clear();
            }
        }

        updateBoardState();
        updateHintState();
        fireStateChanged();
    }

    public void clearPartialMove()
    {
        pmove.clear();
        updateBoardState();
        updateHintState();
        fireStateChanged();
    }

    public MultipleMove getPartialMove()
    {
        return pmove;
    }

    public void setPartialMove(MultipleMove pmove)
    {
        this.pmove = pmove;
        updateBoardState();
        updateHintState();
        fireStateChanged();
    }

    protected void updateBoardState()
    {
        removedPieces.clear();
        pbs = bs.clone();
        List<int[]> ops = Utility.convertMoveToPair(pmove);

        if (Utility.isWalk(pmove))
        {
            int[] op = ops.get(0);
            Utility.walk(pbs, op[0], op[1]);
        }
        else
        {
            for (int[] op : ops)
            {
                int mid = (op[0] + op[1]) / 2;
                removedPieces.put(mid, pbs[mid]);
                Utility.jump(pbs, op[0], op[1]);
            }
        }

        if (Utility.isValidMove(bs, enable, new Move(pmove)))
        {
            Move move = new Move(pmove);
            pmove.clear();
            pbs = bs.clone();
            setEnabled(CheckersConstants.NEITHER);
            fireMoveSelected(move);
        }
    }

    public static final int HINT_NONE    = 0;
    public static final int HINT_VALID   = 1;
    public static final int HINT_INVALID = 2;

    /* 
     * pbs must be updated before this 
     * pmove cannot be a valid move, i.e. must be partial
     */
    protected void updateHintState()
    {
        /* Clear hints */
        for (int i = 0; i < CheckersConstants.H * CheckersConstants.W; i++)
            hint[i] = HINT_NONE;

        if (pmove.size() == 0)
            return;

        int a = pmove.get(pmove.size() - 1);

        if ( pmove.size() > 1 || Utility.isForcedJump(pbs, enable) )
            for (int d : Utility.DIAG)
            {
                int b = a + 2 * d;
                if (Utility.canJump(pbs, a, b))
                    hint[b] = HINT_VALID;
            }
        else if (pmove.size() == 1)
            for (int d : Utility.DIAG)
            {
                int b = a + d;
                if (Utility.canWalk(pbs, a, b))
                    hint[b] = HINT_VALID;
            }

        if ( pmove.size() == 1 && Utility.isForcedJump(pbs, enable) )
            for (int d : Utility.DIAG)
            {
                int b = a + d;
                if (Utility.canWalk(pbs, a, b))
                    hint[b] = HINT_INVALID;
            }
    }

    public int[] getBoardState() { return pbs.clone(); }

    public void setBoardState(int[] bs) 
    { 
        if (Arrays.equals(this.bs, bs))
            return;

        this.bs = bs.clone();
        pmove.clear();
        updateBoardState();
        updateHintState();
        fireStateChanged();
    }

    public int getPiece(int index)
    { 
        if (removedPieces.containsKey(index))
            return removedPieces.get(index);
        else
            return pbs[index];
    }

    public int getHint(int index) { return hint[index]; }
    
    public int getEnabled() { return enable; }

    public void setEnabled(int enable) 
    { 
        if (this.enable == enable)
            return;

        clearPartialMove();
        this.enable = enable;
        fireStateChanged();
    }

    protected EventListenerList listenerList = new EventListenerList();

    public void addMoveListener(MoveListener listener)
    { listenerList.add(MoveListener.class, listener); }

    public void removeMoveListener(MoveListener listener)
    { listenerList.remove(MoveListener.class, listener); }

    public void addChangeListener(ChangeListener listener)
    { listenerList.add(ChangeListener.class, listener); }

    public void removeChangeListener(ChangeListener listener)
    { listenerList.remove(ChangeListener.class, listener); }

    public void addActionListener(ActionListener listener)
    { listenerList.add(ActionListener.class, listener); }

    public void removeActionListener(ActionListener listener)
    { listenerList.remove(ActionListener.class, listener); }

    protected void fireMoveSelected(Move move)
    {
        MoveEvent e = new MoveEvent(this, move);
        for (MoveListener listener : listenerList.getListeners(MoveListener.class))
            listener.moveSelected(e);
    }

    protected void fireStateChanged()
    {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener listener : listenerList.getListeners(ChangeListener.class))
            listener.stateChanged(e);
    }

    protected void fireActionPerformed()
    {
        ActionEvent e = new ActionEvent(this, 0, "asdf");
        for (ActionListener listener : listenerList.getListeners(ActionListener.class))
            listener.actionPerformed(e);
    }
}
