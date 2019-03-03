package org.team2471.frc.lib.input

import org.team2471.frc.lib.framework.Events

fun (() -> Boolean).whenTrue(body: suspend () -> Unit) = Events.whenActive(this, body)

fun (() -> Boolean).whileTrue(body: suspend () -> Unit) = Events.whileActive(this, body)

fun (() -> Boolean).toggleWhenTrue(body: suspend () -> Unit) = Events.toggleWhenActive(this, body)
