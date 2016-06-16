package checkers;

public interface GameClock {
    public static final int PAUSED    = 0;
    public static final int RUNNING   = 1;
    public static final int FINISHED  = 2;

    public void press();

    public void pause();

    public void resume();

    public long getGameTime(int side);

    public long getGameTimeRemain(int side);

    public long getTurnTime(int side);

    public int getSide();

    public int getState();
}
