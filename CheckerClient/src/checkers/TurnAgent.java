package checkers;

public class TurnAgent {
    protected Thread thread;

    protected CheckersPlayer cp;

    protected volatile boolean running;

    protected CheckersController callback_controller = null;

    public TurnAgent() { }

    public synchronized void startCalculate(final CheckersPlayer cp, final int[] bs)
    {
        this.cp = cp;

        thread = new Thread() {
            public void run()
            {
                synchronized(cp)
                {
                    try {
                        cp.calculateMove(bs);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    running = false;
                }

                /* Invoke the callback, if enabled */
                if (callback_controller != null)
                    callback_controller.loopLater(1);
            }
        } ;

        running = true;
        thread.start();
    }

    public synchronized void stopCalculate()
    {
        //thread.interrupt();
        thread.stop();
        running = false;
    }

    public synchronized boolean hasMove()
    {
        if (running)
            return false;
        else
            return true;
    }

    public synchronized Move getMove()
    {
        if (running)
            throw new IllegalStateException();

        Move move = cp.getMove();
        if (move == null)
            return null;
        else
            return new Move(move);
    }

    public synchronized Move getForcedMove()
    {
        Move move = cp.getMove();
        if (move == null)
            return null;
        else
            return new Move(move);
    }

    public synchronized void setCallbackController(CheckersController ctl)
    {
        this.callback_controller = ctl;
    }
}
