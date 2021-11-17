package com.biblefoundry.prayday.commands

import com.biblefoundry.prayday.DatabaseController
import com.github.ajalt.clikt.core.CliktCommand
import org.apache.logging.log4j.kotlin.Logging

class ListFamiliesCommand : CliktCommand(), Logging {
    override fun run() {
        DatabaseController.listFamilies()!!
            .forEach { logger.info(it) }
    }
}