package com.biblefoundry.prayday.commands

import com.biblefoundry.prayday.DatabaseController
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.apache.logging.log4j.kotlin.Logging

class CreateFamilyCommand : CliktCommand(), Logging {
    private val description by option(
        "-d",
        "--description",
        help = "Description of family, like SMITH, John & Mary"
    ).required()

    override fun run() {
        DatabaseController.createFamily(description)
    }
}