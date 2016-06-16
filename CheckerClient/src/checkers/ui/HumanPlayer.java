package checkers.ui;

import checkers.CheckersPlayer;

import java.awt.HeadlessException;

public class HumanPlayer extends CheckersPlayer implements MoveListener {
    protected CheckersBoardModel cbwidget;

    public HumanPlayer(String name, int side) {
        super(name, side);
        cbwidget = null;
    }

    public synchronized void calculateMove(int[] bs) {
        if (cbwidget == null)
            throw new HeadlessException();

        cbwidget.setEnabled(side);

        cbwidget.addMoveListener(this);

        try {
            wait();
        } catch (InterruptedException e) { }
    }

    public synchronized void moveSelected(MoveEvent e) {
        setMove(e.getMove());
        cbwidget.removeMoveListener(this);
        notify();
    }

    public boolean isHuman()
    {
        return true;
    }

    public void setCheckersBoardWidget(CheckersBoardModel cb)
    {
        this.cbwidget = cb;
    }
}
