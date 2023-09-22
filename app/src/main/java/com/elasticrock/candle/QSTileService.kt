package com.elasticrock.candle

import android.app.PendingIntent
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.service.quicksettings.TileService

class QSTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile.label = getString(R.string.candle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val intent = Intent(this, MainActivity::class.java)
            qsTile.activityLaunchForClick = PendingIntent.getActivity(applicationContext, 1, intent, PendingIntent.FLAG_IMMUTABLE + PendingIntent.FLAG_UPDATE_CURRENT)
        }
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java)
        if (isLocked) {
            startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
        } else {
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(intent)
        }
    }
}