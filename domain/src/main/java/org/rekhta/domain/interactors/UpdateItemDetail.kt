package org.rekhta.domain.interactors

import com.kafka.data.feature.item.ItemDetailRepository
import kotlinx.coroutines.withContext
import org.rekhta.analytics.LogContentEvent
import org.rekhta.base.AppCoroutineDispatchers
import org.rekhta.base.domain.Interactor
import javax.inject.Inject

class UpdateItemDetail @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val repository: ItemDetailRepository,
    private val logContentEvent: LogContentEvent,
) : Interactor<UpdateItemDetail.Param>() {

    override suspend fun doWork(params: Param) {
        withContext(dispatchers.io) {
            logContentEvent { itemDetailClick(params.contentId) }
            repository.updateItemDetail(params.contentId)
        }
    }

    data class Param(val contentId: String)
}
