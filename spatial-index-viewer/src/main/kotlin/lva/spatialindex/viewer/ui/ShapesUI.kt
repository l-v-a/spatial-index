package lva.spatialindex.viewer.ui

import lva.spatialindex.viewer.storage.CircleShape
import lva.spatialindex.viewer.storage.RectangleShape
import lva.spatialindex.viewer.storage.Shape
import java.awt.Color
import java.awt.Graphics
import java.awt.Point

/**
 * @author vlitvinenko
 */

interface ShapeUI : Shape {
    fun draw(graphics: Graphics)
    fun hitTest(x: Int, y: Int): Boolean
    val unwrapped: Shape
}

abstract class AbstractShapeUI : ShapeUI {
    val bgColor: Color
        get() = if (isActive) Color.GRAY else Color.LIGHT_GRAY

    val borderColor: Color = Color.BLACK
}

class CircleShapeUI(private val circle: CircleShape) : AbstractShapeUI(), Shape by circle {
    override val unwrapped = circle

    override fun draw(graphics: Graphics) = with(graphics) {
        val boundRect = mbr
        color = bgColor
        fillOval(boundRect.x, boundRect.y, boundRect.width, boundRect.height)
        color = borderColor
        drawOval(boundRect.x, boundRect.y, boundRect.width, boundRect.height)
    }

    override fun hitTest(x: Int, y: Int) = with(circle) {
        (x - this.x) * (x - this.x) + (y - this.y) * (y - this.y) <= radius * radius
    }
}

class RectangleShapeUI(private val rectShape: RectangleShape) : AbstractShapeUI(), Shape by rectShape {
    override val unwrapped = rectShape

    override fun draw(graphics: Graphics) = with(graphics) {
        with(rectShape) {
            color = bgColor
            fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
            color = borderColor
            drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
        }
    }

    override fun hitTest(x: Int, y: Int) = Point(x, y) in rectShape.rectangle
}

fun Shape.asUI(): ShapeUI = when (this) {
    is CircleShape -> CircleShapeUI(this)
    is RectangleShape -> RectangleShapeUI(this)
    else -> error("Unknown shape type ${this::class.simpleName}")
}

