package org.team2471.frc.lib.coroutines
import edu.wpi.first.wpilibj2.command.Command
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture

class CoroutineCommand(private val action: suspend () -> Unit): Command() {
    private var future: CompletableFuture<Unit>? = null

    override fun initialize() {
        future = CompletableFuture()
        CoroutineScope(MeanlibDispatcher).launch {
            try {
                action()
                future?.complete(Unit)
            } catch (e: Exception) {
                future?.completeExceptionally(e)
            }
        }
    }

    override fun isFinished(): Boolean {
        return future?.isDone ?: true
    }

    override fun end(interrupted: Boolean) {
        future?.cancel(true)
        future = null
    }

    override fun runsWhenDisabled(): Boolean = true
}