package de.dkutzer.tcgwatcher.products.domain.port

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import de.dkutzer.tcgwatcher.products.domain.Engines
import de.dkutzer.tcgwatcher.products.domain.Languages
import de.dkutzer.tcgwatcher.products.domain.SettingsEntity

@Dao
interface SettingsDao {

    @Upsert
    suspend fun save(setting: SettingsEntity)

    @Query(value = "SELECT * FROM SETTINGS t where  t.id = 1")
    fun load(): SettingsEntity?

    @Query("UPDATE settings  SET language = :lang WHERE id = 1")
    fun updateLanguage(lang: Languages)

    @Query("UPDATE settings  SET engine = :eng WHERE id = 1")
    fun updateEngine(eng: Engines)

}