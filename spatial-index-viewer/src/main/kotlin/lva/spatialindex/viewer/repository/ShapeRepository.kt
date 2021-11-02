package lva.spatialindex.viewer.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import lva.spatialindex.index.Index
import lva.spatialindex.storage.Storage
import lva.spatialindex.viewer.storage.Shape
import java.awt.Rectangle


/**
 * @author vlitvinenko
 */
class ShapeRepository(private val shapeStorage: Storage<Shape>, private val indexes: Collection<Index>) {
    suspend fun search(area: Rectangle) : Collection<Shape> = coroutineScope {
        val searchResults = indexes.map { index ->
            async(Dispatchers.IO) {
                index.search(area)
            }
        }

        val offsets = searchResults.awaitAll().flatten()
        offsets.map { shapeStorage.read(it) }.toList()
    }

    fun update(shape: Shape) =
        shapeStorage.write(shape.offset, shape)
}

