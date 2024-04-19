package com.explyt.ton.intellij.model

enum class TonFileType {
    FIF, FC, TACT;

    companion object {
        fun fromExtension(extension: String): TonFileType? = when (extension) {
            in setOf("fif", ".fif") -> FIF
            in setOf("fc", ".fc") -> FC
            in setOf("tact", ".tact") -> TACT
            else -> null
        }
    }
}
