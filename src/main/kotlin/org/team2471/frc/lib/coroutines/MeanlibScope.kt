package org.team2471.frc.lib.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object MeanlibScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default
}
