package com.animestudios.animeapp.parsers.extractors

import android.util.Base64
import com.animestudios.animeapp.anilist.api.common.Anilist.okHttpClient
import com.animestudios.animeapp.findBetween
import com.animestudios.animeapp.parsers.*
import com.animestudios.animeapp.tools.FileUrl
import com.animestudios.animeapp.tools.Mapper
import com.animestudios.animeapp.tools.client
import kotlinx.serialization.Serializable
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Suppress("BlockingMethodInNonBlockingContext")
class RapidCloud(override val server: VideoServer) : VideoExtractor() {

    override suspend fun extract(): VideoContainer {
        println(server.embed.url)
        val videos = mutableListOf<Video>()
        val subtitles = mutableListOf<Subtitle>()

        val sId = wss()
        val decryptKey = decryptKey()

        if (sId.isNotEmpty() && decryptKey.isNotEmpty()) {
            val jsonLink = "https://rapid-cloud.co/ajax/embed-6/getSources?id=${
                server.embed.url.findBetween("/embed-6/", "?")!!
            }&sId=$sId"
            val response = client.get(jsonLink)

            val sourceObject = if (response.text.contains("encrypted")) {
                val encryptedMap = response.parsedSafe<SourceResponse.Encrypted>()
                val sources = encryptedMap?.sources

                if (sources == null || encryptedMap.encrypted == false)
                    response.parsedSafe()
                else {
                    val decrypted = decryptMapped<List<SourceResponse.Track>>(sources, decryptKey)
                    SourceResponse(sources = decrypted, tracks = encryptedMap.tracks)
                }
            }
            else response.parsedSafe()

            sourceObject?.sources?.forEach {
                videos.add(Video(0, VideoType.M3U8, FileUrl(it.file ?: return@forEach)))
            }
            sourceObject?.sourcesBackup?.forEach {
                videos.add(Video(0, VideoType.M3U8, FileUrl(it.file ?: return@forEach), extraNote = "Backup"))
            }
            sourceObject?.tracks?.forEach {
                if (it.kind == "captions" && it.label != null && it.file != null)
                    subtitles.add(Subtitle(it.label, it.file))
            }
        }

        return VideoContainer(videos, subtitles)
    }

    companion object {
        private suspend fun decryptKey(): String {
            return client.get("https://raw.githubusercontent.com/enimax-anime/key/e6/key.txt").text
        }

        private suspend fun wss(): String {
            var sId = client.get("https://api.enime.moe/tool/rapid-cloud/server-id", mapOf("User-Agent" to "Saikou")).text
            if (sId.isEmpty()) {
                val latch = CountDownLatch(1)
                val listener = object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send("40")
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        when {
                            text.startsWith("40") -> {
                                sId = text.findBetween("40{\"sid\":\"", "\"}") ?: ""
                                latch.countDown()
                            }
                            text == "2"           -> webSocket.send("3")
                        }
                    }
                }
                okHttpClient.newWebSocket(
                    Request.Builder().url("wss://ws1.rapid-cloud.co/socket.io/?EIO=4&transport=websocket").build(),
                    listener
                )
                latch.await(30, TimeUnit.SECONDS)
            }
            return sId
        }

        private fun md5(input: ByteArray): ByteArray {
            return MessageDigest.getInstance("MD5").digest(input)
        }

        private fun generateKey(salt: ByteArray, secret: ByteArray): ByteArray {
            var key = md5(secret + salt)
            var currentKey = key
            while (currentKey.size < 48) {
                key = md5(key + secret + salt)
                currentKey += key
            }
            return currentKey
        }

        private fun decryptSourceUrl(decryptionKey: ByteArray, sourceUrl: String): String {
            val cipherData = Base64.decode(sourceUrl, Base64.DEFAULT)
            val encrypted = cipherData.copyOfRange(16, cipherData.size)
            val aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding")

            Objects.requireNonNull(aesCBC).init(
                Cipher.DECRYPT_MODE, SecretKeySpec(
                    decryptionKey.copyOfRange(0, 32),
                    "AES"
                ),
                IvParameterSpec(decryptionKey.copyOfRange(32, decryptionKey.size))
            )
            val decryptedData = aesCBC!!.doFinal(encrypted)
            return String(decryptedData, StandardCharsets.UTF_8)
        }

        private inline fun <reified T> decryptMapped(input: String, key: String): T? {
            return Mapper.parse(decrypt(input, key))
        }

        private fun decrypt(input: String, key: String): String {
            return decryptSourceUrl(
                generateKey(
                    Base64.decode(input, Base64.DEFAULT).copyOfRange(8, 16),
                    key.toByteArray()
                ), input
            )
        }
    }


    @Serializable
    private data class SourceResponse(
        val encrypted: Boolean? = null,
        val sources: List<Track>? = null,
        val sourcesBackup: List<Track>? = null,
        val tracks: List<Track>? = null
    ) {
        @Serializable
        data class Encrypted (
            val sources: String? = null,
            val sourcesBackup: String? = null,
            val tracks: List<Track>? = null,
            val encrypted: Boolean? = null,
            val server: Long? = null
        )

        @Serializable
        data class Track (
            val file: String? = null,
            val label: String? = null,
            val kind: String? = null,
            val default: Boolean? = null
        )
    }

}
