package me.ash.reader.domain.model.account.security

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("local-security-key")
class LocalSecurityKey private constructor() : SecurityKey() {

    constructor(value: String? = DESUtils.empty) : this() {
        decode<LocalSecurityKey>(value).let {

        }
    }
}
