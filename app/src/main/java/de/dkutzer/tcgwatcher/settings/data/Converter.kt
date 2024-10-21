package de.dkutzer.tcgwatcher.settings.data

import androidx.room.TypeConverter
import de.dkutzer.tcgwatcher.settings.domain.Engines
import de.dkutzer.tcgwatcher.settings.domain.Languages

class Converter {

    @TypeConverter
    fun toLanguage(value: String) = enumValueOf<Languages>(value)

    @TypeConverter
    fun fromLanguage(value: Languages) = value.name

    @TypeConverter
    fun toEngine(value: String) = enumValueOf<Engines>(value)

    @TypeConverter
    fun fromEngine(value: Engines) = value.name
}