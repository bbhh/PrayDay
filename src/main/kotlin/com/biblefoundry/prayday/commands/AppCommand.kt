package com.biblefoundry.prayday.commands

import com.biblefoundry.prayday.*
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import org.apache.logging.log4j.kotlin.Logging
import kotlin.system.exitProcess

class AppCommand : CliktCommand(), Logging {
    private val configFile: String by option("-c", "--config", help = "Configuration file").default("config.toml")
    private val globalConfig by findOrSetObject { mutableMapOf<String, Any>() }

    override fun run() {
        // load configuration
        val config = try {
            Config(configFile)
        } catch (e: ConfigException) {
            logger.error(e)
            exitProcess(1)
        }

        // set values in context
        globalConfig["FAMILIES_DATA_FILE"] = config.config[DatabaseSpec.families_data_file]
        globalConfig["FAMILIES_PER_BATCH"] = config.config[MessageSpec.families_per_batch]
        globalConfig["DELAY_BETWEEN_MESSAGES"] = config.config[MessageSpec.delay_between_messages]
        globalConfig["PINPOINT_APP_ID"] = config.config[MessageSpec.pinpoint_app_id]
    }
}
