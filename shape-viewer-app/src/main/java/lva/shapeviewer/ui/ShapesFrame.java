package lva.shapeviewer.ui;

import lombok.Setter;
import lva.shapeviewer.Shape;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.Collection;

public class ShapesFrame extends JFrame {
    public interface ShapeViewListener {
        void clicked(MouseEvent event);
        void viewPortChanged();
        void closing();
    }

    private static final ShapeViewListener NULL_LISTENER = new ShapeViewListener() {
        @Override
        public void clicked(MouseEvent event) {}

        @Override
        public void viewPortChanged() {}

        @Override
        public void closing() {}
    };

    private static final Dimension PANE_SIZE = new Dimension(5000, 5000);

    @Setter
    private ShapeViewListener viewListener = NULL_LISTENER;

    private final Canvas canvas;
    private final JScrollBar hbar;
    private final JScrollBar vbar;

    public ShapesFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(0, 0, 1000, 800);
        setTitle("Shape Viewer");

        canvas = new Canvas();

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        hbar = new JScrollBar(JScrollBar.HORIZONTAL);
        vbar = new JScrollBar(JScrollBar.VERTICAL);

        Dimension paneSize = getPaneSize();
        hbar.setMaximum(paneSize.width);
        vbar.setMaximum(paneSize.height);

        hbar.setBlockIncrement(paneSize.width / 10);
        vbar.setBlockIncrement(paneSize.height / 10);

        panel.add(hbar, BorderLayout.SOUTH);
        panel.add(vbar, BorderLayout.EAST);
        panel.add(canvas, BorderLayout.CENTER);

        setContentPane(panel);

        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                canvasResized(e);
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                viewListener.clicked(e);
            }
        });

        hbar.addAdjustmentListener((e) -> viewportChanged());
        vbar.addAdjustmentListener((e) -> viewportChanged());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                viewListener.closing();
            }
        });


        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
    }

    public Rectangle getViewport() {
        return new Rectangle(hbar.getValue(), vbar.getValue(),
            hbar.getVisibleAmount(), vbar.getVisibleAmount());
    }

    public void setShapes(Collection<Shape> shapes) {
        canvas.setShapes(shapes);
    }

    public void update() {
        canvas.repaint();
    }

    private Dimension getPaneSize() {
        return PANE_SIZE;
    }

    private void canvasResized(ComponentEvent event) {
        Dimension size = canvas.getSize();

        setScrollbarAmount(hbar, size.width);
        setScrollbarAmount(vbar, size.height);

        viewportChanged();
    }

    private static void setScrollbarAmount(JScrollBar bar, int amount) {
        bar.setVisibleAmount(amount);
        int newAmount = bar.getVisibleAmount();
        if (newAmount != amount) {
            int value = bar.getValue();
            value -= (amount - newAmount);

            bar.setValueIsAdjusting(true);
            bar.setValue(value);
            bar.setValueIsAdjusting(false);

            bar.setVisibleAmount(amount);
        }
    }

    private void viewportChanged() {
        canvas.setViewport(getViewport());
        viewListener.viewPortChanged();
    }
}



