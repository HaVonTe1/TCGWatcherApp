package de.dkutzer.tcgwatcher.collectables.history.domain

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.Instant


/*
SearchEntity represents a cached search operation in the database.
It stores search parameters like the search term, result size, language settings,
and tracks when the search was last updated and whether it should be used for the search history
meaning if it should be suggested as a search term when the user starts typing a new query in the search field.
 */
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

/*
When a search is performed all found results are stored as a Product.
This entity is associated with a search by 'SearchProductCrossRef'.
 */
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

/*
This cross table connects a search with a found product.
A prodcut can be found via different searches.
 */
@Entity(primaryKeys = ["searchId", "productId"], tableName = "search_product_cross_ref")
data class SearchProductCrossRef(
    val searchId: Int,
    val productId: Int
)
/*
Every product is internatianlised. Which means the same product is available in several
languages. This entity stores the name of a product of a given language.
 */
@Entity(tableName = "product_name")
data class ProductNameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    var productId: Int,
    val language: String,
    val name: String
)

/*
This entity is only a cross reference entity between a product and its set.
Most or all products belong to a series or set. These can have subsets.
e.g:
Karmesin & Purpur : Base
Karmesin & Purpur : 151
Karmesin & Purpur : Ewige Rivalen
Schwarz & Weiß: Base
etc
 */

@Entity(tableName = "product_set")
data class  ProductSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    val productId: Int,
    val setId: String,
)

@Entity(tableName = "set_name")
data class SetNameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    var setId: Int,
    val language: String,
    val name: String
)

/*
A Set is a group of products.
 */
@Entity(tableName = "set")
data class SetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @Relation(
        parentColumn = "id",
        entityColumn = "setId"
    )
    val names: List<SetNameEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "setId"
    )
    val series: Series,

    //TODO: add third party ID relationsships
)

/*
A Series is a group of Sets.
 */
@Entity(tableName = "series")
data class Series(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @Relation(
        parentColumn = "id",
        entityColumn = "seriesId"
    )
    val names: List<SeriesNameEntity>,

    //TODO: add third party ID relationsships
)

/*
I10N names for a series.
 */
@Entity(tableName = "series_name")
data class SeriesNameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    var seriesId: Int,
    val language: String,
    val name: String
)

/*
A product does usualy have offers on the market.
 */
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

/*
This is a kind of projection (entity) to a Product
with base data like the i10n names and
the relation to a set (and series)
AND to the sell offers.

It is used for a detail view of a product.
 */
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

/*
This is a kind of projection (entity) to a Product
with base data like the i10n names and
the relation to a set (and series).

It is used for fast searching of products.
 */
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
    val products: List<BasicProduct>

    fun isOlderThan(seconds: Long): Boolean {
        return Instant.ofEpochSecond(search.lastUpdated)
            .isBefore(Instant.now().minusSeconds(seconds))
    }
}

data class SearchWithFullProductInfo(
    @Embedded override val search: SearchEntity,
    @Relation(
        parentColumn = "id",
        entity = ProductEntity::class,
        entityColumn = "id",            // Refers to ProductEntity.id
        associateBy = Junction(
            value = SearchProductCrossRef::class,
            parentColumn = "searchId",
            entityColumn = "productId"
        )
    )
    val fullProducts: List<ProductWithSellOffers>
) : SearchWithProducts {
    override val products: List<BasicProduct>
        get() = fullProducts
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
    val basicProducts: List<ProductComposite>
) : SearchWithProducts {
    override val products: List<BasicProduct>
        get() = basicProducts
}