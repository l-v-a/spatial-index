package lva.spatialindex.viewer.storage

import java.awt.Rectangle

/**
 * @author vlitvinenko
 */

interface Shape {
    val mbr: Rectangle
    var isActive: Boolean
    var order: Int
    var offset: Long
}

abstract class AbstractShape : Shape {
    override var isActive = false
    override var order = 0
    override var offset: Long = 0

    companion object {
        var maxOrder: Int = 0
    }
}

class CircleShape(val x: Int = 0, val y: Int = 0, val radius: Int = 0) : AbstractShape() {
    override val mbr get() = Rectangle(x - radius, y - radius, radius * 2, radius * 2)
}

class RectangleShape(val rectangle: Rectangle = Rectangle(0, 0, 0, 0)) : AbstractShape() {
    constructor(x: Int, y: Int, width: Int, height: Int) :
            this(Rectangle(x, y, width, height))

    override val mbr get() = Rectangle(rectangle)
}
