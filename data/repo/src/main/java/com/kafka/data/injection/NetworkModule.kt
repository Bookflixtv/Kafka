package com.kafka.data.injection

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kafka.data.api.ArchiveService
import com.kafka.data.api.interceptor.AcceptDialogInterceptor
import com.kafka.data.model.SerializationPolymorphicDefaultPair
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.kafka.base.ApplicationScope
import org.kafka.base.Named
import org.threeten.bp.Duration
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

const val baseUrl = "https://archive.org/"

@Component
@ApplicationScope
interface NetworkModule {

    @Provides
    @ApplicationScope
    fun jsonConfigured(serializersModule: SerializersModule) = Json {
        ignoreUnknownKeys = true
        isLenient = true
        this.serializersModule = serializersModule
    }

    @Provides
    @ApplicationScope
    fun httpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    @Provides
    @ApplicationScope
    fun okHttp(
        cache: Cache,
        acceptDialogInterceptor: AcceptDialogInterceptor,
    ) = getBaseBuilder(cache)
        .addInterceptor(acceptDialogInterceptor)
        .build()

    @Provides
    @ApplicationScope
    @ExperimentalSerializationApi
    fun retrofit(
        client: OkHttpClient,
        json: Json,
        loggingInterceptor: HttpLoggingInterceptor,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(client.run { newBuilder().addInterceptor(loggingInterceptor).build() })
            .build()

    @Provides
    fun provideService(
        retrofit: Retrofit,
    ): ArchiveService {
        return retrofit.create(ArchiveService::class.java)
    }

    @Provides
    @Named("downloader")
    fun downloaderOkHttp(
        cache: Cache,
    ) = getBaseBuilder(cache)
        .readTimeout(Config.DOWNLOADER_TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(Config.DOWNLOADER_TIMEOUT, TimeUnit.MILLISECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            },
        )
        .build()

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    @Provides
    @ApplicationScope
    fun provideSerializersModule(
        polymorphicDefaultPairs: Set<SerializationPolymorphicDefaultPair<*>>,
    ): SerializersModule = SerializersModule {
        polymorphicDefaultPairs.forEach { (base, default) ->
            polymorphicDefaultDeserializer(base as KClass<Any>) { default.serializer() }
        }
    }

    private fun getBaseBuilder(cache: Cache): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .cache(cache)
            .readTimeout(Config.API_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(Config.API_TIMEOUT, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
    }

    object Config {
        val API_TIMEOUT = Duration.ofSeconds(40).toMillis()
        val DOWNLOADER_TIMEOUT = Duration.ofMinutes(3).toMillis()
    }
}
