package com.kafka.data.feature.homepage

import com.kafka.data.dao.ItemDao
import com.kafka.data.entities.HomepageCollection
import com.kafka.data.feature.homepage.HomepageMapperConfig.shuffleIndices
import com.kafka.data.model.homepage.HomepageCollectionResponse
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

object HomepageMapperConfig {
    val shuffleIndices = (0 until 50).shuffled()
}

class HomepageMapper @Inject constructor(private val itemDao: ItemDao) {

    fun map(collection: List<HomepageCollectionResponse>): Flow<List<HomepageCollection>> {
        return combine(
            collection.map { homepageItem ->
                when (homepageItem) {
                    is HomepageCollectionResponse.Row -> homepageItem.mapRows()
                    is HomepageCollectionResponse.Column -> homepageItem.mapColumn()
                    is HomepageCollectionResponse.FeaturedItem -> homepageItem.mapFeatured()
                    is HomepageCollectionResponse.RecentItems -> homepageItem.mapRecentItems()
                    is HomepageCollectionResponse.Grid -> homepageItem.mapGrid()
                    is HomepageCollectionResponse.PersonRow -> homepageItem.mapPersonRow()
                    is HomepageCollectionResponse.Subjects -> homepageItem.mapSubjects()
                    is HomepageCollectionResponse.Unknown -> flowOf(null)
                }
            },
        ) { homepageItems -> homepageItems.filterNotNull().toList() }
    }

    private fun HomepageCollectionResponse.Row.mapRows(): Flow<HomepageCollection.Row> {
        val itemIdList = itemIds.split(", ")
            .run { if (shuffle) shuffleInSync() else this }

        return itemDao.observe(itemIdList).map { items ->
            val sortedItems = items.sortedBy { item -> itemIdList.indexOf(item.itemId) }
            HomepageCollection.Row(
                labels = label.splitLabel().toPersistentList(),
                items = sortedItems.toPersistentList(),
                clickable = clickable,
                shuffle = shuffle
            )
        }
    }

    private fun HomepageCollectionResponse.PersonRow.mapPersonRow(): Flow<HomepageCollection.PersonRow> {
        val indices = itemIds.split(", ").indices
            .run { if (shuffle) shuffleInSync() else this }
        val itemIdList = indices.map { index -> itemIds.split(", ")[index] }
        val images = indices.map { index -> image.getOrNull(index) }

        val personRow = HomepageCollection.PersonRow(
            items = itemIdList.toPersistentList(),
            images = images.mapNotNull { it?.downloadURL }.toPersistentList(),
            enabled = enabled,
            clickable = clickable,
            shuffle = shuffle
        )

        return flowOf(personRow)
    }

    private fun HomepageCollectionResponse.Subjects.mapSubjects(): Flow<HomepageCollection.Subjects> {
        val itemIdList = itemIds.split(", ")
            .run { if (shuffle) shuffleInSync() else this }

        val subjects = HomepageCollection.Subjects(
            items = itemIdList.toPersistentList(),
            clickable = clickable,
            enabled = enabled,
            shuffle = shuffle
        )

        return flowOf(subjects)
    }

    private fun HomepageCollectionResponse.Column.mapColumn(): Flow<HomepageCollection.Column> {
        val itemIdList = itemIds.split(", ")
            .run { if (shuffle) shuffleInSync() else this }

        return itemDao.observe(itemIdList).map { items ->
            val sortedItems = items.sortedBy { item -> itemIdList.indexOf(item.itemId) }
            HomepageCollection.Column(
                labels = label.splitLabel().toPersistentList(),
                items = sortedItems.toPersistentList(),
                clickable = clickable,
                shuffle = shuffle
            )
        }
    }

    private fun HomepageCollectionResponse.Grid.mapGrid(): Flow<HomepageCollection.Grid> {
        val itemIdList = itemIds.split(", ")
            .run { if (shuffle) shuffleInSync() else this }

        return itemDao.observe(itemIdList).map { items ->
            val sortedItems = items.sortedBy { item -> itemIdList.indexOf(item.itemId) }
            HomepageCollection.Grid(
                labels = label.splitLabel().toPersistentList(),
                items = sortedItems.toPersistentList(),
                clickable = clickable,
                shuffle = shuffle
            )
        }
    }

    // todo: items are filled later on domain layer, find a way to fill them here
    private fun HomepageCollectionResponse.RecentItems.mapRecentItems() =
        flowOf(HomepageCollection.RecentItems(persistentListOf(), enabled))

    private fun HomepageCollectionResponse.FeaturedItem.mapFeatured() =
        itemDao.observe(itemIds.split(", ")).map { items ->
            val itemIdsIndexMap = itemIds.split(", ")
                .run { if (shuffle) shuffleInSync() else this }
                .withIndex()
                .associate { it.value to it.index }
            val sortedItems = items.sortedBy { itemIdsIndexMap[it.itemId] ?: Int.MAX_VALUE }

            HomepageCollection.FeaturedItem(
                label = label,
                items = sortedItems.toPersistentList(),
                image = image?.map { it.downloadURL }?.toPersistentList() ?: persistentListOf(),
                enabled = enabled,
                shuffle = shuffle
            )
        }

    private fun String?.splitLabel(separator: String = ", ") =
        this.orEmpty().split(separator).filter { it.isNotEmpty() }

    private fun <T> Iterable<T>.shuffleInSync(): List<T> {
        return withIndex()
            .sortedBy { (index, _) -> shuffleIndices.getOrElse(index) { index } }
            .map { it.value }
    }
}
