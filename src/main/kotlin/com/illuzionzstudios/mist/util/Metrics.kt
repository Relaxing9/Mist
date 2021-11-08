package com.illuzionzstudios.mist.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.Callable
import java.util.logging.Level
import java.util.zip.GZIPOutputStream
import javax.net.ssl.HttpsURLConnection

/**
 * bStats collects some data for plugin authors.
 *
 *
 * Check out https://bStats.org/ to learn more about bStats!
 */
class Metrics(plugin: Plugin?, pluginId: Int) {
    companion object {
        // The version of this bStats class
        const val B_STATS_VERSION = 1

        // The url to which the data is sent
        private const val URL = "https://bStats.org/submitData/bukkit"

        // Should failed requests be logged?
        private var logFailedRequests: Boolean

        // Should the sent data be logged?
        private var logSentData: Boolean

        // Should the response text be logged?
        private var logResponseStatusText: Boolean

        // The uuid of the server
        private var serverUUID: String

        /**
         * Sends the data to the bStats server.
         *
         * @param plugin Any plugin. It's just used to get a logger instance.
         * @param data   The data to send.
         * @throws Exception If the request failed.
         */
        @Throws(Exception::class)
        private fun sendData(plugin: Plugin, data: JsonObject?) {
            requireNotNull(data) { "Data cannot be null!" }
            if (Bukkit.isPrimaryThread()) {
                throw IllegalAccessException("This method must not be called from the main thread!")
            }
            if (logSentData) {
                plugin.logger.info("Sending data to bStats: $data")
            }
            val connection = URL(URL).openConnection() as HttpsURLConnection

            // Compress the data to save bandwidth
            val compressedData = compress(data.toString())

            // Add headers
            connection.requestMethod = "POST"
            connection.addRequestProperty("Accept", "application/json")
            connection.addRequestProperty("Connection", "close")
            connection.addRequestProperty("Content-Encoding", "gzip") // We gzip our request
            connection.addRequestProperty("Content-Length", compressedData!!.size.toString())
            connection.setRequestProperty("Content-Type", "application/json") // We send our data in JSON format
            connection.setRequestProperty("User-Agent", "MC-Server/" + B_STATS_VERSION)

            // Send data
            connection.doOutput = true
            DataOutputStream(connection.outputStream).use { outputStream -> outputStream.write(compressedData) }
            val builder = StringBuilder()
            BufferedReader(InputStreamReader(connection.inputStream)).use { bufferedReader ->
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
            }
            if (logResponseStatusText) {
                plugin.logger.info("Sent data to bStats and received response: $builder")
            }
        }

        /**
         * Gzips the given String.
         *
         * @param str The string to gzip.
         * @return The gzipped String.
         * @throws IOException If the compression failed.
         */
        @Throws(IOException::class)
        private fun compress(str: String?): ByteArray? {
            if (str == null) {
                return null
            }
            val outputStream = ByteArrayOutputStream()
            GZIPOutputStream(outputStream).use { gzip -> gzip.write(str.toByteArray(StandardCharsets.UTF_8)) }
            return outputStream.toByteArray()
        }

        init {
            // You can use the property to disable the check in your test environment
            if (System.getProperty("bstats.relocatecheck") == null || System.getProperty("bstats.relocatecheck") != "false") {
                // Maven's Relocate is clever and changes strings, too. So we have to use this little "trick" ... :D
                val defaultPackage = String(
                    byteArrayOf(
                        'o'.code.toByte(),
                        'r'.code.toByte(),
                        'g'.code.toByte(),
                        '.'.code.toByte(),
                        'b'.code.toByte(),
                        's'.code.toByte(),
                        't'.code.toByte(),
                        'a'.code.toByte(),
                        't'.code.toByte(),
                        's'.code.toByte(),
                        '.'.code.toByte(),
                        'b'.code.toByte(),
                        'u'.code.toByte(),
                        'k'.code.toByte(),
                        'k'.code.toByte(),
                        'i'.code.toByte(),
                        't'.code.toByte()
                    )
                )
                val examplePackage = String(
                    byteArrayOf(
                        'y'.code.toByte(),
                        'o'.code.toByte(),
                        'u'.code.toByte(),
                        'r'.code.toByte(),
                        '.'.code.toByte(),
                        'p'.code.toByte(),
                        'a'.code.toByte(),
                        'c'.code.toByte(),
                        'k'.code.toByte(),
                        'a'.code.toByte(),
                        'g'.code.toByte(),
                        'e'.code.toByte()
                    )
                )
                // We want to make sure nobody just copy & pastes the example and use the wrong package names
                check(!(Metrics::class.java.getPackage().name == com.illuzionzstudios.mist.util.defaultPackage || Metrics::class.java.getPackage().name == com.illuzionzstudios.mist.util.examplePackage)) { "bStats Metrics class has not been relocated correctly!" }
            }
        }
    }

