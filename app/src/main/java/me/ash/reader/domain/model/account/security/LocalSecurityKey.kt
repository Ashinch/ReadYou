package me.ash.reader.domain.model.account.security

class LocalSecurityKey private constructor() : SecurityKey() {

    constructor(value: String? = DESUtils.empty) : this() {
        decode<LocalSecurityKey>(value).let {

        }
    }
}
