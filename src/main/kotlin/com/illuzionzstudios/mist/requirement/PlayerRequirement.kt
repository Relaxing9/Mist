package com.illuzionzstudios.mist.requirement

import com.illuzionzstudios.mist.data.controller.BukkitPlayerController
import com.illuzionzstudios.mist.data.controller.GamePlayerController
import com.illuzionzstudios.mist.util.PlayerUtil
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.function.Predicate
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A custom player filter that filters things based on the player. Used
 * as a requirement as the player needs to require things
 *
 * @param type The type of requirement
 * @param args Arguments for the requirement
 */
class PlayerRequirement(val type: RequirementType, private val args: List<String?>): Predicate<Player> {

    constructor(type: RequirementType, vararg arguments: String?): this(type, arguments.toList())

    override fun test(player: Player): Boolean {
        // args[0] is the first value, args[1..2..3] etc are other arguments
        val strArg: String? = args[0]
        val intArg: Int = Integer.parseInt(args[0])
        val intArg2: Int = Integer.parseInt(args[1])

        // Do check based on types
        return when (type) {
            RequirementType.PERMISSION -> PlayerUtil.hasPerm(player, strArg)
            RequirementType.REGION -> true // TODO
            RequirementType.EXPERIENCE -> player.exp >= intArg
            RequirementType.NEAR -> {
                val tokens: List<String> = strArg?.split(",") ?: ArrayList()
                val world: World = Bukkit.getWorld(tokens[0]) ?: Bukkit.getWorlds()[0]
                val x = Integer.parseInt(tokens[1])
                val y = Integer.parseInt(tokens[2])
                val z = Integer.parseInt(tokens[3])
                val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
                return BukkitPlayerController.INSTANCE?.getNearbyPlayers(location, intArg2)?.contains(GamePlayerController.INSTANCE?.getPlayer(player)) ?: false
            }
            RequirementType.STRING_EQUALS -> strArg.equals(args[1])
            RequirementType.STRING_EQUALS_IGNORECASE -> strArg.equals(args[1], true)
            RequirementType.STRING_CONTAINS -> strArg?.contains(args[1] ?: "") ?: false
            RequirementType.REGEX -> Pattern.compile(args[1] ?: "").matcher(strArg ?: "").find()
            RequirementType.EQUAL -> intArg == intArg2
            RequirementType.GREATER_THAN_OR_EQUAL -> intArg >= intArg2
            RequirementType.LESS_THAN_OR_EQUAL -> intArg <= intArg2
            RequirementType.NOT_EQUAL -> intArg != intArg2
            RequirementType.GREATER_THAN -> intArg > intArg2
            RequirementType.LESS_THAN -> intArg < intArg2
            // Default to yes
            else -> true
        }
    }

}