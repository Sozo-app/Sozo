package com.sozo.nicehttp

import java.io.File


fun Map<String, String>.toNiceFiles(): List<NiceFile> =
    this.map {
        NiceFile(it.key, it.value)
    }

class NiceFile(val name: String, val fileName: String, val file: File?, val fileType: String?) {
    constructor(name: String, value: String) : this(name, value, null, null)
    constructor(name: String, file: File) : this(name, file.name, file, null)
    constructor(file: File) : this(file.name, file)
}