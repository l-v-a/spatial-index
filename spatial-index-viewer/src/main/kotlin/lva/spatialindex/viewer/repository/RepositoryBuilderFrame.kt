package lva.spatialindex.viewer.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lva.spatialindex.viewer.ui.UIScope
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar

/**
 * @author vlitvinenko
 */
class RepositoryBuilderFrame : JFrame(), UIScope {
    private val progressBar = JProgressBar()
    private val messageLabel = JLabel()

    init {
        val panel = JPanel()
        with(panel) {
            layout = BorderLayout()
            border = BorderFactory.createEmptyBorder(7, 7, 7, 7)
            add(messageLabel, BorderLayout.NORTH)
            add(progressBar, BorderLayout.CENTER)
        }

        defaultCloseOperation = EXIT_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                job.cancel()
            }
        })

        progressBar.isStringPainted = true
        isResizable = false
        title = "Shape Viewer"
        contentPane = panel

        setBounds(0, 0, 500, 70)
        val dim = Toolkit.getDefaultToolkit().screenSize
        setLocation(dim.width / 2 - size.width / 2, dim.height / 2 - size.height / 2)
    }

    override val job = Job()

    private fun setProgress(value: Int) {
        progressBar.value = value
    }

    fun setMessage(message: String) {
        messageLabel.text = message
    }

    fun startProcessing(shapesFile: Path, onComplete: (ShapeRepository) -> Unit) {
        launch {
            val repository = ShapesRepositoryBuilder.build(shapesFile) {
                withContext(Dispatchers.Main) {
                    setProgress(it)
                }
            }

            onComplete(repository)
        }
    }
}


fun buildShapesRepository(shapesFile: String, onComplete: (ShapeRepository) -> Unit) = with(RepositoryBuilderFrame()) {
    setMessage("indexing...")
    isVisible = true

    startProcessing(Paths.get(shapesFile)) {
        isVisible = false
        onComplete(it)
    }
}