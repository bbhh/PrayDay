package com.biblefoundry.prayday

import org.apache.logging.log4j.kotlin.Logging
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.pinpoint.PinpointClient
import software.amazon.awssdk.services.pinpoint.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Messenger(
    private val pinpointAppId: String,
    private val familiesPerBatch: Int,
) : Logging {
    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE M/d")

    private var client: PinpointClient = PinpointClient.builder()
        .region(Region.US_WEST_2)
        .build()

    fun sendMessages(subscribers: List<Subscriber>?, families: List<String>?, delayBetweenMessages: Long) {
        logger.info("Sending messages...")

        if (subscribers == null) {
            logger.error("No subscribers found")
            return
        }
        if (families == null) {
            logger.error("No families found")
            return
        }

        val today = LocalDate.now()
        val todayFormatted = today.format(dateFormatter)
        subscribers.forEach { subscriber ->
            // generate batch of families
            val batch = families.shuffled()
                .take(familiesPerBatch)
                .sorted()
                .mapIndexed { index, family -> "${index + 1}. $family" }
                .joinToString("\n")

            // construct message and send to subscriber
            logger.info("Sending message to ${subscriber.firstName} ${subscriber.lastName} (${subscriber.phone})...")
            val message = """Hi ${subscriber.firstName}, today is $todayFormatted. Please pray for:
                |$batch""".trimMargin()
            logger.info(message)
            sendMessage(subscriber.phone, message)

            if (delayBetweenMessages > 0) {
                Thread.sleep(delayBetweenMessages)
            }
        }
    }

    private fun sendMessage(destinationNumber: String, message: String) {
        try {
            val addressMap = mutableMapOf<String, AddressConfiguration>()
            val addressConfig = AddressConfiguration.builder()
                .channelType(ChannelType.SMS)
                .build()
            addressMap[destinationNumber] = addressConfig
            val smsMessage = SMSMessage.builder()
                .body(message)
                .messageType("PROMOTIONAL")
                .build()

            val directMessageConfig = DirectMessageConfiguration.builder()
                .smsMessage(smsMessage)
                .build()
            val messageRequest = MessageRequest.builder()
                .addresses(addressMap)
                .messageConfiguration(directMessageConfig)
                .build()

            val request = SendMessagesRequest.builder()
                .applicationId(pinpointAppId)
                .messageRequest(messageRequest)
                .build()
            client.sendMessages(request)
        } catch (e: PinpointException) {
            logger.error(e.message.toString())
        }
    }
}