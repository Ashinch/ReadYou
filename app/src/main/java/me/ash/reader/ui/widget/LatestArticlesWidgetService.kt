package me.ash.reader.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService

class LatestArticlesWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory? {
        return LatestArticleWidgetRemoteViewsFactory(applicationContext)
    }
}