package org.team2471.frc.lib.control.experimental.next

import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlin.coroutines.experimental.CoroutineContext

object MeanlibContext : CoroutineContext by newSingleThreadContext("Meanlib Dispatcher")
