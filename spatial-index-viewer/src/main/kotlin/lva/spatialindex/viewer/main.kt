package lva.spatialindex.viewer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import lva.spatialindex.viewer.repository.RepositoryBuilderFrame.Companion.buildShapesRepository
import lva.spatialindex.viewer.ui.ShapesViewController.Companion.showShapesRepository

/**
 * @author vlitvinenko
 */
suspend fun main(vararg args: String): Unit = coroutineScope {
    args.firstOrNull()?.let { shapesFile ->
        launch(Dispatchers.Main) {
            val repository = buildShapesRepository(shapesFile)
            showShapesRepository(repository)
        }
    } ?: run {
        System.err.println("shapes file path is required")
    }
}
