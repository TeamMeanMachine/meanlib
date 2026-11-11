package frc.detekt

import dev.detekt.api.ConsoleReport
import dev.detekt.api.Detektion

class CommandAwaitReport : ConsoleReport {
    override val id: String = "CommandAwaitReport"

    override fun render(detektion: Detektion): String? {
        val commandIssues = detektion.issues.filter {
            it.ruleInstance.id == "IgnoredReturnValue"
        }

        if (commandIssues.isEmpty()) return null

        return commandIssues.joinToString(separator = "\n\n") { issue ->
            """
            |file://${issue.location.path}:${issue.location.source}
            |This command is created but will never run.
            |
            |To run a sequential command, it must be put inside an await(...):
            |
            |    await(yourCommand())
            |    """.trimMargin()
        }
    }
}