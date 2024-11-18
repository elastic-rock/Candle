package com.elasticrock.candle

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.service.quicksettings.TileService
import android.util.Log

class QSTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile.label = getString(R.string.candle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Ensure proper handling after update to 1.7.1
            qsTile.activityLaunchForClick = null
        }
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(applicationContext, 1, intent, FLAG_IMMUTABLE + FLAG_UPDATE_CURRENT)
            startActivityAndCollapse(pendingIntent)
        } else if (isLocked) {
            startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
        } else {
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(intent)
        }
    }
}