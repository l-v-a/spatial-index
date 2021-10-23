package lva.spatialindex.viewer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import lva.spatialindex.viewer.repository.ShapeRepository
import lva.spatialindex.viewer.repository.buildShapesRepository
import lva.spatialindex.viewer.ui.ShapesViewController
import lva.spatialindex.viewer.ui.ShapesViewFrame

/**
 * @author vlitvinenko
 */
private fun showShapesRepository(shapeRepository: ShapeRepository) =
    ShapesViewController(ShapesViewFrame(), shapeRepository).run()

suspend fun main(vararg args: String): Unit = coroutineScope {
    if (args.isNotEmpty()) {
        val shapesFile = args[0]
        launch(Dispatchers.Main) {
            buildShapesRepository(shapesFile) { showShapesRepository(it) }
        }
    } else {
        System.err.println("shapes file path is required")
    }
}

