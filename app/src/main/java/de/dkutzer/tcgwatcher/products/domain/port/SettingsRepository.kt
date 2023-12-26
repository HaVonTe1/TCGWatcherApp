package de.dkutzer.tcgwatcher.products.domain.port

import de.dkutzer.tcgwatcher.products.domain.Engines
import de.dkutzer.tcgwatcher.products.domain.Languages
import de.dkutzer.tcgwatcher.products.domain.SettingsEntity


interface SettingsRepository {
    fun load(): SettingsEntity?
    suspend fun save(settingsEntity: SettingsEntity)
    suspend fun updateLanguage(lang: String)
    suspend fun updateEngine(engine: String)
}

class SettingsRepositoryImpl(private val settingsDao: SettingsDao) : SettingsRepository {
    override fun load(): SettingsEntity? {
        return settingsDao.load()
    }

    override suspend fun save(settingsEntity: SettingsEntity) {

        settingsDao.save(settingsEntity)
    }

    override suspend fun updateLanguage(lang: String) {

        var settingsEntity = settingsDao.load()
        settingsEntity = if(settingsEntity==null) {
            SettingsEntity(
                language = Languages.valueOf(lang),
                engine = Engines.HTMLUNIT_JS
            )
        } else {
            SettingsEntity(
                language = Languages.valueOf(lang),
                engine = settingsEntity.engine
            )
        }
        settingsDao.save(settingsEntity)

    }

    override suspend fun updateEngine(eng: String) {

        var settingsEntity = settingsDao.load()
        settingsEntity = if(settingsEntity==null) {
            SettingsEntity(
                language = Languages.DE,
                engine = Engines.valueOf(eng),
            )
        } else {
            SettingsEntity(
                language = settingsEntity.language,
                engine =  Engines.valueOf(eng),
            )
        }
        settingsDao.save(settingsEntity)

    }


}