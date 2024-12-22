
package io.goji.telegraph.types

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val ok: Boolean,
    val error: String? = null,
    val result: T? = null
)
