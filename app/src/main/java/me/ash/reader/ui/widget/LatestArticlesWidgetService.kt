package me.ash.reader.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService

class LatestArticlesWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory? {
        requireNotNull(intent) { "onGetViewFactory() received a null intent. This should never happen." }
        return LatestArticleWidgetRemoteViewsFactory(applicationContext, intent)
    }
}