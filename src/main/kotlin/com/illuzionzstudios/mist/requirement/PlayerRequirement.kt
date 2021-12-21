package com.illuzionzstudios.mist.requirement

import com.illuzionzstudios.mist.Logger
import com.illuzionzstudios.mist.data.controller.BukkitPlayerController
import com.illuzionzstudios.mist.data.controller.GamePlayerController
import com.illuzionzstudios.mist.plugin.Hooks
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
class PlayerRequirement(val type: RequirementType, private val args: List<Any?>): Predicate<Player> {

    constructor(type: RequirementType, vararg arguments: Any?): this(type, arguments.toList())

    override fun test(player: Player): Boolean {
        // args[0] is the first value, args[1..2..3] etc are other arguments
        val strArg: String = args[0].toString()

        // Do check based on types
        return when (type) {
            RequirementType.PERMISSION -> PlayerUtil.hasPerm(player, strArg)
            RequirementType.REGION -> Hooks.worldguard?.getRegionsAt(player.location)?.contains(strArg) ?: true
            RequirementType.EXPERIENCE -> player.exp >= args[0] as Int
            RequirementType.NEAR -> {
                val tokens: List<String> = strArg.split(",")
                val world: World = Bukkit.getWorld(tokens[0]) ?: Bukkit.getWorlds()[0]
                val x = Integer.parseInt(tokens[1])
                val y = Integer.parseInt(tokens[2])
                val z = Integer.parseInt(tokens[3])
                val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
                val distance = (args[1] as Int).toDouble()
                return player.location.distanceSquared(location) <= (distance * distance)
            }
            RequirementType.WORLD -> player.location.world?.name.equals(strArg, true)
            RequirementType.STRING_EQUALS -> strArg == args[1].toString()
            RequirementType.STRING_EQUALS_IGNORECASE -> strArg.equals(args[1].toString(), true)
            RequirementType.STRING_CONTAINS -> strArg.contains(args[1].toString())
            RequirementType.REGEX -> Pattern.compile(args[1].toString()).matcher(strArg).find()
            RequirementType.EQUAL -> args[0] as Int == args[1] as Int
            RequirementType.GREATER_THAN_OR_EQUAL -> args[0] as Int >= args[1] as Int
            RequirementType.LESS_THAN_OR_EQUAL -> args[0] as Int <= args[1] as Int
            RequirementType.NOT_EQUAL -> args[0] as Int != args[1] as Int
            RequirementType.GREATER_THAN -> args[0] as Int > args[1] as Int
            RequirementType.LESS_THAN -> (args[0] as Int) < (args[1] as Int)
            // Default to yes
            else -> true
        }
    }

}