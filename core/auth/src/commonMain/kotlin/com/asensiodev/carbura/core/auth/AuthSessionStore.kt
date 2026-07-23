package com.asensiodev.carbura.core.auth

interface AuthSessionStore {
    suspend fun read(): ByteArray?

    suspend fun write(value: ByteArray)

    suspend fun clear()
}

class InMemoryAuthSessionStore : AuthSessionStore {
    private var value: ByteArray? = null

    override suspend fun read(): ByteArray? = value?.copyOf()

    override suspend fun write(value: ByteArray) {
        this.value?.fill(0)
        this.value = value.copyOf()
    }

    override suspend fun clear() {
        value?.fill(0)
        value = null
    }
}
