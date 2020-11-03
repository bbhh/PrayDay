package com.biblefoundry.prayday.commands

import com.biblefoundry.prayday.DatabaseController
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import org.apache.logging.log4j.kotlin.Logging

class SetupCommand : CliktCommand(), Logging {
    private val config by requireObject<Map<String, Any>>()

    override fun run() {
        val familiesDataFile = config["FAMILIES_DATA_FILE"].toString()

        // configure database
        DatabaseController.createTables(familiesDataFile)
    }
}
