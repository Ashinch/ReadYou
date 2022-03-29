package me.ash.reader.data.source

import android.content.Context
import android.util.Log
import android.util.Xml
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.*
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.group.Group
import me.ash.reader.data.group.GroupWithFeed
import me.ash.reader.data.repository.StringsRepository
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.*
import javax.inject.Inject

class OpmlLocalDataSource @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val stringsRepository: StringsRepository,
) {
    fun getDefaultGroupId(): String {
        val readYouString = stringsRepository.getString(R.string.read_you)
        val defaultString = stringsRepository.getString(R.string.defaults)
        return context.dataStore
            .get(DataStoreKeys.CurrentAccountId)!!
            .spacerDollar(readYouString + defaultString)
    }

    //    @Throws(XmlPullParserException::class, IOException::class)
    fun parseFileInputStream(inputStream: InputStream, defaultGroup: Group): List<GroupWithFeed> {
        val groupWithFeedList = mutableListOf<GroupWithFeed>()
        val accountId = context.currentAccountId
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                if (parser.name != "outline") {
                    continue
                }
                if ("rss" == parser.getAttributeValue(null, "type")) {
                    val title = parser.getAttributeValue(null, "title")
                    val xmlUrl = parser.getAttributeValue(null, "xmlUrl")
                    Log.i("RLog", "rss: ${title} , ${xmlUrl}")
                    if (groupWithFeedList.isEmpty()) {
                        groupWithFeedList.add(
                            GroupWithFeed(
                                group = defaultGroup,
                                feeds = mutableListOf()
                            )
                        )
                    }
                    groupWithFeedList.last().let { groupWithFeed ->
                        groupWithFeed.feeds.add(
                            Feed(
                                id = UUID.randomUUID().toString(),
                                name = title,
                                url = xmlUrl,
                                groupId = groupWithFeed.group.id,
                                accountId = accountId,
                            )
                        )
                    }
                } else {
                    val title = parser.getAttributeValue(null, "title")
                    Log.i("RLog", "title: ${title}")
                    groupWithFeedList.add(
                        GroupWithFeed(
                            group = Group(
                                id = UUID.randomUUID().toString(),
                                name = title,
                                accountId = accountId,
                            ),
                            feeds = mutableListOf()
                        )
                    )
                }
            }
            return groupWithFeedList
        }
    }
}