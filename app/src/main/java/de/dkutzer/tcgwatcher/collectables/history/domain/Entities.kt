package de.dkutzer.tcgwatcher.collectables.history.domain

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
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

@Entity(primaryKeys = ["searchId", "productId"], tableName = "search_product_cross_ref")
data class SearchProductCrossRef(
    val searchId: Int,
    val productId: Int
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

interface BasicProduct {
    val productEntity: ProductEntity
    val names: List<ProductNameEntity>
    val set: ProductSetEntity?
}

data class ProductWithSellOffers(
    @Embedded override val productEntity: ProductEntity,
    @Relation(
        parentColumn = "id", 
        entityColumn = "productId"
    )
    val offers: List<SellOfferEntity>,
    @Relation(
        parentColumn = "id", 
        entityColumn = "productId"
    )
    override val names: List<ProductNameEntity>,
    @Relation(
        parentColumn = "id", 
        entityColumn = "productId"
    )
    override val set: ProductSetEntity?
): BasicProduct

data class ProductComposite(
    @Embedded override val productEntity: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    override val  names: List<ProductNameEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    override val set: ProductSetEntity?
): BasicProduct


interface SearchWithProducts {
    val search: SearchEntity
    val products: List<BasicProduct>  // Use BasicProduct here

    fun isOlderThan(seconds: Long): Boolean {
        return Instant.ofEpochSecond(search.lastUpdated)
            .isBefore(Instant.now().minusSeconds(seconds))
    }
}

data class SearchWithFullProductInfo(
    @Embedded override val search: SearchEntity,
    @Relation(
        parentColumn = "id",
        entity = ProductEntity::class,  // Explicit entity
        entityColumn = "id",            // Refers to ProductEntity.id
        associateBy = Junction(
            value = SearchProductCrossRef::class,
            parentColumn = "searchId",
            entityColumn = "productId"
        )
    )
    val fullProducts: List<ProductWithSellOffers>  // Renamed for clarity
) : SearchWithProducts {
    override val products: List<BasicProduct>
        get() = fullProducts  // Implicit cast to BasicProduct
}

data class SearchWithBasicProductsInfo(
    @Embedded override val search: SearchEntity,
    @Relation(
        parentColumn = "id",
        entity = ProductEntity::class,
        entityColumn = "id",
        associateBy = Junction(
            value = SearchProductCrossRef::class,
            parentColumn = "searchId",
            entityColumn = "productId"
        )
    )
    val basicProducts: List<ProductComposite>  // Renamed for clarity
) : SearchWithProducts {
    override val products: List<BasicProduct>
        get() = basicProducts  // Implicit cast to BasicProduct
}