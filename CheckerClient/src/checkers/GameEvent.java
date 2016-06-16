package checkers;

import java.util.EventObject;

public class GameEvent extends EventObject
{
    protected String action;
    protected String details;

    public GameEvent(Object source, String action, String details)
    {
        super(source);
        this.action = action;
        this.details = details;
    }
}
