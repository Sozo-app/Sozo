package com.sozo.cli

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sozo.nicehttp.Requests
import com.sozo.nicehttp.ResponseParser
import kotlin.reflect.KClass

data class GithubJson(
    @JsonProperty("description") val description: String,
    @JsonProperty("html_url") val htmlUrl: String,
    @JsonProperty("stargazers_count") val stargazersCount: Int,
    @JsonProperty("private") val private: Boolean
)

suspend fun main() {
    /**
     * Implement your own json parsing to then do request.parsed<T>()
     * */
    val parser = object : ResponseParser {
        val mapper: ObjectMapper = jacksonObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
        )

        override fun <T : Any> parse(text: String, kClass: KClass<T>): T {
            return mapper.readValue(text, kClass.java)
        }

        override fun <T : Any> parseSafe(text: String, kClass: KClass<T>): T? {
            return try {
                mapper.readValue(text, kClass.java)
            } catch (e: Exception) {
                null
            }
        }


    }

    val requests = Requests(responseParser = parser)

    // Example for query selector
    val doc = requests.get("https://github.com/Blatzar/NiceHttp").document
    println("Selector description: ${doc.select("p.f4.my-3").text()}")

    // Example for json Parser
    val json =
        requests.get("https://api.github.com/repos/blatzar/nicehttp")
            .parsed<GithubJson>()
    println("JSON description: ${json.description}")
}