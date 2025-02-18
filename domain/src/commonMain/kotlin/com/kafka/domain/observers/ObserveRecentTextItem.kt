package com.kafka.domain.observers

import com.kafka.data.dao.RecentTextDao
import com.kafka.data.entities.RecentTextItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import com.kafka.base.CoroutineDispatchers
import com.kafka.base.debug
import com.kafka.base.domain.SubjectInteractor
import javax.inject.Inject

/**
 * Interactor for updating the homepage.
 * */
class ObserveRecentTextItem @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val recentTextDao: RecentTextDao,
) : SubjectInteractor<String, RecentTextItem?>() {

    override fun createObservable(params: String): Flow<RecentTextItem?> {
        return recentTextDao.observe(params)
            .onEach { debug { "recent item $it" } }
            .flowOn(dispatchers.io)
    }
}
