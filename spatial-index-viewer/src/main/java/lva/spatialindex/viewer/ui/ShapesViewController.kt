package lva.spatialindex.viewer.ui

import lva.spatialindex.viewer.repository.ShapeRepository
import lva.spatialindex.viewer.storage.AbstractShape.Companion.maxOrder
import java.awt.event.MouseEvent

/**
 * @author vlitvinenko
 */
class ShapesViewController(private val shapeRepository: ShapeRepository) {
    private val view = ShapesViewFrame()
    private val visibleShapes = arrayListOf<ShapeUI>()

    init {
        view.onClicked(this::onShapesViewClicked)
        view.onViewportChanged(this::onViewPortChanged)
        view.onClose(this::onClose)
    }

    fun show() {
        view.isVisible = true
    }

    private fun onShapesViewClicked(event: MouseEvent) {
        val viewport = view.viewport
        val x = event.x + viewport.x
        val y = event.y + viewport.y

        val clickedShape = visibleShapes.lastOrNull { it.hitTest(x, y) }
        clickedShape?.let { shape ->
            // push to back with the highest order
            maxOrder += 1
            shape.order = maxOrder
            shape.isActive = !shape.isActive

            shapeRepository.update(shape.unwrapped)

            visibleShapes.remove(shape)
            visibleShapes.add(shape)

            view.update()
        }
    }

    private fun onViewPortChanged() {
        val viewport = view.viewport
        val foundShapes = shapeRepository.search(viewport).asSequence()
            .sortedBy { it.order }
            .map { it.asUI() }
            .toList()

        visibleShapes.clear()
        visibleShapes.addAll(foundShapes)

        view.setShapes(visibleShapes)
        view.update()
    }

    private fun onClose() = shapeRepository.close()

}

fun showShapesRepository(shapeRepository: ShapeRepository) =
    ShapesViewController(shapeRepository).show()
