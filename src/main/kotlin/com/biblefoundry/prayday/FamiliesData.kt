package com.biblefoundry.prayday

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

@Serializable
data class FamiliesData(val families: List<Family>)

@Serializable
data class Family(
    val members: List<Member>,
)

@Serializable
data class Member(
    val first_name: String,
    val last_name: String,
    val gender: String,
    val is_member: Boolean = false,
    val cell_phone: String = "",
)

object FamiliesDataController {
    fun processFamiliesData(path: String): FamiliesData {
        val contents = File(path).readText()
        return Json { ignoreUnknownKeys = true }.decodeFromString<FamiliesData>(contents)
    }
}