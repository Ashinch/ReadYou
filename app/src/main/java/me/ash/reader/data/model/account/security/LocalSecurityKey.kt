package me.ash.reader.data.model.account.security

class LocalSecurityKey private constructor() : SecurityKey() {

    constructor(value: String? = DESUtils.empty) : this() {
        decode(value, LocalSecurityKey::class.java).let {

        }
    }
}
