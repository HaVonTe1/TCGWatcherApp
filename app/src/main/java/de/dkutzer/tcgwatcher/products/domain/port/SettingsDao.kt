package de.dkutzer.tcgwatcher.products.domain.port

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.dkutzer.tcgwatcher.products.domain.SettingsEntity

@Dao
interface SettingsDao {

    @Upsert
    suspend fun save(setting: SettingsEntity)

    @Query(value = "SELECT * FROM SETTINGS t where  t.id = 1")
    fun load(): SettingsEntity?
}