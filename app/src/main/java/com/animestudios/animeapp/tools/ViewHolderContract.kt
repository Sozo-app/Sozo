package com.animestudios.animeapp.tools

interface ViewHolderContract<T> {
    fun bind(item: T, index: Int)
}