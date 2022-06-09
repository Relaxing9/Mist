package com.illuzionzstudios.mist

import com.illuzionzstudios.mist.scheduler.MinecraftScheduler
import com.illuzionzstudios.mist.util.TextUtil
import org.bukkit.conversations.Conversable
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset
import java.util.stream.Collectors
import java.util.stream.StreamSupport

class Mist {

    companion object {
        /**
         * The name (with extension) for the main config file
         */
        const val SETTINGS_NAME: String = "config.yml"

        /**
         * Amount of ticks for an invocation to pause before warning
         */
        const val TIME_WARNING_THRESHOLD: Long = 100

        /**
         * Sends the conversable a message later
         */
        @JvmStatic
        fun tellLaterConversing(conversable: Conversable, message: String?, delayTicks: Int) {
            MinecraftScheduler.get()?.synchronize({ tellConversing(conversable, message) }, delayTicks.toLong())
        }

        /**
         * Sends the conversable player a colorized message
         */
        @JvmStatic
        fun tellConversing(conversable: Conversable, message: String?) {
            conversable.sendRawMessage(TextUtil.formatText(message).trim { it <= ' ' })
        }

        /**
         * Convert [Iterable] to [List]
         *
         * @param iterable The iterable to convert
         * @param <T>      Type of object
         * @return As a collection
         */
        @JvmStatic
        fun <T> toList(iterable: Iterable<T>): List<T>? {
            return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList())
        }

        var uid = "%%__USER__%%"

        /**
         * Is this plugin blacklisted by running because of the spigot user who downloaded it`
         */
        fun isBlacklisted(): Boolean {
            try {
                val localURLConnection: URLConnection = URL("https://cdn.illuzionzstudios.com/spigot/blacklist.txt").openConnection()
                localURLConnection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
                )
                localURLConnection.connect()
                val localBufferedReader = BufferedReader(InputStreamReader(localURLConnection.getInputStream(), Charset.forName("UTF-8")))
                val localStringBuilder = StringBuilder()
                var str1: String?
                while (localBufferedReader.readLine().also { str1 = it } != null) {
                    localStringBuilder.append(str1)
                }
                val str2 = localStringBuilder.toString()
                if (str2.contains(java.lang.String.valueOf(uid))) {
                    return true
                }
            } catch (localIOException: IOException) {
                return false
            }

            return false
        }
    }
}