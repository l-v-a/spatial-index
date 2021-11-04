package lva.spatialindex.utils

import kotlin.reflect.KProperty

/**
 * @author vlitvinenko
 */

fun <T: Any> resettableWith(uninitializedValue: T, init: () -> T) = Resettable(uninitializedValue, init)

class Resettable<T: Any>(private val uninitializedValue: T, private val init: () -> T) {
    private var value: T = uninitializedValue

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value === uninitializedValue) {
            value = init()
        }
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        value = newValue
    }
}
