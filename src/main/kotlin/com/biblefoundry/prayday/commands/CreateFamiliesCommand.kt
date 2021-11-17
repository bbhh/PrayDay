package com.biblefoundry.prayday.commands

import com.biblefoundry.prayday.DatabaseController
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.apache.logging.log4j.kotlin.Logging
import java.io.File

class CreateFamiliesCommand : CliktCommand(), Logging {
    private val familiesFile by option(
        "-f",
        "--families-file",
        help = "File containing a list of families, one family description per line"
    ).required()

    override fun run() {
        File(familiesFile).forEachLine { description ->
            DatabaseController.createFamily(description)
        }
    }
}