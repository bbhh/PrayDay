package com.biblefoundry.prayday

import com.biblefoundry.prayday.commands.*
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) = AppCommand()
    .subcommands(
        SetupCommand(),
        MessageCommand(),
        ListFamiliesCommand(),
        CreateFamilyCommand(),
        CreateFamiliesCommand(),
    )
    .main(args)