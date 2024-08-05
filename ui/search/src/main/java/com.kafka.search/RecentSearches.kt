package com.kafka.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kafka.data.entities.RecentSearch
import org.kafka.common.animation.Delayed
import org.kafka.common.image.Icons
import org.kafka.common.simpleClickable
import org.kafka.common.widgets.IconResource
import org.kafka.search.R
import org.kafka.ui.components.LabelMedium
import ui.common.theme.theme.Dimens

@Composable
fun RecentSearches(
    recentSearches: List<RecentSearch>,
    onSearchClicked: (RecentSearch) -> Unit,
    onRemoveSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Delayed(modifier = modifier) {
        LazyColumn(contentPadding = contentPadding) {
            item {
                LabelMedium(
                    text = stringResource(R.string.recent_searches),
                    modifier = Modifier.padding(Dimens.Spacing16)
                )
            }

            items(recentSearches) { recentSearch ->
                RecentSearchItem(
                    recentSearch = recentSearch,
                    onSearchClicked = onSearchClicked,
                    onRemoveSearch = onRemoveSearch,
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

@Composable
fun RecentSearchItem(
    recentSearch: RecentSearch,
    modifier: Modifier = Modifier,
    onSearchClicked: (RecentSearch) -> Unit,
    onRemoveSearch: (String) -> Unit,
) {
    SelectionContainer {
        Row(
            modifier = modifier
                .clickable(onClick = { onSearchClicked(recentSearch) })
                .padding(horizontal = Dimens.Spacing24, vertical = Dimens.Spacing08),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = recentSearch.searchTerm,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconResource(
                modifier = Modifier
                    .simpleClickable { onRemoveSearch(recentSearch.searchTerm) }
                    .padding(10.dp)
                    .size(Dimens.Spacing24),
                imageVector = Icons.XCircle
            )
        }
    }
}


