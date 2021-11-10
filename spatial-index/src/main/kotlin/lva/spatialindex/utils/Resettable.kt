package lva.spatialindex.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author vlitvinenko
 */

fun <V: Any> resettableWith(uninitializedValue: V, init: () -> V) = Resettable(uninitializedValue, init)

class Resettable<V: Any>(private val uninitializedValue: V, private val init: () -> V) : ReadWriteProperty<Any, V> {
    private var value: V = uninitializedValue

    override operator fun getValue(thisRef: Any, property: KProperty<*>): V {
        if (value === uninitializedValue) {
            value = init()
        }
        return value
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        this.value = value
    }
}
