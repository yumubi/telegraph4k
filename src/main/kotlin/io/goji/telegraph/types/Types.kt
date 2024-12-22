package io.goji.io.goji.telegraph.models

import io.goji.io.goji.telegraph.NodeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Account(
    @SerialName("short_name") val shortName: String,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("author_url") val authorUrl: String? = null,

    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("auth_url") val authUrl: String? = null,
    @SerialName("page_count") val pageCount: Int? = null
)

@Serializable
data class Page(
    val path: String,
    val url: String,
    val title: String,
    val description: String,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("author_url") val authorUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val content: List<Node>? = null,
    val views: Int,
    @SerialName("can_edit") val canEdit: Boolean? = null
)

@Serializable
data class PageList(
    @SerialName("total_count") val totalCount: Int,
    val pages: List<Page>
)

@Serializable
data class PageViews(
    val views: Int
)


@Serializable
data class NodeElement(
    val tag: String,
    val attrs: Map<String, String>? = null,
    val children: List<Node>? = null
)


typealias Node = @Serializable(with = NodeSerializer::class) Any
