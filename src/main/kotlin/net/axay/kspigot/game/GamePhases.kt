@file:Suppress("MemberVisibilityCanBePrivate")

package net.axay.kspigot.game

import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.main.KSpigotMainInstance
import net.md_5.bungee.api.ChatColor

class GamePhaseSystem(vararg gamePhases: GamePhase) {
    val gamePhases = gamePhases.toMutableList()
    fun begin() = gamePhases.removeAt(0).startIt(gamePhases)
}

fun counterMessage(
    beginning: String,
    seconds: String,
    second: String,
    prefix: String? = null,
    suffix: String? = null
): (Long) -> String {

    val realPrefix = prefix?.plus(" ") ?: ""
    val realSuffix = suffix ?: ""

    return {
        val realSeconds = if (it <= 1L) second else seconds
        "$realPrefix${ChatColor.RESET}$beginning $it $realSeconds$realSuffix"
    }

}

class GamePhase(
        val length: Long,
        val start: (() -> Unit)?,
        val end: (() -> Unit)?,
        val counterMessage: ((secondsLeft: Long) -> String)?
) {
    fun startIt(phaseQueue: MutableList<GamePhase>) {
        start?.invoke()
        KSpigotMainInstance.task(
                period = 20,
                howOften = (length / 20) + 1,
                endCallback = {

                    end?.invoke()

                    if (phaseQueue.isNotEmpty())
                        phaseQueue.removeAt(0).startIt(phaseQueue)

                }
        ) {

            if (counterMessage != null) {
                val currentCounter = it.counterDownToZero
                if (currentCounter?.isCounterValue == true)
                    broadcast(counterMessage.invoke(currentCounter))
            }

        }
    }
}

private val Long.isCounterValue: Boolean get() = when (this) {
    1L, 2L, 3L, 4L, 5L, 10L, 15L, 20L, 30L -> true
    0L -> false
    else -> this % 60 == 0L
}