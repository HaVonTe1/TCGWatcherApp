package de.dkutzer.tcgwatcher.settings.data

import de.dkutzer.tcgwatcher.settings.domain.Engines
import de.dkutzer.tcgwatcher.settings.domain.Languages
import de.dkutzer.tcgwatcher.settings.domain.SettingsRepository


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