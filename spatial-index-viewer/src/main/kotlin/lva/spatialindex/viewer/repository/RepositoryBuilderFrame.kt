package lva.spatialindex.viewer.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.nio.file.Paths
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar

/**
 * @author vlitvinenko
 */
class RepositoryBuilderFrame private constructor(): JFrame() {
    private val progressBar = JProgressBar()
    private val messageLabel = JLabel()

    init {
        title = "Shape Viewer"
        isResizable = false
        defaultCloseOperation = EXIT_ON_CLOSE

        val panel = JPanel()
        with(panel) {
            layout = BorderLayout()
            border = BorderFactory.createEmptyBorder(7, 7, 7, 7)
            add(messageLabel, BorderLayout.NORTH)
            add(progressBar, BorderLayout.CENTER)
        }
        contentPane = panel
        progressBar.isStringPainted = true

        setBounds(0, 0, 500, 70)
        val dim = Toolkit.getDefaultToolkit().screenSize
        setLocation(dim.width / 2 - size.width / 2, dim.height / 2 - size.height / 2)
    }

    private fun onClose(block: () -> Unit) = addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent?) {
            block()
        }
    })

    companion object {
        suspend fun buildShapesRepository(shapesFile: String): ShapeRepository = coroutineScope {
            with(RepositoryBuilderFrame()) {
                messageLabel.text = "indexing..."
                isVisible = true

                val buildResult = async {
                    ShapesRepositoryBuilder.build(Paths.get(shapesFile)) { progress ->
                        withContext(Dispatchers.Main) {
                            progressBar.value = progress
                        }
                    }
                }

                onClose {
                    buildResult.cancel()
                }

                val repository = buildResult.await()
                isVisible = false

                repository
            }
        }
    }
}
