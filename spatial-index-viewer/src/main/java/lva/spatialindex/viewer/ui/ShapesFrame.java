package lva.spatialindex.viewer.ui;

import lombok.NonNull;
import lva.spatialindex.viewer.storage.Shape;
import lva.spatialindex.viewer.utils.EventSource.EventStore;
import lva.spatialindex.viewer.utils.EventSource.TypedEventStore;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;


public class ShapesFrame extends JFrame {
    public final EventStore closingEvent = new EventStore();
    public final EventStore viewPortChangedEvent  = new EventStore();
    public final TypedEventStore<MouseEvent> clickedEvent = new TypedEventStore<>();

    private static final Dimension PANE_SIZE = new Dimension(5000, 5000);

    private final Canvas canvas = new Canvas();
    private final JScrollBar hbar = new JScrollBar(JScrollBar.HORIZONTAL);
    private final JScrollBar vbar = new JScrollBar(JScrollBar.VERTICAL);

    public ShapesFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(0, 0, 1000, 800);
        setTitle("Shape Viewer");

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

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
                handleCanvasResized(e);
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickedEvent.dispatch(e);
            }
        });

        hbar.addAdjustmentListener((e) -> viewportChanged());
        vbar.addAdjustmentListener((e) -> viewportChanged());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closingEvent.dispatch();
            }
        });


        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
    }

    @NonNull
    public Rectangle getViewport() {
        return new Rectangle(hbar.getValue(), vbar.getValue(),
            hbar.getVisibleAmount(), vbar.getVisibleAmount());
    }

    public void setShapes(@NonNull Collection<Shape> shapes) {
        canvas.shapes = shapes;
    }

    public void update() {
        canvas.repaint();
    }

    private Dimension getPaneSize() {
        return PANE_SIZE;
    }

    private void handleCanvasResized(ComponentEvent event) {
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
        canvas.viewport = getViewport();
        viewPortChangedEvent.dispatch();
    }

    private static class Canvas extends JComponent {
        private Rectangle viewport = new Rectangle();
        private Collection<Shape> shapes = new ArrayList<>();

        @Override
        public void paint(Graphics g) {
            g.translate(-viewport.x, -viewport.y);
            for (Shape shape : shapes) {
                shape.draw(g);
            }
        }
    }
}



