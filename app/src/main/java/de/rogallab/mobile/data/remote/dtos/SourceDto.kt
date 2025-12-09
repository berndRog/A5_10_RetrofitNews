package de.rogallab.mobile.data.remote.dtos

import kotlinx.serialization.Serializable

@Serializable
data class SourceDto(
   val id: String? = "",
   val name: String? = ""
)