package com.sozo.nicehttp

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress

fun OkHttpClient.Builder.addGenericDns(url: String, ips: List<String>) = dns(
    DnsOverHttps
        .Builder()
        .client(build())
        .url(url.toHttpUrl())
        .bootstrapDnsHosts(
            ips.map { InetAddress.getByName(it) }
        )
        .build()
)