package checkers;

public class DefaultGameClock implements GameClock
{
    protected long[] gameLimit;
    protected long[] gameRemain;
    protected int side;
    protected int state;

    protected long resumeTime;
    protected long[] gameRemainPrev;

    public DefaultGameClock()
    {
        this(new long[] {300 * 1000, 300 * 1000} , Utility.INITIAL_SIDE);
    }

    public DefaultGameClock(long[] gameLimit, int side)
    {
        this.gameLimit = gameLimit.clone();
        this.gameRemain = gameLimit.clone();
        this.side= side;
        state = PAUSED;
        gameRemainPrev = gameRemain.clone();
    }

    public long getGameTime(int _side)
    {
        touch();
        return gameLimit[_side] - gameRemain[_side];
    }

    public long getGameTimeRemain(int _side)
    {
        touch();
        return gameRemain[_side];
    }

    public long getTurnTime(int _side)
    {
        touch();
        return gameRemainPrev[_side] - gameRemain[_side];
    }

    public void press()
    {
        pause();
        if (state == PAUSED)
        {
            side = Utility.otherSide(side);
            gameRemainPrev[side] = gameRemain[side];
        }
        resume();
    }

    public void pause()
    {
        touch();
        if (state == RUNNING)
            state = PAUSED;
    }

    public void resume()
    {
        touch();
        if (state == PAUSED)
            state = RUNNING;
    }

    protected void touch()
    {
        long currentTime = System.currentTimeMillis();
        if (state == RUNNING)
            gameRemain[side] -= (currentTime - resumeTime);
        resumeTime = currentTime;

        if (gameRemain[side] <= 0)
            state = FINISHED;
    }

    public int getState()
    { 
        touch();
        return state;
    }

    public int getSide() { return side; }
}
