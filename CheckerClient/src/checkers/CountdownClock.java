package checkers;

public interface CountdownClock
{
    public static final int PAUSED    = 0;
    public static final int RUNNING   = 1;
    public static final int FINISHED  = 2;

    public void reset();

    public void pause();

    public void resume();

    public long getTimeRemain();

    public int getState();

    public long getDelay();

    public void setDelay(long delay);
}
