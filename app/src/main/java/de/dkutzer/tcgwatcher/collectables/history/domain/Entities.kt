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
    val language: String,
    val lastUpdated: Long,
    val history: Boolean
)

@Entity(tableName = "search_result_item")
data class ProductItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    var searchId: Int,

    val displayName: String, //TODO: normalize the displayName into seperate Entity to make is multilangual
    val language: String = "en",
    val genre: String = "",
    val type: String = "",
    val rarity: String = "",
    val code: String,
    val orgName: String,
    val cmLink: String,
    val imgLink: String,
    val price: String,
    val priceTrend: String,
    val setName: String, //TODO: normalize the setName into seperate Entity to make is multilangual
    val setLink: String,
    val lastUpdated: Long
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


data class SearchAndProductsEntity(
    @Embedded val search: SearchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "searchId"
    )
    val products: List<ProductItemEntity>

) {
    fun isOlderThan(seconds: Long): Boolean {

        return Instant.ofEpochSecond(this.search.lastUpdated).isBefore(Instant.now().minusSeconds(seconds))
    }
}

data class SearchAndProductsAndSelloffersEntity(
    @Embedded val search: SearchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "searchId"
    )
    val products: List<Product>

){
    fun isOlderThan(seconds: Long): Boolean {

        return Instant.ofEpochSecond(this.search.lastUpdated).isBefore(Instant.now().minusSeconds(seconds))
    }
}

data class Product(
    @Embedded val productItemEntity: ProductItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val offers: List<SellOfferEntity>
)


