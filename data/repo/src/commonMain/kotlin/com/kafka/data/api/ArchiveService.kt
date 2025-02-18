package com.kafka.data.api

import com.kafka.data.model.item.ItemDetailResponse
import com.kafka.data.model.item.SearchResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import me.tatarka.inject.annotations.Inject
import com.kafka.base.ApplicationScope

/**
 * @author Vipul Kumar; dated 29/11/18.
 *
 * Configures the API module and provides services to interact with the APIs.
 */
@ApplicationScope
class ArchiveService @Inject constructor(private val httpClient: HttpClient) {
    suspend fun search(
        query: String?,
        output: String = "json",
        rows: String = "200",
        page: String = "1",
        sort: String = "-downloads",
    ): SearchResponse = httpClient.get {
        url("https://archive.org/advancedsearch.php")
        parameter("q", query)
        parameter("output", output)
        parameter("rows", rows)
        parameter("page", page)
        parameter("sort", sort)
    }.body()

    suspend fun getItemDetail(id: String?): ItemDetailResponse = httpClient.get {
        url("https://archive.org/metadata/$id")
    }.body()
}
