package me.ash.reader.data.entity

data class GitHubRelease(
    val html_url: String? = null,
    val tag_name: String? = null,
    val name: String? = null,
    val draft: Boolean? = null,
    val prerelease: Boolean? = null,
    val created_at: String? = null,
    val published_at: String? = null,
    val assets: List<AssetsItem>? = null,
    val body: String? = null,
)

data class AssetsItem(
    val name: String? = null,
    val content_type: String? = null,
    val size: Int? = null,
    val download_count: Int? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val browser_download_url: String? = null,
)