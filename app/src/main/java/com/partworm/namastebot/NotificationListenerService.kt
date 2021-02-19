package com.partworm.namastebot

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.content.pm.PackageInfoCompat

class NotificationListenerService : NotificationListenerService() {

	var kakaotalk_version: Long = 0

	override fun onCreate() {
		super.onCreate()
		try {
			kakaotalk_version = PackageInfoCompat.getLongVersionCode(
				packageManager.getPackageInfo("com.kakao.talk", 0)
			)
		} catch (_: Exception) {
		}
	}

	override fun onNotificationPosted(noti: StatusBarNotification) {
		super.onNotificationPosted(noti)
		val message = parse_message_from_notification(noti, kakaotalk_version) ?: return
	}

}
