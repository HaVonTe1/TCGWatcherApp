package de.dkutzer.tcgwatcher.settings.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.dkutzer.tcgwatcher.settings.domain.Engines
import de.dkutzer.tcgwatcher.settings.domain.Languages

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val language: Languages,
    val engine: Engines

)