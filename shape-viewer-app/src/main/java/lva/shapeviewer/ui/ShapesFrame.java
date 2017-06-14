package lva.shapeviewer.ui;

import com.google.common.collect.Lists;
import lva.shapeviewer.Shape;
import lva.shapeviewer.ShapeRepository;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.*;

public class ShapesFrame extends JFrame {
    private final Canvas canvas;
    private ShapeRepository shapeRepository;
    private List<Shape> shapes = new ArrayList<>();
    private final JScrollBar hbar;
    private final JScrollBar vbar;

    public ShapesFrame(ShapeRepository shapeRepository) {
        this.shapeRepository = shapeRepository;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(0, 0, 1000, 800);

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
        setTitle("Shape Viewer");

        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                canvasResized(e);
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                canvasClicked(e);
            }
        });

        hbar.addAdjustmentListener(this::scrollValueChanged);
        vbar.addAdjustmentListener(this::scrollValueChanged);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeResources();
                super.windowClosing(e);
            }
        });

    }

    private static final Dimension PANE_SIZE = new Dimension(5000, 5000);
    private Dimension getPaneSize() {
        return PANE_SIZE;
    }

    private void canvasResized(ComponentEvent event) {
        Dimension size = canvas.getSize();

        setScrollbarAmount(hbar, size.width);
        setScrollbarAmount(vbar, size.height);

        update();
    }

    private void canvasClicked(MouseEvent event) {
        Rectangle viewport = getViewport();
        // translate to viewport
        int x = event.getX() + viewport.x;
        int y = event.getY() + viewport.y;

        Shape clickedShape = null;
        Iterator<Shape> shapeIterator = Lists.reverse(shapes).iterator();
        while (shapeIterator.hasNext()) {
            Shape shape = shapeIterator.next();
            if (shape.hitTest(x, y)) {
                clickedShape = shape;
                break;
            }
        }

        if (clickedShape == null) {
            // nothing to do
            return;
        }

        clickedShape.setActive(!clickedShape.isActive());
// for reorder
//        if (shapes.size() > 1) {
//            // TODO: BUG: must use maxOrder over all elements
//            clickedShape.setOrder(Iterables.getLast(shapes).getOrder() + 1); // TODO: think about overflow
//        }

        shapeRepository.update(clickedShape);

        // push to back with highest order
// for reorder
//        shapeIterator.remove();
//        shapes.add(clickedShape);

        canvas.repaint();


//        // TODO: BUG: must store reference to active shapes
//        // TODO: think about to use LinkedList for shapes
//        // find shape
//        Shape clickedShape = null;
//        List<Shape> activeShapes = new ArrayList<>();
//
//        // TODO: use iterator
//        for(Shape shape: shapes) {
//            if (shape.isActive()) {
//                activeShapes.add(shape);
//            }
//            if (shape.hitTest(x, y)) {
//                if (clickedShape == null || clickedShape.getOrder() < shape.getOrder()) {
//                    clickedShape = shape;
//                }
//            }
//        }
//        // set as active
//        if (clickedShape != null && !clickedShape.isActive()) {
//            clickedShape.setActive(true);
//            try {
//                dbStorage.write(clickedShape.getOffset(), clickedShape);
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
//
//            // push to back with highest order
//            int maxOrder = shapes.get(shapes.size() - 1).getOrder();
//            clickedShape.setOrder(maxOrder + 1); // TODO: think about overflow
//
//            // TODO: use iterator
//            shapes.remove(clickedShape); // TODO: think about memory usage
//            shapes.add(clickedShape);
//        }
//
//        // reset prev active
//        for (Shape shape: activeShapes) {
//            shape.setActive(false);
//            try {
//                dbStorage.write(shape.getOffset(), shape);
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
//        }
//        // re-render
//        canvas.repaint();

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


    private void scrollValueChanged(AdjustmentEvent event) {
        update();
    }

    private Rectangle getViewport() {
        return new Rectangle(hbar.getValue(), vbar.getValue(),
            hbar.getVisibleAmount(), vbar.getVisibleAmount());
    }

    private void update() {
        Rectangle viewport = getViewport();

        try {
            shapes = shapeRepository.search(viewport);
            Collections.sort(shapes, Comparator.comparing(Shape::getOrder, Integer::compare));

            canvas.setViewport(viewport);
            canvas.setShapes(shapes);
            canvas.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeResources() {
        shapeRepository.close();
    }
}



