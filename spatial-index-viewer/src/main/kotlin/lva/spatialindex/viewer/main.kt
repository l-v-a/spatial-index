package lva.spatialindex.viewer

import lva.spatialindex.viewer.repository.ShapeRepository
import lva.spatialindex.viewer.repository.buildShapesRepository
import lva.spatialindex.viewer.ui.ShapesViewController
import lva.spatialindex.viewer.ui.ShapesViewFrame
import javax.swing.SwingUtilities

/**
 * @author vlitvinenko
 */
private fun showShapesRepository(shapeRepository: ShapeRepository) =
    ShapesViewController(ShapesViewFrame(), shapeRepository).run()

fun main(vararg args: String) {
    if (args.isNotEmpty()) {
        val shapesFile = args[0]
        SwingUtilities.invokeLater {
            buildShapesRepository(shapesFile) { showShapesRepository(it) }
        }
    } else {
        System.err.println("shapes file path is required")
    }
}
