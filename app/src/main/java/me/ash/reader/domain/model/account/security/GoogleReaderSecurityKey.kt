package me.ash.reader.domain.model.account.security

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("google-reader-security-key")
class GoogleReaderSecurityKey private constructor() : SecurityKey() {

    var serverUrl: String? = null
    var username: String? = null
    var password: String? = null
    var clientCertificateAlias: String? = null

    constructor(serverUrl: String?, username: String?, password: String?, clientCertificateAlias: String?) : this() {
        this.serverUrl = serverUrl
        this.username = username
        this.password = password
        this.clientCertificateAlias = clientCertificateAlias
    }

    constructor(value: String? = DESUtils.empty) : this() {
        decode<GoogleReaderSecurityKey>(value).let {
            serverUrl = it.serverUrl
            username = it.username
            password = it.password
            clientCertificateAlias = it.clientCertificateAlias
        }
    }
}
