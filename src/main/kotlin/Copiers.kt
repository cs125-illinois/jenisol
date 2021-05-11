package edu.illinois.cs.cs125.jenisol.core

import com.rits.cloning.Cloner

inline fun <reified T> T.deepCopy(): T {
    return when {
        this == null -> null as T
        else -> Cloner.shared().deepClone(this)
    }
}