package lva.spatialindex.viewer.ui

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import lva.spatialindex.viewer.repository.ShapeRepository
import lva.spatialindex.viewer.storage.AbstractShape.Companion.maxOrder
import java.awt.event.MouseEvent


/**
 * @author vlitvinenko
 */
class ShapesViewController private constructor(private val shapeRepository: ShapeRepository) {
    private val view = ShapesViewFrame()
    private var visibleShapes = mutableListOf<ShapeUI>()

    init {
        view.onClicked(this::onShapesViewClicked)
        view.onClose(this::onClose)
    }

    private suspend fun run() = coroutineScope {
        view.viewportChanges().debounce(50)
            .onEach { update() }
            .launchIn(this)
        view.isVisible = true
    }

    private fun onShapesViewClicked(event: MouseEvent) {
        val viewport = view.viewport
        val x = event.x + viewport.x
        val y = event.y + viewport.y

        val clickedShape = visibleShapes.lastOrNull { it.hitTest(x, y) }
        clickedShape?.let { shape ->
            shape.order = ++maxOrder
            shape.isActive = !shape.isActive

            shapeRepository.update(shape.unwrapped)

            // push to back with the highest order
            visibleShapes -= shape
            visibleShapes += shape

            view.update()
        }
    }

    private suspend fun update() {
        visibleShapes = shapeRepository.search(view.viewport).asSequence()
            .sortedBy { it.order }
            .map { it.asUI() }
            .toMutableList()

        view.setShapes(visibleShapes)
        view.update()
    }

    private fun onClose() =
        shapeRepository.close()

    companion object {
        suspend fun showShapesRepository(shapeRepository: ShapeRepository) =
            ShapesViewController(shapeRepository).run()
    }
}