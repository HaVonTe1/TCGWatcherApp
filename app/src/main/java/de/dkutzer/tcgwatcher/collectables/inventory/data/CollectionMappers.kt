package de.dkutzer.tcgwatcher.collectables.inventory.data

import de.dkutzer.tcgwatcher.collectables.inventory.domain.*

fun CollectionEntity.toDomain() = CollectionModel(
    id = id,
    name = name,
    description = description
)

fun CollectionEntryEntity.toDomain() = CollectionEntryModel(
    id = id,
    collectionId = collectionId,
    productId = productId,
    condition = CardCondition.valueOf(condition),
    language = CardLanguage.valueOf(language),
    price = price,
    origin = CardOrigin.valueOf(origin),
    date = java.time.LocalDate.parse(date)
)

fun CollectionModel.toEntity() = CollectionEntity(
    id = id,
    name = name,
    description = description
)

fun CollectionEntryModel.toEntity() = CollectionEntryEntity(
    id = id,
    collectionId = collectionId,
    productId = productId,
    condition = condition.name,
    language = language.name,
    price = price,
    origin = origin.name,
    date = date.toString()
)
