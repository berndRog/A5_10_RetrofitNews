package de.rogallab.mobile.data.remote.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import de.rogallab.mobile.Globals
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

fun createRetrofit(
   okHttpClient: OkHttpClient,
) : Retrofit {
   logDebug("<-Retrofit", "create()")

   val json = Json {
      ignoreUnknownKeys = true      // wie lenient GSON: unbekannte Felder ignorieren
      isLenient = true              // toleranter Parser (z.B. fehlende Quotes etc.)
      encodeDefaults = true         // default-Werte werden mit serialisiert
      explicitNulls = false         // nulls ggf. weglassen (je nach API)
   }
   val contentType = "application/json".toMediaType()

   return Retrofit.Builder()
      .baseUrl(Globals.BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(json.asConverterFactory(contentType))
      .build()
}