package org.team2471.frc.lib.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

@Deprecated("Use the MeanlibDispatcher instead", ReplaceWith("MeanlibDispatcher"), DeprecationLevel.WARNING)
object MeanlibScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = MeanlibDispatcher
}
