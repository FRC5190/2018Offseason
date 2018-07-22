package frc.team5190.lib.commands

import java.util.concurrent.atomic.AtomicLong

object SubsystemHandler {
    private val subsystems = mutableListOf<Subsystem>()

    operator fun Subsystem.unaryPlus() {
        println("[SubsystemHandler] Registered $name")
        subsystems.add(this)
    }
}

abstract class Subsystem(val name: String) {
    companion object {
        private val subsystemId = AtomicLong()
    }

    constructor() : this("Subsystem ${subsystemId.incrementAndGet()}")

    var defaultCommand: Command? = null
        protected set
}