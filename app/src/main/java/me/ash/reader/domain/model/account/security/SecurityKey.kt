package me.ash.reader.domain.model.account.security

import kotlinx.serialization.*
import kotlinx.serialization.json.*


@Serializable
sealed class SecurityKey {

    inline fun <reified T> decode(value: String?): T {
        val decrypted = DESUtils.decrypt(value?.ifEmpty { DESUtils.empty } ?: DESUtils.empty)
        return Json.decodeFromString<T>(decrypted)
    }

    override fun toString(): String {
        val json = Json.encodeToString(serializer(), this)
        return DESUtils.encrypt(json)
    }

    override fun equals(other: Any?): Boolean {
        return this.toString() == other.toString()
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
