package org.team2471.frc.lib.control.experimental.next

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class SubsystemLock(
        internal val name: String,
        private val defaultAction: (suspend () -> Unit)? = null,
        private val defaultActionDelay: Int = 20
) {
    init {
        ActionScheduler.register(this)
    }

    internal fun launchDefaultAction() {
        val action = defaultAction ?: return

        launch(MeanlibContext) {
            ActionScheduler.use(this@SubsystemLock, cancelConflicts = false) {
                while (true) {
                    action()
                    delay(defaultActionDelay)
                }
            }
        }
    }
}
