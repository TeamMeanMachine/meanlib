package org.team2471.frc.lib

/**
 * Signals that the annotated code has not been tested, or is otherwise not known to be stable, and should be
 * treated with caution. It may also imply that the code is subject to change. After sufficient testing this
 * annotation should be removed.
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Experimental(Experimental.Level.WARNING)
annotation class Unproven

