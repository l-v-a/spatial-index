package lva.spatialindex.viewer.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import lva.spatialindex.index.Index
import lva.spatialindex.index.RStarTree
import lva.spatialindex.storage.Storage
import lva.spatialindex.viewer.index.MultiIndex
import lva.spatialindex.viewer.model.ShapeRepository
import lva.spatialindex.viewer.storage.Shape
import lva.spatialindex.viewer.storage.ShapeStorage
import lva.spatialindex.viewer.utils.AutoCloseables.close
import org.slf4j.LoggerFactory
import java.awt.Rectangle
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.max
import kotlin.math.min


private data class IndexData(val offset: Long, val mbr: Rectangle)

object ShapesRepositoryBuilder {
    private const val MAX_ELEMENTS_IN_TREE = 1000 * 1000
    private const val STORAGE_INITIAL_SIZE = 64 * 1024L * 1024L
    private const val SHAPES_QUEUE_CAPACITY = 1000 * 1000
    private const val SIZE_OF_SHAPE_BYTES_AVG = 26
    private const val DB_FILE_NAME = "shapes.bin"

    private val log = LoggerFactory.getLogger(ShapesRepositoryBuilder::class.java)

    suspend fun build(shapesFile: Path, onProgress: suspend (Int) -> Unit): ShapeRepository {
        val shapeStorage = ShapeStorage(shapesFile.resolveSibling(DB_FILE_NAME).toString(), STORAGE_INITIAL_SIZE)
        return try {
            val index = MultiIndex(buildIndexes(shapeStorage, shapesFile, onProgress))
            ShapeRepository(shapeStorage, index)
        } catch (e: Exception) {
            log.error("Unable to create repository for $shapesFile", e)
            close(listOf(shapeStorage))
            throw e
        }
    }

    private suspend fun buildIndexes(
        storage: Storage<Shape>,
        shapesFile: Path,
        onProgress: suspend (Int) -> Unit
    ): Collection<Index> = coroutineScope {

        val itemsEstimated = max(Files.size(shapesFile) / SIZE_OF_SHAPE_BYTES_AVG, 100).toInt()
        val numOfTasks = (itemsEstimated + MAX_ELEMENTS_IN_TREE - 1) / MAX_ELEMENTS_IN_TREE
        var itemsProcessed = 0;

        suspend fun onItemIndexed() {
            withContext(coroutineContext) {
                val percentage = (++itemsProcessed * 100) / itemsEstimated
                onProgress(min(percentage, 100))
            }
        }

        log.info("Starting build indexes. shapes number est.: $itemsEstimated, tasks number: $numOfTasks")
        onProgress(0)

        val shapes = readShapes(shapesFile)
        val shapesToIndex = commitShapes(shapes, storage)
        val deferIndexes = (1..numOfTasks).map {
            async(Dispatchers.IO) {
                indexShapes(it, shapesFile.parent, shapesToIndex) { onItemIndexed() }
            }
        }

        val indexes = deferIndexes.awaitAll()
        onProgress(100)

        indexes
    }

    private fun CoroutineScope.readShapes(shapesFile: Path): ReceiveChannel<Shape> = produce(capacity = UNLIMITED) {
        val reader = Files.newBufferedReader(shapesFile)
        val shapes = reader.use {
            it.lineSequence()
                .filter(String::isNotBlank)
                .map(ShapeParser::parseShape)
                .filterNotNull()
                .toList()
        }

        for (shape in shapes) {
            send(shape)
        }
    }

    private fun CoroutineScope.commitShapes(
        shapes: ReceiveChannel<Shape>,
        storage: Storage<Shape>
    ): ReceiveChannel<IndexData> = produce(capacity = SHAPES_QUEUE_CAPACITY) {
        for (shape in shapes) {
            shape.maxOrder += 1
            shape.order = shape.maxOrder
            send(IndexData(storage.add(shape), shape.mbr))
        }
    }

    private suspend fun indexShapes(
        taskNumber: Int,
        indexPath: Path,
        toIndex: ReceiveChannel<IndexData>,
        onItem: suspend () -> Unit
    ): Index {
        log.info("Starting indexing task $taskNumber")

        val storageFile = indexPath.resolve("shapes_idx$taskNumber.bin")
        val indexTree = RStarTree(MAX_ELEMENTS_IN_TREE, storageFile.toString())

        return try {
            var numOfElements = 0
            for (dataToIndex in toIndex) {
                indexTree.insert(dataToIndex.offset, dataToIndex.mbr)
                onItem()
                if (++numOfElements >= MAX_ELEMENTS_IN_TREE) break
            }

            indexTree
        } catch (e: Exception) {
            log.error("task $taskNumber closed by exception", e)
            close(setOf(indexTree))
            Files.deleteIfExists(storageFile)
            throw e;
        }
    }
}