    // The plugin
    private val plugin: Plugin

    // The plugin id
    private val pluginId: Int

    // A list with all custom charts
    private val charts: MutableList<CustomChart> = ArrayList()

    /**
     * Checks if bStats is enabled.
     *
     * @return Whether bStats is enabled or not.
     */
    // Is bStats enabled on this server?
    val isEnabled: Boolean

    /**
     * Adds a custom chart.
     *
     * @param chart The chart to add.
     */
    fun addCustomChart(chart: CustomChart?) {
        requireNotNull(chart) { "Chart cannot be null!" }
        charts.add(chart)
    }

    /**
     * Starts the Scheduler which submits our data every 30 minutes.
     */
    private fun startSubmitting() {
        val timer = Timer(true) // We use a timer cause the Bukkit scheduler is affected by server lags
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (!plugin.isEnabled) { // Plugin was disabled
                    timer.cancel()
                    return
                }
                // Nevertheless we want our code to run in the Bukkit main thread, so we have to use the Bukkit scheduler
                // Don't be afraid! The connection to the bStats server is still async, only the stats collection is sync ;)
                Bukkit.getScheduler().runTask(plugin, Runnable { submitData() })
            }
        }, (1000 * 60 * 5).toLong(), (1000 * 60 * 30).toLong())
        // Submit the data every 30 minutes, first time after 5 minutes to give other plugins enough time to start
        // WARNING: Changing the frequency has no effect but your plugin WILL be blocked/deleted!
        // WARNING: Just don't do it!
    }// If the chart is null, we skip it// Add the data of the custom charts// Append the name of the plugin
    // Append the id of the plugin
    // Append the version of the plugin
    /**
     * Gets the plugin specific data.
     * This method is called using Reflection.
     *
     * @return The plugin specific data.
     */
    val pluginData: JsonObject
        get() {
            val data = JsonObject()
            val pluginName = plugin.description.name
            val pluginVersion = plugin.description.version
            data.addProperty("pluginName", pluginName) // Append the name of the plugin
            data.addProperty("id", pluginId) // Append the id of the plugin
            data.addProperty("pluginVersion", pluginVersion) // Append the version of the plugin
            val customCharts = JsonArray()
            for (customChart in charts) {
                // Add the data of the custom charts
                val chart: JsonObject = customChart.getRequestJsonObject()
                    ?: // If the chart is null, we skip it
                    continue
                customCharts.add(chart)
            }
            data.add("customCharts", customCharts)
            return data
        }// Just use the new method if the Reflection failed

    // OS/Java specific data
// Around MC 1.8 the return type was changed to a collection from an array,
    // This fixes java.lang.NoSuchMethodError: org.bukkit.Bukkit.getOnlinePlayers()Ljava/util/Collection;
