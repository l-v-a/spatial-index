package lva.spatialindex.viewer.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

interface UIScope : CoroutineScope {
    val job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

}