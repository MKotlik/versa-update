package checkers.ui;

import checkers.CheckersConstants;
import checkers.Utility;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class CheckersBoard extends JPanel implements ChangeListener, ActionListener
{
    protected CheckersBoardModel model;

    protected CheckerPanel[] sqs;

    public CheckersBoard(CheckersBoardModel model)
    {
        this.model = model;
        model.addChangeListener(this);

        setDoubleBuffered(true);
        
        setLayout(new GridLayout(CheckersConstants.W, CheckersConstants.H));

        sqs = new CheckerPanel[CheckersConstants.H * CheckersConstants.W];
        for (int i = 0; i < CheckersConstants.H * CheckersConstants.W; i++)
        {
            sqs[i] = new CheckerPanel(model, i);
            add(sqs[i]);
            sqs[i].getModel().addActionListener(this);
        }
    }

    protected static final Stroke arrowStroke = new BasicStroke(5.0f);
    protected static final Color arrowColor = new Color(0, 0, 128);
    protected static final double arrowWidth = 12.0;
    protected static final double arrowHeight = 18.0;
    protected static final double arrowOffset = 5.0;


    public void drawArrow(Graphics2D g, int ax, int ay, int bx, int by)
    {
        double d = Math.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay));
        double zx = (bx - ax) / d;
        double zy = (by - ay) / d;

        ax += (int)(arrowOffset * zx);
        ay += (int)(arrowOffset * zy);
        bx -= (int)(arrowOffset * zx);
        by -= (int)(arrowOffset * zy);

        // projection vector
        double mx = (bx - ax) / d;
        double my = (by - ay) / d;
        // normal vector
        double nx = -my;
        double ny =  mx;

        int px = (int)(bx - mx * arrowHeight + nx * arrowWidth);
        int py = (int)(by - my * arrowHeight + ny * arrowWidth);
        int qx = (int)(bx - mx * arrowHeight - nx * arrowWidth);
        int qy = (int)(by - my * arrowHeight - ny * arrowWidth);

        Polygon arrowPoly = new Polygon();
        arrowPoly.addPoint(bx, by);
        arrowPoly.addPoint(px, py);
        arrowPoly.addPoint(qx, qy);

        g.setStroke(arrowStroke);
        g.drawLine(ax, ay, bx - (int)(arrowHeight * mx), by - (int)(arrowHeight * my));

        g.setStroke(new BasicStroke());
        g.fill(arrowPoly);
    }


    public void paintArrows(Graphics _g)
    {
        Graphics2D g = (Graphics2D)_g;

        java.util.List<int[]> pair = Utility.convertMoveToPair(model.getPartialMove());
        g.setColor(arrowColor);
        for (int i = 0; i < pair.size(); i++)
        {
            int ax = (int)(CheckerPanel.SQUARE_WIDTH  * (pair.get(i)[0] % CheckersConstants.W + 0.5));
            int ay = (int)(CheckerPanel.SQUARE_HEIGHT * (pair.get(i)[0] / CheckersConstants.W + 0.5));
            int bx = (int)(CheckerPanel.SQUARE_WIDTH  * (pair.get(i)[1] % CheckersConstants.W + 0.5));
            int by = (int)(CheckerPanel.SQUARE_HEIGHT * (pair.get(i)[1] / CheckersConstants.W + 0.5));
            drawArrow(g, ax, ay, bx, by);
        }
    }

    public void paint(Graphics g)
    {
        /* 
         * Normally paintComponent() is called before paintChildren().
         * Thus we override paint so that arrows are painted on top of the 
         * checker board.
         */
        super.paint(g);
        paintArrows(g);
    }

    public void stateChanged(ChangeEvent e)
    {
    }

    public void actionPerformed(ActionEvent e)
    {
        if (model.getEnabled() == CheckersConstants.NEITHER)
            model.boardPressed();
    }

    public CheckersBoardModel getModel() { return model; }
}
