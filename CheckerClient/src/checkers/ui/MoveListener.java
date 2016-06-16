package checkers.ui;

import java.util.EventListener;

public interface MoveListener extends EventListener{
    void moveSelected(MoveEvent e);
}

