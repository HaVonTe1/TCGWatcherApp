package de.dkutzer.tcgwatcher.settings.control

import de.dkutzer.tcgwatcher.settings.entity.Engines
import de.dkutzer.tcgwatcher.settings.entity.Languages
import de.dkutzer.tcgwatcher.settings.entity.SettingsEntity


interface SettingsRepository {
    suspend fun load(): SettingsEntity
    suspend fun save(settingsEntity: SettingsEntity)
    suspend fun updateLanguage(lang: Languages)
    suspend fun updateEngine(engine: Engines)
}

class SettingsRepositoryImpl(private val settingsDao: SettingsDao) : SettingsRepository {

    override suspend fun load(): SettingsEntity {
        var settingsEntity = settingsDao.load()
        if (settingsEntity == null) {
            settingsEntity = SettingsEntity(language = Languages.DE, engine = Engines.HTMLUNIT_NOJS)
            settingsDao.save(settingsEntity)
        }
        return settingsEntity
    }

    override suspend fun save(settingsEntity: SettingsEntity) {

        settingsDao.save(settingsEntity)
    }

    override suspend fun updateLanguage(lang: Languages) {
        settingsDao.updateLanguage(lang)
    }

    override suspend fun updateEngine(engine: Engines) {
        settingsDao.updateEngine(engine)
    }

}