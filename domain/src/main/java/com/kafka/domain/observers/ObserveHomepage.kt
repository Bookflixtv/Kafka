package com.kafka.domain.observers

import com.kafka.data.entities.Homepage
import com.kafka.data.entities.HomepageCollection
import com.kafka.data.feature.homepage.HomepageRepository
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import com.kafka.base.CoroutineDispatchers
import com.kafka.base.domain.SubjectInteractor
import javax.inject.Inject

class ObserveHomepage @Inject constructor(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val observeRecentItems: ObserveRecentItems,
    private val homepageRepository: HomepageRepository,
) : SubjectInteractor<Unit, Homepage>() {

    override fun createObservable(params: Unit): Flow<Homepage> {
        return combine(
            observeRecentItems.execute(ObserveRecentItems.Params(RECENT_ITEMS_LIMIT)),
            homepageRepository.observeHomepageCollection(),
        ) { recentItems, collection ->
            val collectionWithRecentItems = collection.map {
                when (it) {
                    is HomepageCollection.RecentItems -> {
                        it.copy(items = recentItems)
                    }

                    else -> it
                }
            }.toPersistentList()

            Homepage(collection = collectionWithRecentItems)
        }.flowOn(coroutineDispatchers.io)
    }
}

private const val RECENT_ITEMS_LIMIT = 10