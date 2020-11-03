package com.biblefoundry.prayday

import com.biblefoundry.prayday.commands.AppCommand
import com.biblefoundry.prayday.commands.MessageCommand
import com.biblefoundry.prayday.commands.SetupCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) = AppCommand()
    .subcommands(
        SetupCommand(),
        MessageCommand(),
    )
    .main(args)