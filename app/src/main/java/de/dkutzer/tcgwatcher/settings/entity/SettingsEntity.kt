package de.dkutzer.tcgwatcher.settings.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val language: Languages,
    val engine: Engines

)