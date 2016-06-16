package checkers;

import java.util.*;

/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * VersaServer
 */

public class CheckersController {
    /**
     * does what its name implies, also helps controls the clock
     */
    protected CheckersModel model;

    protected TurnAgent turnAgent;

    protected Timer timer;

    protected CountdownClock[] turnClock;

    protected static final int BREAK_LOOP = -1;
    protected static final int CONTINUE_LOOP = 0;

    public CheckersController(CheckersModel model)
    {
        this(model, new long[] {-1, -1});
    }

    public CheckersController(CheckersModel model, long[] turnLimit) {
        this.model = model;
        
        /* Create the turn clock, which enforces for how long each player can
         * think per turn */
        turnClock = new CountdownClock[2];
        for (int i = 0; i < 2; i++)
            turnClock[i] = new DefaultCountdownClock(turnLimit[i]);

        turnAgent = new TurnAgent();
        turnAgent.setCallbackController(this);
        timer = new Timer(true);
    }


    public void loopLater(long delayTime) {
        TimerTask task = new TimerTask()
        {
            public void run() { loop(); }
        };
        timer.schedule(task, delayTime);
    }

    public synchronized void loop() {
        long sleepTime = 0;
        while (sleepTime == CONTINUE_LOOP)
        {
            try {
                sleepTime = step();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        if (sleepTime > 0)
            loopLater(sleepTime);
    }

    protected long step() {
        switch (model.getState()) {
            case ANTE:     return stepAnte();
            case READY:    return stepReady();
            case WAITING:  return stepWaiting();
            case FINISHED: return BREAK_LOOP;
            case INVALID:  return BREAK_LOOP;
            default:       return BREAK_LOOP;
        }
    }


    protected long stepAnte() {
        model.startGame();
        return CONTINUE_LOOP;
    }

    protected long stepWaiting() {
        int side = model.getSide();
        CheckersPlayer player = model.getPlayer(side);

        /* If a player has chosen a move, use it. */
        if (turnAgent.hasMove()) {
            /* Execute the move, and continue loop */
            try {
                model.makeMove(turnAgent.getMove());
            } catch (InvalidMoveException e) {
                model.forfeit("invalid move " + turnAgent.getMove());
            }
            return CONTINUE_LOOP;
        }

        if (turnClock[side].getState() == CountdownClock.FINISHED) {
            /* Stop calculation and forcefully obtain a move */
            turnAgent.stopCalculate();

            try { 
                model.makeMove(turnAgent.getForcedMove());
            } catch (InvalidMoveException e) {
                model.forfeit("invalid move " + turnAgent.getMove());
            }
            return CONTINUE_LOOP;
        }
        else
            return turnClock[side].getTimeRemain();
    }

    protected long stepReady() {
        int side = model.getSide();
        CheckersPlayer player = model.getPlayer(side);

        /* Set model state to WAITING */
        model.startWaiting();

        /* Start the turn clock that enforces term limits */
        turnClock[side].reset();
        turnClock[side].resume();

        /* Begin calculations with a TurnAgent */
        turnAgent.startCalculate(player, model.getBoardState());

        return CONTINUE_LOOP;
    }

    public synchronized void terminateGame(String reason) {
        turnAgent.stopCalculate();

        if (model.getState() == CheckersModel.State.READY ||
            model.getState() == CheckersModel.State.WAITING )
            model.crashGame(reason);
    }

    public void setTurnLimit(int side, long limit)
    { 
        turnClock[side].setDelay(limit);
    }

    public long getTurnLimit(int side) { return turnClock[side].getDelay(); }
}
