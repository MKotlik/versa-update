package checkers;

import javax.swing.event.*;

/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * VersaServer
 */

public class CheckersModel {
    protected int side;

    protected int[] bs;

    protected State state;

    protected GameClock clock;

    protected int winner;

    protected int ply;

    protected int lastCapturePly;

    protected int drawCaptureCondition = 100;

    public static enum State
    {
        ANTE, READY, WAITING, FINISHED, INVALID
    }

    protected CheckersPlayer[] cp;

    public CheckersModel(CheckersPlayer[] cp)
    {
        this(cp, Utility.INITIAL_BOARDSTATE, Utility.INITIAL_SIDE);
    }

    public CheckersModel(CheckersPlayer[] cp, int[] bs, int side)
    {
        this.cp = cp.clone();
        this.bs = bs.clone();
        this.side = side;
        winner = CheckersConstants.NEITHER;
        this.clock = new DefaultGameClock();
        ply = 0;
        lastCapturePly = 0;
        state = State.ANTE;
    }

    public synchronized void startGame()
    {
        if (state != State.ANTE)
            throw new IllegalStateException();

        GameEvent e = new GameEvent(this, "START", 
                cp[CheckersConstants.RED].getName() + " " + cp[CheckersConstants.BLUE].getName());
        fireGameChanged(e);

        if (Utility.isLoser(bs, side))
            declareWinner(Utility.otherSide(side));
        else
            state = State.READY;
        fireStateChanged();
    }

    public synchronized void makeMove(Move move) throws InvalidMoveException
    {
        if (state != State.WAITING)
            throw new IllegalStateException();

        if (!Utility.isValidMove(bs, side, move))
            throw new InvalidMoveException();

        String detail = String.format("%s %s (%d ms)", Utility.reportSide(side),
                move, clock.getTurnTime(side));
        GameEvent e = new GameEvent(this, "MOVE", detail);

        /* Update state */
        Utility.execute(bs, move);
        side = Utility.otherSide(side);
        ply += 1;
        if (!Utility.isWalk(move))
            lastCapturePly = ply;
        state = State.READY;

        /* Update clock */
        clock.press();
        clock.pause();

        fireGameChanged(e);

        /* End if lose or tie */
        if (Utility.isLoser(bs, side))
            declareWinner(Utility.otherSide(side));
        if (lastCapturePly + drawCaptureCondition == ply)
            declareWinner(CheckersConstants.NEITHER);

        fireStateChanged();
    }

    public synchronized void startWaiting()
    {
        if (state != State.READY)
            throw new IllegalStateException();

        state = State.WAITING;
        clock.resume();

        fireGameChanged(new GameEvent(this, "WAIT", Utility.reportSide(side)));
        fireStateChanged();
    }

    public synchronized void forfeit(String reason)
    {
        if (state != State.WAITING)
            throw new IllegalStateException();

        clock.press();
        clock.pause();

        String detail = String.format("%s (reason: %s)", Utility.reportSide(side), reason);
        fireGameChanged(new GameEvent(this, "FORFEIT", detail));
        declareWinner(Utility.otherSide(side));
    }

    protected synchronized void declareWinner(int side)
    {
        clock.pause();
        winner = side;
        state = State.FINISHED;

        if (side != CheckersConstants.NEITHER)
            fireGameChanged(new GameEvent(this, "WIN", Utility.reportSide(side)));
        else
            fireGameChanged(new GameEvent(this, "DRAW", ""));
        fireStateChanged();
    }

    public synchronized void crashGame(String reason)
    {
        if (state != State.WAITING && state != State.READY)
            throw new IllegalStateException();

        clock.pause();
        winner = CheckersConstants.NEITHER;
        state = State.INVALID;

        String detail = String.format("(reason: %s)", reason);
        fireGameChanged(new GameEvent(this, "CRASH", detail));
        fireStateChanged();
    }

    public int[] getBoardState() { return (int[])bs.clone(); }

    public int getSide() { return side; }

    public State getState() { return state; }

    public int getWinner() { return winner; }

    public CheckersPlayer getPlayer(int side) { return cp[side]; }

    public GameClock getClock() { return clock; }

    public void setClock(GameClock clock) { this.clock = clock; }

    public int getPly() { return ply; }

    protected EventListenerList listenerList = new EventListenerList();

    public void addChangeListener(ChangeListener listener)
    { listenerList.add(ChangeListener.class, listener); }

    public void removeChangeListener(ChangeListener listener)
    { listenerList.remove(ChangeListener.class, listener); }

    protected void fireStateChanged()
    {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener listener : listenerList.getListeners(ChangeListener.class))
            listener.stateChanged(e);
    }

    public void addGameListener(GameListener listener)
    { listenerList.add(GameListener.class, listener); }

    public void removeGameListener(GameListener listener)
    { listenerList.remove(GameListener.class, listener); }

    protected void fireGameChanged(GameEvent e)
    {
        for (GameListener listener : listenerList.getListeners(GameListener.class))
            listener.gameChanged(e);
    }
}
