package lva.spatialindex.viewer.ui

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollBar

class ShapesViewFrame : JFrame() {
    private val canvas = Canvas()
    private val hbar = JScrollBar(JScrollBar.HORIZONTAL)
    private val vbar = JScrollBar(JScrollBar.VERTICAL)
    private var onViewportChanged: () -> Unit = {  }

    val viewport
        get() = Rectangle(hbar.value, vbar.value, hbar.visibleAmount, vbar.visibleAmount)

    init {
        title = "Shape Viewer"
        defaultCloseOperation = EXIT_ON_CLOSE

        val paneSize = PANE_SIZE
        hbar.maximum = paneSize.width
        vbar.maximum = paneSize.height
        hbar.blockIncrement = paneSize.width / 10
        vbar.blockIncrement = paneSize.height / 10
        hbar.addAdjustmentListener { handleViewportChanged() }
        vbar.addAdjustmentListener { handleViewportChanged() }

        val panel = JPanel()
        with(panel) {
            layout = BorderLayout()
            add(hbar, BorderLayout.SOUTH)
            add(vbar, BorderLayout.EAST)
            add(canvas, BorderLayout.CENTER)
        }
        contentPane = panel

        setBounds(0, 0, 1000, 800)
        val dim = Toolkit.getDefaultToolkit().screenSize
        setLocation(dim.width / 2 - size.width / 2, dim.height / 2 - size.height / 2)

        canvas.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                handleCanvasResized()
            }
        })
    }

    fun onClicked(block: (e: MouseEvent) -> Unit) = canvas.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            block(e)
        }
    })

    fun viewportChanges(): Flow<Unit> = callbackFlow {
        onViewportChanged = { trySend(Unit) }
        awaitClose {}
    }

    fun setShapes(shapes: Collection<ShapeUI>) {
        canvas.shapes = shapes
    }

    fun update() =
        canvas.repaint()

    private fun handleCanvasResized() {
        val size = canvas.size
        hbar.adjustVisibleAmount(size.width)
        vbar.adjustVisibleAmount(size.height)
        handleViewportChanged()
    }

    private fun handleViewportChanged() {
        canvas.viewport = viewport
        onViewportChanged()
    }

    private class Canvas : JComponent() {
        var viewport = Rectangle()
        var shapes: Collection<ShapeUI> = listOf()

        override fun paint(graphics: Graphics) {
            graphics.translate(-viewport.x, -viewport.y)
            shapes.forEach { it.draw(graphics) }
        }
    }

    companion object {
        private val PANE_SIZE = Dimension(5000, 5000)
    }
}

private fun JScrollBar.adjustVisibleAmount(amount: Int) {
    visibleAmount = amount
    val newAmount = visibleAmount
    if (newAmount != amount) {
        val newValue = value - (amount - newAmount)

        valueIsAdjusting = true
        value = newValue
        valueIsAdjusting = false

        visibleAmount = amount
    }
}
