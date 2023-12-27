package de.dkutzer.tcgwatcher.products.domain.port

import androidx.compose.ui.res.stringResource
import de.dkutzer.tcgwatcher.products.domain.Engines
import de.dkutzer.tcgwatcher.products.domain.Languages
import de.dkutzer.tcgwatcher.products.domain.SettingsEntity


interface SettingsRepository {
    fun load(): SettingsEntity
    suspend fun save(settingsEntity: SettingsEntity)
    suspend fun updateLanguage(lang: Languages)
    suspend fun updateEngine(engine: Engines)
}

class SettingsRepositoryImpl(private val settingsDao: SettingsDao) : SettingsRepository {

    override fun load(): SettingsEntity {
        var settingsEntity = settingsDao.load()
        if (settingsEntity == null) {
            settingsEntity = SettingsEntity(language = Languages.DE, engine = Engines.HTMLUNIT_NOJS)
//            settingsDao.save(settingsEntity)
        }
        return settingsEntity
    }

    override suspend fun save(settingsEntity: SettingsEntity) {

        settingsDao.save(settingsEntity)
    }

    override suspend fun updateLanguage(lang: Languages) {

        var settingsEntity = settingsDao.load()
        settingsEntity = if (settingsEntity == null) {
            SettingsEntity(
                language = lang,
                engine = Engines.HTMLUNIT_JS
            )
        } else {
            SettingsEntity(
                language = lang,
                engine = settingsEntity.engine
            )
        }
        settingsDao.save(settingsEntity)

    }



    override suspend fun updateEngine(eng: Engines) {

        var settingsEntity = settingsDao.load()
        settingsEntity = if(settingsEntity==null) {
            SettingsEntity(
                language = Languages.DE,
                engine = eng,
            )
        } else {
            SettingsEntity(
                language = settingsEntity.language,
                engine =  eng,
            )
        }
        settingsDao.save(settingsEntity)

    }


}