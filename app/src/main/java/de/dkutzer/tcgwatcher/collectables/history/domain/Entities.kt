package de.dkutzer.tcgwatcher.collectables.history.domain

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.Instant


@Entity(tableName = "search")
data class SearchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    val searchTerm: String,
    val size: Int,
    val language: String, // the language setting at the time of this search
    val lastUpdated: Long,
    val history: Boolean
)

@Entity(tableName = "search_result_item")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    var searchId: Int,

    val language: String,
    val genre: String = "",
    val type: String = "",
    val rarity: String = "",
    val code: String,
    val externalId: String,
    val externalLink: String,
    val imgLink: String,
    val price: String,
    val priceTrend: String,
    val lastUpdated: Long
)

@Entity(tableName = "product_name")
data class ProductNameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    val productId: Int,
    val language: String,
    val name: String
)

@Entity(tableName = "product_set")
data class ProductSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    val productId: Int,
    val setName: String,
    val setId: String,
    val language: String // Sprache des Sets
)

@Entity(tableName = "product_offer")
data class SellOfferEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    var productId: Int,

    val sellerName: String,
    val sellerLocation: String,
    val productLanguage: String,
    val condition: String,
    val amount: Int,
    val price: String,
    val special: String,
)

@Entity("remote_key")
data class RemoteKeyEntity(
    @PrimaryKey val id: String,
    val nextOffset: Int,
)


data class SearchWithProducts(
    @Embedded val search: SearchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "searchId"
    )
    val products: List<ProductEntity>

) {
    fun isOlderThan(seconds: Long): Boolean {

        return Instant.ofEpochSecond(this.search.lastUpdated).isBefore(Instant.now().minusSeconds(seconds))
    }
}

data class SearchWithProductsAndSellOffers(
    @Embedded val search: SearchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "searchId"
    )
    val productWithSellOffers: List<ProductWithSellOffers>

)

data class ProductWithSellOffers(
    @Embedded val productEntity: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val offers: List<SellOfferEntity>
)

data class ProductWithNamesAndSets(
    @Embedded val productEntity: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val names: List<ProductNameEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val sets: List<ProductSetEntity>
)

