package lva.spatialindex.viewer.repository

import lva.spatialindex.index.Index
import lva.spatialindex.storage.Storage
import lva.spatialindex.viewer.storage.Shape
import lva.spatialindex.viewer.utils.AutoCloseables.close
import java.awt.Rectangle

/**
 * @author vlitvinenko
 */
class ShapeRepository(private val shapeStorage: Storage<Shape>, private val index: Index) : AutoCloseable {
    fun search(area: Rectangle) =
        index.search(area).map { shapeStorage.read(it) }.toList()

    fun update(shape: Shape) = shapeStorage.write(shape.offset, shape)

    override fun close() = close(listOf(index, shapeStorage))
}