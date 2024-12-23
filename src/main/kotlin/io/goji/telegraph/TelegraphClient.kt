package io.goji.io.goji.telegraph

import io.goji.io.goji.telegraph.models.*
import io.goji.telegraph.HTMLParser
import io.goji.telegraph.types.ApiResponse
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.ProxyOptions
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.serialization.json.*
class TelegraphClient(
    private val accessToken: String? = null,
    private val proxyOptions: ProxyOptions? = null
) {
    private val vertx = Vertx.vertx()
    private val client: WebClient
    private val json = Json { ignoreUnknownKeys = true }

    init {
        val options = WebClientOptions().apply {
            proxyOptions?.let {
                proxyOptions = io.vertx.core.net.ProxyOptions().apply {
                    type = it.type
                    host = it.host
                    port = it.port
                }
            }
        }
        client = WebClient.create(vertx, options)
    }

    companion object {
        const val API_BASE_URL = "https://api.telegra.ph"

        suspend fun create(
            shortName: String,
            authorName: String? = null,
            authorUrl: String? = null,
            proxyOptions: ProxyOptions? = null
        ): TelegraphClient {
            val client = TelegraphClient(proxyOptions = proxyOptions)
            val account = client.createAccount(shortName, authorName, authorUrl)
            return TelegraphClient(account.accessToken, proxyOptions)
        }

        suspend fun load(
            accessToken: String,
            proxyOptions: ProxyOptions? = null
        ): TelegraphClient {
            val client = TelegraphClient(accessToken, proxyOptions)
            // Verify the token by making a test request
            client.getAccountInfo()
            return client
        }
    }

    /**
     * Create a new Telegraph account.
     * Original URL: [https://telegra.ph/api#createAccount](https://telegra.ph/api#createAccount)
     * @param shortName 1-32 characters
     * @param authorName 0-128 characters
     * @param authorUrl 0-512 characters
     * @return The created account
     */
    suspend fun createAccount(
        shortName: String,
        authorName: String? = null,
        authorUrl: String? = null
    ): Account {
        val params = buildJsonObject {
            put("short_name", shortName)
            authorName?.let { put("author_name", it) }
            authorUrl?.let { put("author_url", it) }
        }

        return post<Account>("createAccount", params)
//        val response = client.postAbs("$API_BASE_URL/createAccount")
//            .sendJson(params)
//            .coAwait()
//
//        return when (response.statusCode()) {
//            200 -> {
//                val apiResponse = json.decodeFromString<ApiResponse<Account>>(response.bodyAsString())
//                if (apiResponse.ok) {
//                    apiResponse.result ?: throw TelegraphException("No result in response")
//                } else {
//                    throw TelegraphException(apiResponse.error ?: "Unknown error")
//                }
//            }
//            else -> throw TelegraphException("HTTP ${response.statusCode()}: ${response.bodyAsString()}")
//        }
    }


    /**
     * EditAccountInfo updates information about a Telegraph account.
     * Original URL: [https://telegra.ph/api#editAccountInfo](https://telegra.ph/api#editAccountInfo)
     * @param shortName 1-32 characters
     * @param authorName 0-128 characters
     * @param authorUrl 0-512 characters
     * @return The edited account
     */
    suspend fun editAccountInfo(
        shortName: String,
        authorName: String? = null,
        authorUrl: String? = null
    ): Account {
        val params = buildJsonObject {
            put("access_token", requireAccessToken())
            put("short_name", shortName)
            authorName?.let { put("author_name", it) }
            authorUrl?.let { put("author_url", it) }
        }

        return post<Account>("editAccountInfo", params)
    }


    /**
     *GetAccountInfo fetches information about a Telegraph account.
     * Original URL: [https://telegra.ph/api#getAccountInfo](https://telegra.ph/api#getAccountInfo)
     * @param fields List of account fields to return. Available fields: short_name, author_name, author_url, auth_url, page_count default ["short_name", "author_name", "author_url"]
     * @return The account information
     */
    suspend fun getAccountInfo(fields: List<String>? = null): Account {
        val params = buildJsonObject {
            put("access_token", requireAccessToken())
            put("fields", JsonArray(
                (fields ?: listOf("short_name", "author_name", "author_url"))
                    .map { JsonPrimitive(it) }
            ))
        }

        return post<Account>("getAccountInfo", params)
    }

    /**
     * RevokeAccessToken revokes access_token and generates a new one, for example, if the user would like to reset all connected sessions, or you have reasons to believe the token was compromised.
     * Original URL: [https://telegra.ph/api#revokeAccessToken](https://telegra.ph/api#revokeAccessToken)
     */
    suspend fun revokeAccessToken(): Account {
        val params = buildJsonObject {
            put("access_token", requireAccessToken())
        }

        return post<Account>("revokeAccessToken", params)
    }


    /**
     * Create a new page on Telegraph.
     * Original URL: [https://telegra.ph/api#createPage](https://telegra.ph/api#createPage)
     * @param title 1-256 characters
     * @param content The page content
     * @param authorName 0-128 characters
     * @param authorUrl 0-512 characters
     * @param returnContent If true, a content field will be returned in the Page object
     * @return The created page
     * @see Node
     */
    suspend fun createPage(
        title: String,
        content: List<Node>,
        authorName: String? = null,
        authorUrl: String? = null,
        returnContent: Boolean = false
    ): Page {
        val params = buildJsonObject {
            put("access_token", requireAccessToken())
            put("title", title)
            put("content", json.encodeToJsonElement(content))
            authorName?.let { put("author_name", it) }
            authorUrl?.let { put("author_url", it) }
            put("return_content", returnContent)
        }

        return post<Page>("createPage", params)
    }


    /**
     * Create a new page on Telegraph with HTML content.
     * Original URL: [https://telegra.ph/api#createPage](https://telegra.ph/api#createPage)
     * @param title 1-256 characters
     * @param htmlContent The page content in HTML format
     * @param authorName 0-128 characters
     * @param authorUrl 0-512 characters
     * @param returnContent If true, a content field will be returned in the Page object
     * @return The created page
     */
    suspend fun createPageWithHTML(
        title: String,
        htmlContent: String,
        authorName: String? = null,
        authorUrl: String? = null,
        returnContent: Boolean = false
    ): Page {
        val nodes = HTMLParser.parseHTML(htmlContent)
        return createPage(title, nodes, authorName, authorUrl, returnContent)
    }


    /**
     * Edit an existing Telegraph page.
     * Original URL: [https://telegra.ph/api#editPage](https://telegra.ph/api#editPage)
     * @param path The path of the page
     * @param title 1-256 characters
     * @param content The new page content
     * @param authorName 0-128 characters
     * @param authorUrl 0-512 characters
     * @param returnContent If true, a content field will be returned in the Page object
     * @return The edited page
     * @see Node
     */
    suspend fun editPage(
        path: String,
        title: String,
        content: List<Node>,
        authorName: String? = null,
        authorUrl: String? = null,
        returnContent: Boolean = false
    ): Page {
        val params = buildJsonObject {
            put("access_token", requireAccessToken())
            put("title", title)
            put("content", json.encodeToJsonElement(content))
            authorName?.let { put("author_name", it) }
            authorUrl?.let { put("author_url", it) }
            put("return_content", returnContent)
        }

        return post<Page>("editPage/$path", params)
    }


    /**
     * GetPage fetches a Telegraph page.
     * Original URL: [https://telegra.ph/api#getPage](https://telegra.ph/api#getPage)
     * @param path The path of the page
     * @param returnContent If true, a content field will be returned in the Page object
     * @return The fetched page
     */
    suspend fun getPage(
        path: String,
        returnContent: Boolean = false
    ): Page {
        val params = buildJsonObject {
            put("return_content", returnContent)
        }

        return post<Page>("getPage/$path", params)
    }


    /**
     * GetPageList fetches a list of pages belonging to a Telegraph account.
     * Original URL: [https://telegra.ph/api#getPageList](https://telegra.ph/api#getPageList)
     * @param offset Sequential number of the first page to be returned (default 0)
     * @param limit Limits the number of pages to be retrieved (0-200, default 50)
     * @return The fetched page list
     */
    suspend fun getPageList(
        offset: Int = 0,
        limit: Int = 50
    ): PageList {
        val params = buildJsonObject {
            put("access_token", requireAccessToken())
            put("offset", offset)
            put("limit", limit.coerceIn(0, 200))
        }

        return post<PageList>("getPageList", params)
    }

    /**
     * GetViews fetches the number of views for a Telegraph page.
     * Original URL: [https://telegra.ph/api#getViews](https://telegra.ph/api#getViews)
     * @param path The path of the page
     * @param year Required if month is passed, 2000-2100
     * @param month Required if day is passed, 1-12
     * @param day Required if hour is passed, 1-31
     * @param hour pass -1 if none, 0-24
     * @return The number of views
     * @see PageViews
     *
     */
    suspend fun getViews(
        path: String,
        year: Int? = null,
        month: Int? = null,
        day: Int? = null,
        hour: Int? = null
    ): PageViews {
        //Todo: Add validation for year, month, day, hour
        val params = buildJsonObject {
            year?.let { put("year", it) }
            month?.let { put("month", it) }
            day?.let { put("day", it) }
            hour?.let { put("hour", it) }
        }

        return post<PageViews>("getViews/$path", params)
    }


    /**
     * GetAccountInfo fetches information about a Telegraph account.
     */
    fun requireAccessToken(): String {
        return accessToken ?: throw TelegraphException("Access token is required")
    }

    private suspend inline fun <reified T> post(
        endpoint: String,
        params: JsonObject
    ): T {
        val response = client.postAbs("$API_BASE_URL/$endpoint")
            .putHeader("content-type", "application/json")
            .putHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
            .sendJson(params)
            .coAwait()

        val jsonResponse = json.parseToJsonElement(response.bodyAsString())
        // 如果返回的json中没有result字段, 查看是否有错误信息
//        val result = jsonResponse.jsonObject["result"]
//            ?: throw TelegraphException("No result in response")
        println(jsonResponse.toString())
        val result = jsonResponse.jsonObject["result"] ?:
            throw TelegraphException("No result in response, error: ${jsonResponse.jsonObject["error"]?: "Unknown error"}")
        println("result: $result")
        return json.decodeFromJsonElement(result)
    }


//    private inline fun <reified T> handleResponse(response: HttpResponse<Buffer>): T {
//        return when (response.statusCode()) {
//            200 -> {
//                val apiResponse = json.decodeFromString<ApiResponse<T>>(response.bodyAsString())
//                if (apiResponse.ok) {
//                    apiResponse.result ?: throw TelegraphException("No result in response")
//                } else {
//                    throw TelegraphException(apiResponse.error ?: "Unknown error")
//                }
//            }
//            else -> throw TelegraphException("HTTP ${response.statusCode()}: ${response.bodyAsString()}")
//        }
//    }

    fun close() {
        client.close()
        vertx.close()
    }
}

class TelegraphException(message: String) : Exception(message)
