package de.dkutzer.tcgwatcher.products.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val language: String,
    val enableJs: Boolean
)