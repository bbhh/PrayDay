package com.biblefoundry.prayday

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.toml

object DatabaseSpec : ConfigSpec() {
    val families_data_file by required<String>()
}

object MessageSpec : ConfigSpec() {
    val pinpoint_app_id by required<String>()
    val families_per_batch by required<Int>()
}

class ConfigException(message: String) : Exception(message)

class Config(configFile: String) {
    val config = Config {
        addSpec(DatabaseSpec)
        addSpec(MessageSpec)
    }
            .from.toml.file(configFile)
            .from.env()
            .from.systemProperties()
}