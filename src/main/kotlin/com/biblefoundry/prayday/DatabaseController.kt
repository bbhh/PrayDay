package com.biblefoundry.prayday

import org.apache.logging.log4j.kotlin.Logging
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import java.util.*

object DatabaseController : Logging {
    private const val MEMBERS_TABLE_NAME = "PrayDay.Members"
    private const val FAMILIES_TABLE_NAME = "PrayDay.Families"

    private const val DEFAULT_WAIT_TIMEOUT = 5 * 60 * 1000 // 5 minutes
    private const val DEFAULT_WAIT_INTERVAL = 1000L // 1 second

    private val PHONE_PATTERN = "\\((\\d{3})\\) (\\d{3})-(\\d{4})".toRegex()

    var client: DynamoDbClient = DynamoDbClient.builder()
            .region(Region.US_WEST_2)
            .build()

    fun createTables(familiesDataFile: String) {
        // load families data
        val familiesData = FamiliesDataController.processFamiliesData(familiesDataFile)

        // create tables
        createMembersTable(familiesData)
        createFamiliesTable(familiesData)
    }

    private fun createMembersTable(familiesData: FamiliesData) {
        deleteTable(MEMBERS_TABLE_NAME)
        createTable(MEMBERS_TABLE_NAME)

        // populate with all members and their phone numbers
        try {
            familiesData.families.forEach outer@{ family ->
                family.members.filter { person -> person.is_member && person.cell_phone.isNotEmpty() }
                    .forEach { member ->
                        val name = "${member.first_name} ${member.last_name}"

                        // validate cell phone
                        val match = PHONE_PATTERN.matchEntire(member.cell_phone)
                        if (match == null) {
                            logger.warn("-> Skipping $name due to invalid cell phone: ${member.cell_phone}")
                            return@forEach
                        }
                        // convert to E.164 format
                        val cellPhone = "+1${match.groupValues[1]}${match.groupValues[2]}${match.groupValues[3]}"

                        logger.info("-> Adding $name to members table...")

                        val itemValues = mapOf<String, AttributeValue>(
                            "id" to AttributeValue.builder().s(cellPhone).build(),
                            "firstName" to AttributeValue.builder().s(member.first_name).build(),
                            "lastName" to AttributeValue.builder().s(member.last_name).build(),
                            "subscribed" to AttributeValue.builder().bool(false).build(),
                        )
                        val request = PutItemRequest.builder()
                            .tableName(MEMBERS_TABLE_NAME)
                            .item(itemValues)
                            .build()
                        client.putItem(request)
                    }
            }
        }
        catch (e: Exception) {
            logger.error(e.message.toString())
        }
    }

    private fun createFamiliesTable(familiesData: FamiliesData) {
        deleteTable(FAMILIES_TABLE_NAME)
        createTable(FAMILIES_TABLE_NAME)

        // populate with all families and their members
        familiesData.families.forEach { family ->
            val lastName = family.members[0].last_name.toUpperCase()
            val persons = family.members.partition { it.is_member }
            val primary = when (persons.first.size) {
                2 -> {
                    persons.first.sortedByDescending { it.gender } // M, then F
                    "${persons.first[0].first_name} & ${persons.first[1].first_name}"
                }
                1 -> {
                    persons.first[0].first_name
                }
                else -> {
                    logger.error("neither 1 nor 2 members in this family; skipping")
                    return@forEach
                }
            }
            val secondaryContents = persons.second.map { it.first_name }
                .joinToString(", ")
            val secondary = if (secondaryContents.isEmpty()) "" else " ($secondaryContents)"

            val description = "$lastName, $primary$secondary"

            logger.info("-> Adding $description to families table...")

            val itemValues = mapOf<String, AttributeValue>(
                "id" to AttributeValue.builder().s(UUID.randomUUID().toString()).build(),
                "description" to AttributeValue.builder().s(description).build(),
            )
            val request = PutItemRequest.builder()
                .tableName(FAMILIES_TABLE_NAME)
                .item(itemValues)
                .build()
            client.putItem(request)
        }
    }

    private fun createTable(tableName: String) {
        logger.info("Creating table $tableName...")

        val request = CreateTableRequest.builder()
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .provisionedThroughput(
                    ProvisionedThroughput.builder()
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build()
                )
                .tableName(tableName)
                .build()

        client.createTable(request)

        waitUntilTableActive(tableName)
    }

    private fun deleteTable(tableName: String) {
        logger.info("Deleting table $tableName...")

        val request = DeleteTableRequest.builder()
            .tableName(tableName)
            .build()

        try {
            client.deleteTable(request)
        } catch (e: ResourceNotFoundException) {
            logger.info("Table $tableName does not exist, so it does not need to be deleted")
            return
        }

        waitUntilTableDeleted(tableName)
    }

    private fun waitUntilTableActive(tableName: String): Boolean {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + DEFAULT_WAIT_TIMEOUT
        val request = DescribeTableRequest.builder()
            .tableName(tableName)
            .build()

        while (System.currentTimeMillis() < endTime) {
            try {
                val table = client.describeTable(request).table()
                if (table.tableStatus() == TableStatus.ACTIVE) {
                    return true
                }

                Thread.sleep(DEFAULT_WAIT_INTERVAL)
            } catch (e: ResourceNotFoundException) {
                // table does not exist yet, so keep polling
            }
        }
        return false
    }

    private fun waitUntilTableDeleted(tableName: String): Boolean {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + DEFAULT_WAIT_TIMEOUT
        val request = DescribeTableRequest.builder()
            .tableName(tableName)
            .build()

        while (System.currentTimeMillis() < endTime) {
            try {
                client.describeTable(request)

                Thread.sleep(DEFAULT_WAIT_INTERVAL)
            } catch (e: ResourceNotFoundException) {
                return true
            }
        }
        return false
    }

    fun listSubscribers(): List<Subscriber>? {
        val request = ScanRequest.builder()
            .tableName(MEMBERS_TABLE_NAME)
            .filterExpression("subscribed = :s")
            .expressionAttributeValues(mapOf(":s" to AttributeValue.builder().bool(true).build()))
            .build()

        val response = client.scan(request)
        return response.items()?.map {
            Subscriber(
                it["firstName"]!!.s(),
                it["lastName"]!!.s(),
                it["id"]!!.s(),
            )
        }
    }

    fun subscribe(phone: String) {
        setSubscribed(phone, true)
    }

    fun unsubscribe(phone: String) {
        setSubscribed(phone, false)
    }

    private fun setSubscribed(phone: String, subscribed: Boolean) {
        val key = mapOf<String, AttributeValue>(
            "id" to AttributeValue.builder().s(phone).build(),
        )
        val attributeUpdates = mapOf<String, AttributeValueUpdate>(
            "subscribed" to AttributeValueUpdate.builder()
                .value(AttributeValue.builder().bool(subscribed).build())
                .action(AttributeAction.PUT)
                .build(),
        )
        val request = UpdateItemRequest.builder()
            .tableName(MEMBERS_TABLE_NAME)
            .key(key)
            .attributeUpdates(attributeUpdates)
            .build()

        client.updateItem(request)
    }

    fun listFamilies(): List<String>? {
        val request = ScanRequest.builder()
            .tableName(FAMILIES_TABLE_NAME)
            .build()

        val response = client.scan(request)
        return response.items()?.map { it["description"]!!.s() }
    }
}

data class Subscriber(val firstName: String, val lastName: String, val phone: String)