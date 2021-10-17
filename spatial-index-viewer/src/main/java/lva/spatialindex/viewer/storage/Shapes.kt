package lva.spatialindex.viewer.storage

import java.awt.Color
import java.awt.Graphics
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
    fun draw(g: Graphics)
    fun hitTest(x: Int, y: Int): Boolean
}


private var maxOrderImpl: Int = 0

abstract class AbstractShape : Shape {
    override var isActive = false
    override var order = 0
    override var offset: Long = 0

    // override var maxOrder by ::maxOrderImpl // TODO: fix it
    override var maxOrder
        get() = maxOrderImpl
        set(value) { maxOrderImpl = value }
}


class CircleShape(private val x: Int, private val y: Int, private val radius: Int) : AbstractShape() {
    private constructor() : this(0, 0, 0) // for deserialization

    override val mbr: Rectangle
        get() = Rectangle(x - radius, y - radius, radius * 2, radius * 2)

    override fun draw(g: Graphics) {
        val boundRect = mbr
        g.color = Color.LIGHT_GRAY
        g.fillOval(boundRect.x, boundRect.y, boundRect.width, boundRect.height)
        g.color = if (isActive) Color.RED else Color.BLACK
        g.drawOval(boundRect.x, boundRect.y, boundRect.width, boundRect.height)
    }

    override fun hitTest(x: Int, y: Int) =
        (x - this.x) * (x - this.x) + (y - this.y) * (y - this.y) <= radius * radius
}


class RectangleShape(private val rectangle: Rectangle) : AbstractShape() {
    constructor(x: Int, y: Int, width: Int, height: Int) : this(Rectangle(x, y, width, height))
    private constructor() : this(0, 0, 0, 0) // for deserialization

    override val mbr: Rectangle
        get() = Rectangle(rectangle)

    override fun draw(g: Graphics) {
        g.color = Color.LIGHT_GRAY
        g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
        g.color = if (isActive) Color.RED else Color.BLACK
        g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
    }

    override fun hitTest(x: Int, y: Int) = rectangle.contains(x, y)
}