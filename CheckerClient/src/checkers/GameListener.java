package checkers;

import java.util.EventListener;

public interface GameListener extends EventListener {
    void gameChanged(GameEvent e);
}

