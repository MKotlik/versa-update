package checkers.ui;

import static checkers.CheckersConstants.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class CheckerPanel extends JComponent implements ChangeListener, ActionListener
{
    public final static int SQUARE_WIDTH = 50;
    public final static int SQUARE_HEIGHT = 50;

    protected int index;
    protected checkers.ui.CheckersBoardModel cbm;
    protected ButtonModel model;

    public CheckerPanel(checkers.ui.CheckersBoardModel cbm, int index)
    {
        this.model = new DefaultButtonModel();

        this.index = index;
        this.cbm = cbm;

        setPreferredSize(new Dimension(SQUARE_WIDTH, SQUARE_HEIGHT));

        loadImages();

        if ( (index + index/8) % 2 == 0 )
            setBackground(Color.WHITE);
        else
            setBackground(Color.BLACK);

        cbm.addChangeListener(this);
        model.addActionListener(this);

        Listener listener = new Listener();
        this.addMouseListener(listener);
        this.addFocusListener(listener);
        //this.addChangeListener(listener);
    }

    protected static int focusBorderLength = 12;
    protected static int focusBorderWidth = 2;
    protected static Polygon focusPoly;
    static
    {
        int a = focusBorderLength;
        int b = focusBorderWidth;
        focusPoly = new Polygon();
        focusPoly.addPoint(0, 0);
        focusPoly.addPoint(0, a);
        focusPoly.addPoint(b, a);
        focusPoly.addPoint(b, b);
        focusPoly.addPoint(a, b);
        focusPoly.addPoint(a, 0);
    }

    public void paintBorder(Graphics _g)
    {
        Graphics2D g = (Graphics2D)_g;

        /* If has focus, paint focus border */
        if (hasFocus())
        {
            g.setColor(Color.BLACK);
            for (int i = 0; i < 4; i++)
            {
                g.draw(focusPoly);
                g.rotate(Math.PI / 2, (getWidth() - 1.0) / 2.0, (getHeight() - 1.0) / 2.0);
            }
        }
    }

    protected final static Color hintValidColor = new Color(0, 255, 0, 128);
    protected final static Color hintInvalidColor = new Color(255, 0, 0, 128);

    public void paintComponent(Graphics _g)
    {
        Graphics2D g = (Graphics2D)_g;

        Rectangle rectAll = new Rectangle(0, 0, getWidth(), getHeight());

        g.setColor(getBackground());
        g.fill(rectAll);

        if (cbm.getHint(index) == checkers.ui.CheckersBoardModel.HINT_VALID)
        {
            g.setColor(hintValidColor);
            g.fill(rectAll);
        }

        if (cbm.getHint(index) == checkers.ui.CheckersBoardModel.HINT_INVALID)
        {
            g.setColor(hintInvalidColor);
            g.fill(rectAll);
        }

        g.drawImage(images[cbm.getPiece(index)], 0, 0, null);
    }

    public ButtonModel getModel() { return model; }

    public void stateChanged(ChangeEvent e)
    {
        repaint();
    }

    public void actionPerformed(ActionEvent e)
    {
        if (cbm.getEnabled() != NEITHER)
            cbm.squarePressed(index);
    }

    protected static Image[] images;

    protected static boolean imagesLoaded = false;


    public void loadImages()
    {
        synchronized (CheckerPanel.class)
        {
            if (imagesLoaded)
                return;

            Toolkit tk = Toolkit.getDefaultToolkit();

            images = new Image[PIECES_MAX];
            images[RED_PAWN] = tk.getImage( this.getClass().getResource("images/red_piece.png") );
            images[BLUE_PAWN] = tk.getImage( this.getClass().getResource("images/blue_piece.png") );
            images[RED_KING] = tk.getImage( this.getClass().getResource("images/red_piece_king.png") );
            images[BLUE_KING] = tk.getImage( this.getClass().getResource("images/blue_piece_king.png") );

            MediaTracker mediaTracker = new MediaTracker(this);
            for (int i : new int[] {RED_PAWN, BLUE_PAWN, RED_KING, BLUE_KING} )
                mediaTracker.addImage(images[i], 0);

            try {
                mediaTracker.waitForAll();
            } catch ( InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    class Listener implements MouseListener, FocusListener, ChangeListener
    {
        public void stateChanged(ChangeEvent e)
        {
            repaint();
        }

        public void focusGained(FocusEvent e)
        {
            repaint();
        }

        public void focusLost(FocusEvent e)
        {
            model.setArmed(false);
            model.setPressed(false);

            repaint();
        }

        public void mouseClicked(MouseEvent e)
        {
        }

        public void mousePressed(MouseEvent e)
        {
            if ( !contains(e.getX(), e.getY()) )
                return;

            if (!model.isEnabled())
                return;

            if ( SwingUtilities.isLeftMouseButton(e) )
            {
                model.setArmed(true);
                model.setPressed(true);
            }
        }

        public void mouseReleased(MouseEvent e)
        {
            if ( SwingUtilities.isLeftMouseButton(e) )
            {
                model.setPressed(false);
                model.setArmed(false);
            }
        }

        public void mouseEntered(MouseEvent e)
        {
            if (!hasFocus())
                requestFocusInWindow();

            //model.setRollover(true);
            if (model.isPressed())
                model.setArmed(true);
        }

        public void mouseExited(MouseEvent e)
        {
            //model.setRollover(false);
            model.setArmed(false);
        }
    }
}