// Minecraft specific data
    /**
     * Gets the server specific data.
     *
     * @return The server specific data.
     */
    private val serverData: JsonObject
        private get() {
            // Minecraft specific data
            val playerAmount: Int
            playerAmount = try {
                // Around MC 1.8 the return type was changed to a collection from an array,
                // This fixes java.lang.NoSuchMethodError: org.bukkit.Bukkit.getOnlinePlayers()Ljava/util/Collection;
                val onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers")
                if (onlinePlayersMethod.returnType == MutableCollection::class.java) (onlinePlayersMethod.invoke(Bukkit.getServer()) as Collection<*>).size else (onlinePlayersMethod.invoke(
                    Bukkit.getServer()
                ) as Array<Player?>).size
            } catch (e: Exception) {
                Bukkit.getOnlinePlayers().size // Just use the new method if the Reflection failed
            }
            val onlineMode = if (Bukkit.getOnlineMode()) 1 else 0
            val bukkitVersion = Bukkit.getVersion()
            val bukkitName = Bukkit.getName()

            // OS/Java specific data
            val javaVersion = System.getProperty("java.version")
            val osName = System.getProperty("os.name")
            val osArch = System.getProperty("os.arch")
            val osVersion = System.getProperty("os.version")
            val coreCount = Runtime.getRuntime().availableProcessors()
            val data = JsonObject()
            data.addProperty("serverUUID", serverUUID)
            data.addProperty("playerAmount", playerAmount)
            data.addProperty("onlineMode", onlineMode)
            data.addProperty("bukkitVersion", bukkitVersion)
            data.addProperty("bukkitName", bukkitName)
            data.addProperty("javaVersion", javaVersion)
            data.addProperty("osName", osName)
            data.addProperty("osArch", osArch)
            data.addProperty("osVersion", osVersion)
            data.addProperty("coreCount", coreCount)
            return data
        }

    /**
     * Collects the data and sends it afterwards.
     */
    private fun submitData() {
        val data = serverData
        val pluginData = JsonArray()
        // Search for all other bStats Metrics classes to get their plugin data
        for (service in Bukkit.getServicesManager().knownServices) {
            try {
                service.getField("B_STATS_VERSION") // Our identifier :)
                for (provider in Bukkit.getServicesManager().getRegistrations(service)) {
                    try {
                        val plugin = provider.service.getMethod("getPluginData").invoke(provider.provider)
                        if (plugin is JsonObject) {
                            pluginData.add(plugin)
                        } else { // old bstats version compatibility
                            try {
                                val jsonObjectJsonSimple = Class.forName("org.json.simple.JSONObject")
                                if (plugin.javaClass.isAssignableFrom(jsonObjectJsonSimple)) {
                                    val jsonStringGetter = jsonObjectJsonSimple.getDeclaredMethod("toJSONString")
                                    jsonStringGetter.isAccessible = true
                                    val jsonString = jsonStringGetter.invoke(plugin) as String
                                    val `object` = JsonParser().parse(jsonString).asJsonObject
                                    pluginData.add(`object`)
                                }
                            } catch (e: ClassNotFoundException) {
                                // minecraft version 1.14+
                                if (logFailedRequests) {
                                    this.plugin.logger.log(Level.SEVERE, "Encountered unexpected exception", e)
                                }
                            }
                        }
                    } catch (ignored: NullPointerException) {
                    } catch (ignored: NoSuchMethodException) {
                    } catch (ignored: IllegalAccessException) {
                    } catch (ignored: InvocationTargetException) {
                    }
                }
            } catch (ignored: NoSuchFieldException) {
            }
        }
        data.add("plugins", pluginData)

        // Create a new thread for the connection to the bStats server
        Thread {
            try {
                // Send the data
                sendData(plugin, data)
            } catch (e: Exception) {
                // Something went wrong! :(
                if (logFailedRequests) {
                    plugin.logger.log(Level.WARNING, "Could not submit plugin stats of " + plugin.name, e)
                }
            }
        }.start()
    }

    /**
     * Represents a custom chart.
     */
    abstract class CustomChart internal constructor(chartId: String?) {
        // The id of the chart
        val chartId: String

        // If the data is null we don't send the chart.
        private val requestJsonObject: JsonObject?
            private get() {
                val chart = JsonObject()
                chart.addProperty("chartId", chartId)
                try {
                    val data = chartData
                        ?: // If the data is null we don't send the chart.
                        return null
                    chart.add("data", data)
                } catch (t: Throwable) {
                    if (logFailedRequests) {
                        Bukkit.getLogger().log(Level.WARNING, "Failed to get data for custom chart with id $chartId", t)
                    }
                    return null
                }
                return chart
            }

        @get:Throws(Exception::class)
        protected abstract val chartData: JsonObject?

        /**
         * Class constructor.
         *
         * @param chartId The id of the chart.
         */
        init {
            require(!(chartId == null || chartId.isEmpty())) { "ChartId cannot be null or empty!" }
            this.chartId = chartId
        }
    }

    /**
     * Represents a custom simple pie.
     */
    class SimplePie
    /**
     * Class constructor.
     *
     * @param chartId  The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(chartId: String?, private val callable: Callable<String>) : CustomChart(chartId) {
        // Null = skip the chart
        @get:Throws(Exception::class)
        override val chartData: JsonObject?
            protected get() {
                val data = JsonObject()
                val value = callable.call()
                if (value == null || value.isEmpty()) {
                    // Null = skip the chart
                    return null
                }
                data.addProperty("value", value)
                return data
            }
    }

    /**
     * Represents a custom advanced pie.
     */
    class AdvancedPie
    /**
     * Class constructor.
     *
     * @param chartId  The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(
        chartId: String?, // Null = skip the chart// Skip this invalid
        private val callable: Callable<Map<String, Int>>
    ) : CustomChart(chartId) {
        // Null = skip the chart
        @get:Throws(Exception::class)
        override val chartData: JsonObject?
            protected get() {
                val data = JsonObject()
                val values = JsonObject()
                val map = callable.call()
                if (map == null || map.isEmpty()) {
                    // Null = skip the chart
                    return null
                }
                var allSkipped = true
                for ((key, value) in map) {
                    if (value == 0) {
                        continue  // Skip this invalid
                    }
                    allSkipped = false
                    values.addProperty(key, value)
                }
                if (allSkipped) {
                    // Null = skip the chart
                    return null
                }
                data.add("values", values)
                return data
            }
    }

    /**
     * Represents a custom drilldown pie.
     */
    class DrilldownPie
    /**
     * Class constructor.
     *
     * @param chartId  The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(
        chartId: String?, // Null = skip the chart
        private val callable: Callable<Map<String, Map<String, Int>>>
    ) : CustomChart(chartId) {
        // Null = skip the chart
        @get:Throws(Exception::class)
        override val chartData: JsonObject?
            get() {
                val data = JsonObject()
                val values = JsonObject()
                val map = callable.call()
                if (map == null || map.isEmpty()) {
                    // Null = skip the chart
                    return null
                }
                var reallyAllSkipped = true
                for ((key) in map) {
                    val value = JsonObject()
                    var allSkipped = true
                    for ((key1, value1) in map[key]!!) {
                        value.addProperty(key1, value1)
                        allSkipped = false
                    }
                    if (!allSkipped) {
                        reallyAllSkipped = false
                        values.add(key, value)
                    }
                }
                if (reallyAllSkipped) {
                    // Null = skip the chart
                    return null
                }
                data.add("values", values)
                return data
            }
    }

    /**
     * Represents a custom single line chart.
     */
    class SingleLineChart
    /**
     * Class constructor.
     *
     * @param chartId  The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(chartId: String?, private val callable: Callable<Int>) : CustomChart(chartId) {
        // Null = skip the chart
        @get:Throws(Exception::class)
        override val chartData: JsonObject?
            protected get() {
                val data = JsonObject()
                val value = callable.call()
                if (value == 0) {
                    // Null = skip the chart
                    return null
                }
                data.addProperty("value", value)
                return data
            }
    }

    /**
     * Represents a custom multi line chart.
     */
    class MultiLineChart
    /**
     * Class constructor.
     *
     * @param chartId  The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(
        chartId: String?, // Null = skip the chart// Skip this invalid
        private val callable: Callable<Map<String, Int>>
    ) : CustomChart(chartId) {
        // Null = skip the chart
        @get:Throws(Exception::class)
        override val chartData: JsonObject?
            protected get() {
                val data = JsonObject()
                val values = JsonObject()
                val map = callable.call()
                if (map == null || map.isEmpty()) {
                    // Null = skip the chart
                    return null
                }
                var allSkipped = true
                for ((key, value) in map) {
                    if (value == 0) {
                        continue  // Skip this invalid
                    }
                    allSkipped = false
                    values.addProperty(key, value)
                }
                if (allSkipped) {
                    // Null = skip the chart
                    return null
                }
                data.add("values", values)
                return data
            }
    }

    /**
     * Represents a custom simple bar chart.
     */
    class SimpleBarChart
    /**
     * Class constructor.
     *
     * @param chartId  The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(chartId: String?, private val callable: Callable<Map<String, Int>>) : CustomChart(chartId) {
        // Null = skip the chart
        @get:Throws(Exception::class)
        override val chartData: JsonObject?
            protected get() {
                val data = JsonObject()
                val values = JsonObject()
                val map = callable.call()
                if (map == null || map.isEmpty()) {
                    // Null = skip the chart
                    return null
                }
                for ((key, value) in map) {
                    val categoryValues = JsonArray()
                    categoryValues.add(JsonPrimitive(value))
                    values.add(key, categoryValues)
                }
                data.add("values", values)
                return data
            }
    }

    /**
     * Represents a custom advanced bar chart.
     */
    class AdvancedBarChart
    /**
     * Class constructor.
     *
     * @param chartId  The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(
        chartId: String?, // Null = skip the chart// Skip this invalid
        private val callable: Callable<Map<String, IntArray>>
    ) : CustomChart(chartId) {
        // Null = skip the chart
        @get:Throws(Exception::class)
        override val chartData: JsonObject?
            protected get() {
                val data = JsonObject()
                val values = JsonObject()
                val map = callable.call()
                if (map == null || map.isEmpty()) {
                    // Null = skip the chart
                    return null
                }
                var allSkipped = true
                for ((key, value) in map) {
                    if (value.length == 0) {
                        continue  // Skip this invalid
                    }
                    allSkipped = false
                    val categoryValues = JsonArray()
                    for (categoryValue in value) {
                        categoryValues.add(JsonPrimitive(categoryValue))
                    }
                    values.add(key, categoryValues)
                }
                if (allSkipped) {
                    // Null = skip the chart
                    return null
                }
                data.add("values", values)
                return data
            }
    }

    /**
     * Class constructor.
     *
     * @param plugin   The plugin which stats should be submitted.
     * @param pluginId The id of the plugin.
     * It can be found at [What is my plugin id?](https://bstats.org/what-is-my-plugin-id)
     */
    init {
        requireNotNull(plugin) { "Plugin cannot be null!" }
        this.plugin = plugin
        this.pluginId = pluginId

        // Get the config file
        val bStatsFolder = File(plugin.dataFolder.parentFile, "bStats")
        val configFile = File(bStatsFolder, "config.yml")
        val config = YamlConfiguration.loadConfiguration(configFile)

        // Check if the config file exists
        if (!config.isSet("serverUuid")) {

            // Add default values
            config.addDefault("enabled", true)
            // Every server gets it's unique random id.
            config.addDefault("serverUuid", UUID.randomUUID().toString())
            // Should failed request be logged?
            config.addDefault("logFailedRequests", false)
            // Should the sent data be logged?
            config.addDefault("logSentData", false)
            // Should the response text be logged?
            config.addDefault("logResponseStatusText", false)

            // Inform the server owners about bStats
            config.options().header(
                """
                    bStats collects some data for plugin authors like how many servers are using their plugins.
                    To honor their work, you should not disable it.
                    This has nearly no effect on the server performance!
                    Check out https://bStats.org/ to learn more :)
                    """.trimIndent()
            ).copyDefaults(true)
            try {
                config.save(configFile)
            } catch (ignored: IOException) {
            }
        }

        // Load the data
        isEnabled = config.getBoolean("enabled", true)
        serverUUID = config.getString("serverUuid")!!
        logFailedRequests = config.getBoolean("logFailedRequests", false)
        logSentData = config.getBoolean("logSentData", false)
        logResponseStatusText = config.getBoolean("logResponseStatusText", false)
        if (isEnabled) {
            var found = false
            // Search for all other bStats Metrics classes to see if we are the first one
            for (service in Bukkit.getServicesManager().knownServices) {
                try {
                    service.getField("B_STATS_VERSION") // Our identifier :)
                    found = true // We aren't the first
                    break
                } catch (ignored: NoSuchFieldException) {
                }
            }
            // Register our service
            Bukkit.getServicesManager().register(Metrics::class.java, this, plugin, ServicePriority.Normal)
            if (!found) {
                // We are the first!
                startSubmitting()
            }
        }
    }
}