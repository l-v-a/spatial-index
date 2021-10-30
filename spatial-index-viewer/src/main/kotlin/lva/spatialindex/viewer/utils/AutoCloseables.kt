package lva.spatialindex.viewer.utils

/**
 * Eliminates lack of AutoCloseable support in Guava's Closer class
 *
 * @author vlitvinenko
 */
fun <T : AutoCloseable> close(closeables: Collection<T>, wasThrown: Exception? = null) {
    var exception = wasThrown
    closeables.forEach { closeable ->
        try {
            closeable.close()
        } catch (e: Exception) {
            exception?.addSuppressed(e) ?: run { exception = e }
        }
    }

    exception?.let { throw it }
}
