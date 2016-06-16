package checkers.ui;

import checkers.Move;

import java.util.EventObject;

public class MoveEvent extends EventObject
{
    protected Move move;

    public MoveEvent(Object source, Move move)
    {
        super(source);
        this.move = move;
    }

    public Move getMove()
    {
        return move;
    }
}
