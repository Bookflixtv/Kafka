package com.kafka.user.review

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import com.google.android.play.core.review.ReviewManagerFactory
import com.kafka.data.prefs.PreferencesStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import org.kafka.analytics.AppReviewManager
import org.kafka.analytics.logger.Analytics
import org.kafka.base.ProcessLifetime
import org.kafka.base.errorLog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppReviewManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @ProcessLifetime private val coroutineScope: CoroutineScope,
    private val analytics: Analytics,
    preferencesStore: PreferencesStore,
) : AppReviewManager {
    private val manager = ReviewManagerFactory.create(context)
    private val itemOpensPrefKey =
        intPreferencesKey("item_opens") // this is actually used when user plays/reads an item, not when user opens the item

    private val itemOpens = preferencesStore.getStateFlow(
        keyName = itemOpensPrefKey, scope = coroutineScope, initialValue = 0
    )

    override fun showReviewDialog(activity: Activity) {
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                analytics.log { showAppReviewDialog() }
                manager.launchReviewFlow(activity, task.result)
            } else {
                errorLog(task.exception) { "Error while generating review info" }
            }
        }
    }

    /**
     * Review dialog is shown based on item opens count
     * */
    override suspend fun incrementItemOpenCount() {
        itemOpens.value++
    }

    override val totalItemOpens: Int
        get() = itemOpens.value
}
