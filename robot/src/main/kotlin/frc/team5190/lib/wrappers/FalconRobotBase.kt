package frc.team5190.lib.wrappers

import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.hal.HAL
import edu.wpi.first.wpilibj.livewindow.LiveWindow
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import frc.team5190.lib.commands.Subsystem
import frc.team5190.lib.commands.SubsystemHandler
import frc.team5190.lib.wrappers.hid.FalconHID
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import java.util.concurrent.TimeUnit

abstract class FalconRobotBase : RobotBase() {

    companion object {
        lateinit var INSTANCE: FalconRobotBase
            private set
    }

    init {
        @Suppress("LeakingThis")
        INSTANCE = this
    }

    enum class Mode {
        NONE,
        ANY,
        DISABLED,
        AUTONOMOUS,
        TELEOP,
        TEST
    }

    // Listener system

    private val listenerMutex = Mutex()
    private val enterListeners = mutableListOf<Pair<Mode, suspend () -> Unit>>()
    private val leaveListeners = mutableListOf<Pair<Mode, suspend () -> Unit>>()
    private val transitionListeners = mutableListOf<Pair<Pair<Mode, Mode>, suspend () -> Unit>>()

    private val whileListeners = mutableListOf<WhileListener>()

    private class WhileListener(val mode: Mode, val frequency: Long, val listener: suspend () -> Unit) {
        lateinit var job: Job
    }

    protected suspend fun onEnter(enterMode: Mode, listener: suspend () -> Unit) {
        val entry = enterMode to listener
        listenerMutex.withLock { enterListeners.add(entry) }
    }

    protected suspend fun onLeave(leaveMode: Mode, listener: suspend () -> Unit) {
        val entry = leaveMode to listener
        listenerMutex.withLock { leaveListeners.add(entry) }
    }

    protected suspend fun onTransition(fromMode: Mode, toMode: Mode, listener: suspend () -> Unit) {
        val entry = (fromMode to toMode) to listener
        listenerMutex.withLock { transitionListeners.add(entry) }
    }

    protected suspend fun onWhile(mode: Mode, frequency: Long = 50, listener: suspend () -> Unit) {
        val entry = WhileListener(mode, frequency, listener)
        listenerMutex.withLock { whileListeners.add(entry) }
    }

    // Main Robot Code

    var currentMode = Mode.NONE
        private set

    var initialized = false
        private set

    abstract suspend fun initialize()

    override fun startCompetition() = runBlocking {
        LiveWindow.setEnabled(false)
        // Disabled
        onWhile(Mode.DISABLED) { HAL.observeUserProgramDisabled() }
        // Autonomous
        onWhile(Mode.AUTONOMOUS) { HAL.observeUserProgramAutonomous() }
        // TeleOp
        onWhile(Mode.TELEOP) { HAL.observeUserProgramTeleop() }
        // Test
        onEnter(Mode.TEST) { LiveWindow.setEnabled(true) }
        onWhile(Mode.TEST) { HAL.observeUserProgramTest() }
        onLeave(Mode.TEST) { LiveWindow.setEnabled(false) }
        // Update Values
        onWhile(Mode.ANY) {
            SmartDashboard.updateValues()
            LiveWindow.updateValues()
        }

        initialize()
        initialized = true
        // Start up the default commands
        SubsystemHandler.startDefaultCommands()

        handleEnter(Mode.ANY)

        // Tell the DS that the robot is ready to be enabled
        HAL.observeUserProgramStarting()

        while (isActive) {
            // Wait for new data to arrive
            m_ds.waitForData()

            val newMode = when {
                isDisabled -> Mode.DISABLED
                isAutonomous -> Mode.AUTONOMOUS
                isOperatorControl -> Mode.TELEOP
                isTest -> Mode.TEST
                else -> TODO("Robot in invalid mode!")
            }
            if (newMode == currentMode) continue

            handleTransition(currentMode, newMode)

            currentMode = newMode
        }
    }

    private suspend fun handleTransition(fromMode: Mode, toMode: Mode) {
        handleLeave(fromMode)
        listenerMutex.withLock {
            transitionListeners.filter { it.first.first == fromMode && it.first.second == toMode }.forEach { it.second() }
        }
        handleEnter(toMode)
    }

    private suspend fun handleEnter(mode: Mode) = listenerMutex.withLock {
        // Handle events and start while threads
        enterListeners.filter { it.first == mode }.forEach { it.second() }
        // Start while listeners
        val whilesToStart = whileListeners.filter { it.mode == mode || it.mode == Mode.ANY }
        whilesToStart.forEach { listener ->
            listener.job = launch {
                val frequency = listener.frequency
                if (frequency < 0) throw IllegalArgumentException("While frequency cannot be negative!")
                val timeBetweenUpdate = TimeUnit.SECONDS.toNanos(1) / frequency

                // Stores when the next update should happen
                var nextNS = System.nanoTime() + timeBetweenUpdate
                while (isActive) {
                    try {
                        listener.listener()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                    val delayNeeded = nextNS - System.nanoTime()
                    nextNS += timeBetweenUpdate
                    delay(delayNeeded, TimeUnit.NANOSECONDS)
                }
            }
        }
    }

    private suspend fun handleLeave(mode: Mode) = listenerMutex.withLock {
        // Handle events and end while threads
        leaveListeners.filter { it.first == mode }.forEach { it.second() }
        // Stop and join while listeners
        val whilesToStop = whileListeners.filter { it.mode == mode }
        whilesToStop.forEach { it.job.cancel() }
        whilesToStop.forEach { it.job.join() }
    }

    // Helpers

    protected suspend operator fun Subsystem.unaryPlus() = SubsystemHandler.addSubsystem(this)
    protected suspend operator fun FalconHID<*>.unaryPlus() {
        onWhile(Mode.TELEOP) { update() }
    }

}
