package com.partworm.namastebot

import android.app.Notification
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.text.Spanned
import androidx.core.text.HtmlCompat
import java.util.*

data class Message(
	val action: Notification.Action,
	val room: String,
	val sender: String,
	val content: String,
	val isGroupChat: Boolean,
	val packageName: String,
)

fun between(
	string: String,
	begin: String,
	end: String,
) = string.split(begin)[1].split(end)[0]

fun parse_message_from_notification(
	noti: StatusBarNotification,
	kakaotalk_version: Long,
): Message? {
	val w_ext = Notification.WearableExtender(noti.notification)
	for (action in w_ext.actions) {
		if (action.remoteInputs.isEmpty()) {
			continue
		}
		if (!action.title.toString().toLowerCase(Locale.KOREA).contains("reply") &&
			!action.title.toString().contains("답장")
		) {
			continue
		}
		val extras = noti.notification.extras
		var room: String?
		val sender: String
		val content: String
		var is_group_chat = false
		val package_name = noti.packageName
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			room = extras.getString("android.summaryText")
			sender = extras.get("android.title")!!.toString()
			content = extras.get("android.text")!!.toString()
			if (room == null) {
				room = sender
			} else {
				is_group_chat = true
			}
		} else {
			if (package_name != "com.kakao.talk" ||
				kakaotalk_version < 1907310
			) {
				room = extras.getString("android.title")!!
				val text = extras.get("android.text")
				if (text !is String) {
					val html = HtmlCompat.toHtml(
						text as Spanned,
						HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE,
					)
					sender = HtmlCompat.fromHtml(
						between(html, "<b>", "</b>"),
						HtmlCompat.FROM_HTML_MODE_COMPACT,
					).toString()
					content = HtmlCompat.fromHtml(
						between(html, "</b>", "</p>").substring(1),
						HtmlCompat.FROM_HTML_MODE_COMPACT,
					).toString()
				} else {
					sender = room
					content = extras.get("android.text")!!.toString()
				}
			} else {
				room = extras.getString("android.subText")
				sender = extras.getString("android.title")!!
				content = extras.getString("android.text")!!
				if (room == null) {
					room = sender
				} else {
					is_group_chat = true
				}
			}
		}
		/*
		if (!PictureManager.profileImage.containsKey(room)){
			PictureManager.profileImage[sender] =
				sbn.notification.getLargeIcon().toBitmap(context)
		}
		*/
		return Message(action, room, content, sender, is_group_chat, package_name)
	}
	return null
}

fun send_to_session(
	context: Context,
	session: Notification.Action,
	content: String,
) {
	val intent = Intent()
	val msg = Bundle()
	for (inputable in session.remoteInputs) {
		msg.putCharSequence(inputable.resultKey, content)
	}
	RemoteInput.addResultsToIntent(session.remoteInputs, intent, msg)
	session.actionIntent.send(context, 0, intent)
}
