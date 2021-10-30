package lva.spatialindex.viewer.repository

import lva.spatialindex.viewer.storage.CircleShape
import lva.spatialindex.viewer.storage.RectangleShape
import org.slf4j.LoggerFactory

/**
 * @author vlitvinenko
 */
internal object ShapeParser {
    private val log = LoggerFactory.getLogger(ShapeParser::class.java)

    fun parseShape(str: String) =
        parse(str) ?: run { log.warn("Unable to parse shape from $str"); null }

    private fun parse(str: String) = Regex("(\\w+):(.+)").find(str)?.let {
        val (type, params) = it.destructured
        val dims = params.trim()
            .split(Regex("\\s*,\\s*"))
            .map(String::toInt)
        toShape(type.lowercase(), dims)
    }

    private fun toShape(type: String, dims: List<Int>) = when (type) {
        "circle" -> {
            val (x, y, radius) = dims
            CircleShape(x, y, radius)
        }
        "rect" -> {
            val (x, y, width, height) = dims
            RectangleShape(x, y, width, height)
        }
        else -> null
    }
}