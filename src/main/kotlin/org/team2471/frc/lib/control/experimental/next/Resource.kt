package org.team2471.frc.lib.control.experimental.next

import kotlinx.coroutines.experimental.launch

class Resource(
        internal val name: String,
        private val defaultAction: (suspend () -> Unit)? = null
) {
    init {
        EventHandler.register(this)
    }

    internal fun launchDefaultAction() {
        val action = defaultAction ?: return

        launch(MeanlibContext) {
            EventHandler.useResources(arrayOf(this@Resource), cancelConflicts = false) {
                action()
            }
        }
    }
}
