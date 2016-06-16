package checkers;

public abstract class CheckersPlayer {
    protected String name;

    protected int side;

    protected volatile Move chosenMove;

    protected int depthLimit;

    public CheckersPlayer(String name, int side)
    {
        this.name = name;
        this.side = side;
        depthLimit = 1000;
    }

    public abstract void calculateMove(int[] bs);

    public final Move getMove()
    {
        return chosenMove;
    }

    protected synchronized void setMove(Move move)
    {
        this.chosenMove = move;
    }


    public String getName()
    {
        return name;
    }


    public void setDepthLimit(int depthLimit)
    {
        this.depthLimit = depthLimit;
    }


    public int getDepthLimit()
    {
        return depthLimit;
    }

    public String toString()
    {
        return name;
    }

    public boolean isHuman()
    {
        return false;
    }
}
