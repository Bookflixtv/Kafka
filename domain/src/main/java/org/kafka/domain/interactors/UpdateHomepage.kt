package org.kafka.domain.interactors

import com.kafka.data.feature.homepage.HomepageRepository
import com.kafka.data.feature.item.ItemRepository
import com.kafka.data.model.ArchiveQuery
import com.kafka.data.model.booksByIdentifiers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.kafka.base.CoroutineDispatchers
import org.kafka.base.domain.Interactor
import org.kafka.domain.interactors.query.BuildRemoteQuery
import javax.inject.Inject

class UpdateHomepage @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val homepageRepository: HomepageRepository,
    private val itemRepository: ItemRepository,
    private val buildRemoteQuery: BuildRemoteQuery,
) : Interactor<Unit>() {

    override suspend fun doWork(params: Unit) {
        withContext(dispatchers.io) {
            homepageRepository.getHomepageIds().mapAsync { ids ->
                val unFetchedIds = ids.filter { !itemRepository.exists(it) }

                if (unFetchedIds.isNotEmpty()) {
                    val query = ArchiveQuery().booksByIdentifiers(unFetchedIds)
                    val items = itemRepository.updateQuery(buildRemoteQuery(query))
                    itemRepository.saveItems(items)
                }
            }
        }
    }
}

private suspend fun <T, R> List<T>.mapAsync(
    mapper: suspend (T) -> R,
): List<R> = coroutineScope { map { async { mapper(it) } }.awaitAll() }
