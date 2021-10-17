package lva.spatialindex.viewer.controller

import lva.spatialindex.viewer.storage.CircleShape
import lva.spatialindex.viewer.storage.RectangleShape
import java.util.*

/**
 * @author vlitvinenko
 */
internal object ShapeParser {
    @JvmStatic
    fun parseShape(str: String) = Optional.ofNullable(parse(str)) //TODO: for compatibility only. remove later

    private fun parse(str: String) = Regex("(\\w+):(.+)").find(str)?.let {
        val (type, params) = it.destructured
        val dims = params.trim()
            .split(Regex("\\s*,\\s*"))
            .map { p -> p.toInt() }
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