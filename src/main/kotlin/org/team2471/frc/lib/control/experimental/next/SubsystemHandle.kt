package org.team2471.frc.lib.control.experimental.next

import kotlinx.coroutines.experimental.launch

class SubsystemHandle(
        internal val name: String,
        private val defaultAction: (suspend () -> Unit)? = null
) {
    init {
        SubsystemScheduler.register(this)
    }

    internal fun launchDefaultAction() {
        val action = defaultAction ?: return

        launch(MeanlibContext) {
            SubsystemScheduler.use(this@SubsystemHandle, cancelConflicts = false) {
                while (true) action()
            }
        }
    }
}
