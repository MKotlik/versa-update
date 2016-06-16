package checkers.ui;

import checkers.*;

import java.awt.event.*;

public class CheckersUIController extends CheckersController implements ActionListener
{
    protected CountdownClock turnDelayClock;

    protected boolean turnDelay;

    protected boolean[] moveOnClick;

    protected volatile boolean isClick;

    public CheckersUIController(CheckersModel model)
    {
        this(model, new long[] {-1, -1}, new boolean[] {false, false});
    }

    public CheckersUIController(CheckersModel model, long[] turnLimit,
                                boolean[] moveOnClick)
    {
        super(model, turnLimit);

        this.moveOnClick = moveOnClick.clone();

        /* Create the clock to enforce delays in between turns, which helps
         * bring clarity to the UI */
        turnDelayClock = new DefaultCountdownClock(-1);
        turnDelay = true;
        updateTurnDelay();
    }

    public void actionPerformed(ActionEvent e)
    {
        /* Mouse clocked */
        if (e.getSource() instanceof CheckersBoardModel)
        {
            isClick = true;
            loop();
        }
    }

    public synchronized void loop()
    {
        super.loop();
        isClick = false;
    }

    protected void updateTurnDelay()
    {
        long turnDelayTime = 0;
        for (int i = 0; i < 2; i++)
            turnDelayTime += turnClock[i].getDelay();
        turnDelayTime /= 10;
        turnDelayTime = Math.max(turnDelayTime,  250);
        turnDelayTime = Math.min(turnDelayTime, 1000);

        if (turnDelay)
            turnDelayClock.setDelay(turnDelayTime);
        else
            turnDelayClock.setDelay(-1);
    }

    protected long stepWaiting()
    {
        int side = model.getSide();
        CheckersPlayer player = model.getPlayer(side);

        /* If a player has chosen a move, use it. */
        if (turnAgent.hasMove())
        {
            /* Execute the move, and continue loop */
            try {
                model.makeMove(turnAgent.getMove());
            } catch (InvalidMoveException e) {
                model.forfeit("invalid move " + turnAgent.getMove());
            }

            turnDelayClock.reset();
            turnDelayClock.resume();

            return CONTINUE_LOOP;
        }

        /* Don't force moves from interactive players. */
        if (player.isHuman())
            return BREAK_LOOP;

        /* 
         * If the player has used up the allocalated per-turn time,
         * forcefully obtain a move from the player and execute it. 
         * Otherwise sleep for timeRemain milliseconds and and check 
         * this condition again after waking up. 
         */
        if (turnClock[side].getState() == CountdownClock.FINISHED)
        {
            /* Stop calculation and forcefully obtain a move */
            turnAgent.stopCalculate();

            try { 
                model.makeMove(turnAgent.getForcedMove());
            } catch (InvalidMoveException e) {
                model.forfeit("invalid move " + turnAgent.getMove());
            }

            turnDelayClock.reset();
            turnDelayClock.resume();

            return CONTINUE_LOOP;
        }
        else
            return turnClock[side].getTimeRemain();
    }

    protected long stepReady()
    {
        int side = model.getSide();
        CheckersPlayer player = model.getPlayer(side);

        /* Skip this entire next section if isClick == true */
        if (!isClick)
        {
            /* Do not continue if moveOnClick is on */
            if (getMoveOnClick(side))
                return BREAK_LOOP;

            /* Otherwise, wait for turn delay */
            if (turnDelayClock.getState() == CountdownClock.RUNNING)
                return turnDelayClock.getTimeRemain();
        }

        /* Set model state to WAITING */
        model.startWaiting();

        /* Start the turn clock that enforces term limits */
        turnClock[side].reset();
        turnClock[side].resume();

        /* Begin calculations with a TurnAgent and register callback */
        turnAgent.startCalculate(player, model.getBoardState());

        return CONTINUE_LOOP;
    }


    public void setTurnDelay(boolean turnDelay)
    {
        this.turnDelay = turnDelay;
        updateTurnDelay();
    }

    public void setTurnLimit(int side, long limit)
    { 
        super.setTurnLimit(side, limit);
        updateTurnDelay();
    }

    public void setMoveOnClick(int side, boolean b) { moveOnClick[side] = b; }

    public boolean getMoveOnClick(int side) { return moveOnClick[side]; }
}
