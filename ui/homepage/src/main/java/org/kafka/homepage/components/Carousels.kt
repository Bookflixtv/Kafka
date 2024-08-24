package org.kafka.homepage.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kafka.data.entities.Item
import kotlinx.collections.immutable.ImmutableList
import org.kafka.ui.components.item.FeaturedItem
import ui.common.theme.theme.Dimens

@Composable
internal fun Carousels(
    carouselItems: ImmutableList<Item>,
    images: List<String>,
    onBannerClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val state = rememberCarouselState { carouselItems.size }
    HorizontalMultiBrowseCarousel(
        state = state,
        modifier = modifier.padding(horizontal = Dimens.Spacing08, vertical = Dimens.Spacing08),
        preferredItemWidth = CarouselItemPreferredWidth.dp,
        itemSpacing = Dimens.Spacing04,
        contentPadding = PaddingValues(horizontal = Dimens.Spacing16)
    ) { index ->
        carouselItems.getOrNull(index)?.let { item ->
            FeaturedItem(
                item = item,
                label = item.title.takeIf { showLabel },
                imageUrl = images.getOrNull(index),
                onClick = { onBannerClick(item.itemId) },
                modifier = Modifier.maskClip(shape = RoundedCornerShape(Dimens.Radius16))
            )
        }
    }
}

const val CarouselItemPreferredWidth = 500
