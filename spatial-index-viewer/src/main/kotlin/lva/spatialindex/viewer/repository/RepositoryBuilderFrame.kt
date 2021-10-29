package lva.spatialindex.viewer.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
class RepositoryBuilderFrame : JFrame() {
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

    fun onClose(block: () -> Unit) = addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent?) {
            block()
        }
    })

    fun setProgress(value: Int) {
        progressBar.value = value
    }

    fun setMessage(message: String) {
        messageLabel.text = message
    }
}

fun CoroutineScope.buildShapesRepository(shapesFile: String, onComplete: (ShapeRepository) -> Unit) = with(RepositoryBuilderFrame()) {
    setMessage("indexing...")
    isVisible = true

    val job = launch {
        val repository = ShapesRepositoryBuilder.build(Paths.get(shapesFile)) {
            withContext(Dispatchers.Main) {
                setProgress(it)
            }
        }

        onComplete(repository)
        isVisible = false
    }

    onClose {
        job.cancel()
    }
}