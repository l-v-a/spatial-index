package lva.spatialindex.viewer.storage

import java.awt.Color
import java.awt.Graphics
import java.awt.Point
import java.awt.Rectangle

/**
 * @author vlitvinenko
 */

interface Shape {
    val mbr: Rectangle
    var isActive: Boolean
    var order: Int
    var maxOrder: Int
    var offset: Long
    fun draw(graphics: Graphics)
    fun hitTest(x: Int, y: Int): Boolean
}


private var maxOrderImpl: Int = 0

abstract class AbstractShape : Shape {
    override var isActive = false
    override var order = 0
    override var offset: Long = 0

    // override var maxOrder by ::maxOrderImpl //FIXME:
    override var maxOrder
        get() = maxOrderImpl
        set(value) { maxOrderImpl = value }
}


class CircleShape(private val x: Int = 0, private val y: Int = 0, private val radius: Int = 0) : AbstractShape() {
    override val mbr: Rectangle
        get() = Rectangle(x - radius, y - radius, radius * 2, radius * 2)

    override fun draw(graphics: Graphics) = with(graphics) {
        val boundRect = mbr
        color = Color.LIGHT_GRAY
        fillOval(boundRect.x, boundRect.y, boundRect.width, boundRect.height)
        color = if (isActive) Color.RED else Color.BLACK
        drawOval(boundRect.x, boundRect.y, boundRect.width, boundRect.height)
    }

    override fun hitTest(x: Int, y: Int) =
        (x - this.x) * (x - this.x) + (y - this.y) * (y - this.y) <= radius * radius
}


class RectangleShape(private val rectangle: Rectangle = Rectangle(0, 0, 0, 0)) : AbstractShape() {
    constructor(x: Int, y: Int, width: Int, height: Int) : this(Rectangle(x, y, width, height))

    override val mbr: Rectangle
        get() = Rectangle(rectangle)

    override fun draw(graphics: Graphics) = with(graphics) {
        color = Color.LIGHT_GRAY
        fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
        color = if (isActive) Color.RED else Color.BLACK
        drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
    }

    override fun hitTest(x: Int, y: Int) = Point(x, y) in rectangle
}