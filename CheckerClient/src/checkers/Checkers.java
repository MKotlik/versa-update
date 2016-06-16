package checkers;

import java.lang.reflect.*;

/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * VersaServer
 */

public class Checkers {
    /**
     * Main constructor
     * Game creation
     * Ran within a seperate thread
     */

    public CheckersPlayer[] cp;
    public long[] turnLimit;
    public boolean turnDelay;
    public int[] depthLimit;

    public int[] bs;
    public int side;

    public Checkers() {
        cp = new CheckersPlayer[] { null, null };
        turnLimit = new long[] {3000, 3000};
        turnDelay = true;
        depthLimit = new int[] {-1, -1};

        bs = Utility.INITIAL_BOARDSTATE;
        side = Utility.INITIAL_SIDE;
    }

    public static CheckersPlayer createCheckersPlayer(String className, String playerName, int side) {
        CheckersPlayer player;
        Class<?> aiClass;
        Constructor<?> aiConst;

        try {
            aiClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            throw new IllegalArgumentException("Cannot load " + className);
        }

        try {
            aiConst = aiClass.getConstructor(String.class, int.class);
        } catch (NoSuchMethodException e) {
            System.out.println(e);
            throw new IllegalArgumentException("Cannot load " + className);
        }

        try {
            player = (CheckersPlayer)aiConst.newInstance(playerName, side);
        } catch (InstantiationException e) {
            System.out.println(e);
            throw new IllegalArgumentException("Cannot load " + className);
        } catch (IllegalAccessException e) {
            System.out.println(e);
            throw new IllegalArgumentException("Cannot load " + className);
        } catch (InvocationTargetException e) {
            System.out.println(e);
            throw new IllegalArgumentException("Cannot load " + className);
        }

        return player;
    }


    /**
     *Starts and initiates the game.
     */
    public void init(String username) {

        /* Handles the player creation */
        String className, playerName;
        className = "checkers.ui.HumanPlayer";
        playerName = username;
        cp[0] = createCheckersPlayer(className, playerName, 0);


        className = "checkers.ai.AlphaBetaPlayer";
        playerName = "AlphaBeta bot";
        cp[1] = createCheckersPlayer(className, playerName, 1);



        /* Set depthLimit for players */
        for (int i : new int[] {CheckersConstants.RED, CheckersConstants.BLUE} )
            if (depthLimit[i] != -1)
                cp[i].setDepthLimit(depthLimit[i]);

        /* Create game model */
        CheckersModel cm = new CheckersModel(cp, bs, side);

        CheckersController ctl;


        /* Create the Swing GUI */
        checkers.ui.CheckersUIController uictl = new checkers.ui.CheckersUIController(cm);
        ctl = uictl;
        checkers.ui.CheckersUI ui = checkers.ui.CheckersUI.launch(cm, ctl);

        /* If any players are HumanPlayer, pass a reference to gui's
            * CheckerBoard widget. This is necessary for HumanPlayer's
            * calculateMove().                                            */
        for (int i : new int[] {CheckersConstants.RED, CheckersConstants.BLUE})
            if (cp[i] instanceof checkers.ui.HumanPlayer)
                ((checkers.ui.HumanPlayer)cp[i]).setCheckersBoardWidget(ui.getCheckersBoardWidget());



        /* Pass turnDelay to the controller */
        uictl.setTurnDelay(turnDelay);


        /* Pass turnLimit to the controller */
        for (int i : new int[] {CheckersConstants.RED, CheckersConstants.BLUE} )
            ctl.setTurnLimit(i, turnLimit[i]);

        /* Create game clock object (max 2 hr) */
        GameClock clock = new DefaultGameClock(
                new long[] {7200 * 1000, 7200 * 1000}, side);

        /* Pass the clock to the model */
        cm.setClock(clock);


        /* Automatically start controller loop (after short delay) */
        ctl.loopLater(500);
    }
}
