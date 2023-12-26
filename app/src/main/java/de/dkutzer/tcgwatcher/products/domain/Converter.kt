package de.dkutzer.tcgwatcher.products.domain

import androidx.room.TypeConverter

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