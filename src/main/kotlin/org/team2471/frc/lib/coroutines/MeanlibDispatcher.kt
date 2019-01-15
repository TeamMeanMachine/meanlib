package org.team2471.frc.lib.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

val MeanlibDispatcher: CoroutineDispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()