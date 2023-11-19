package org.kafka.homepage.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafka.data.entities.RecentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.map
import org.kafka.analytics.logger.Analytics
import org.kafka.base.extensions.stateInDefault
import org.kafka.domain.observers.ObserveRecentItems
import org.kafka.navigation.Navigator
import org.kafka.navigation.Screen
import javax.inject.Inject

@HiltViewModel
class RecentViewModel @Inject constructor(
    observeRecentItems: ObserveRecentItems,
    private val navigator: Navigator,
    private val analytics: Analytics
) : ViewModel() {
    val state = observeRecentItems.flow.map { recentItems ->
        RecentViewState(recentItems = recentItems.map { it.recentItem }.toPersistentList())
    }.stateInDefault(
        scope = viewModelScope,
        initialValue = RecentViewState(),
    )

    init {
        observeRecentItems(Unit)
    }

    fun openItemDetail(itemId: String) {
        analytics.log { this.openItemDetail(itemId) }
        navigator.navigate(Screen.ItemDetail.createRoute(navigator.currentRoot.value, itemId))
    }
}

data class RecentViewState(
    val recentItems: ImmutableList<RecentItem> = persistentListOf()
)
