package com.partworm.namastebot

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat

class NBNotificationListenerService : NotificationListenerService() {

	var kakaotalk_version: Long = 0

	var bot: Bot? = null

	override fun onCreate() {
		super.onCreate()
		try {
			kakaotalk_version = PackageInfoCompat.getLongVersionCode(
				packageManager.getPackageInfo("com.kakao.talk", 0)
			)
		}
		catch (_: Exception) {
		}
		bot = Bot(this)
	}

	override fun onNotificationPosted(noti: StatusBarNotification) {
		super.onNotificationPosted(noti)
		val message = parse_message_from_notification(noti, kakaotalk_version) ?: return
		Log.d("namastebot", """
			room: ${message.room}
			sender: ${message.sender}
			content: ${message.content}
			isGroupChat: ${message.isGroupChat}
			packageName: ${message.packageName}
		""".trimIndent())
		val debug_sender = "FakeWorm"
		if (message.sender != debug_sender) {
			return
		}
		bot?.execute(message.action)
	}

}
