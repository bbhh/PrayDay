package com.biblefoundry.prayday.commands

import com.biblefoundry.prayday.DatabaseController
import com.biblefoundry.prayday.Messenger
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import org.apache.logging.log4j.kotlin.Logging

class MessageCommand : CliktCommand(), Logging {
    private val config by requireObject<Map<String, Any>>()

    override fun run() {
        // set up messenger
        val familiesPerBatch = config["FAMILIES_PER_BATCH"] as Int
        val delayBetweenMessages = config["DELAY_BETWEEN_MESSAGES"] as Long
        val pinpointAppId = config["PINPOINT_APP_ID"] as String
        val messenger = Messenger(pinpointAppId, familiesPerBatch)

        // send messages
        val subscribers = DatabaseController.listSubscribers()
        val families = DatabaseController.listFamilies()
        messenger.sendMessages(subscribers, families, delayBetweenMessages)
    }
}
