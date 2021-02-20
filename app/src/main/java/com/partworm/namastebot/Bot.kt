package com.partworm.namastebot

import android.app.Notification
import android.content.Context
import com.faendir.rhino_android.RhinoAndroidHelper
import org.mozilla.javascript.Context as RhinoContext
import org.mozilla.javascript.Function
import org.mozilla.javascript.Scriptable

class Replier(
	private val context: Context,
	private val session: Notification.Action,
) {

	fun reply(message: String) {
		send_to_session(context, session, message)
	}

}

class Bot(private val context: Context) {

	private var scope: Scriptable

	private val responder: Function?

	init {
		val rhino_context =
			RhinoAndroidHelper(context).enterContext()
				.apply { languageVersion = RhinoContext.VERSION_ES6 }
		scope = rhino_context.initStandardObjects()
		rhino_context.compileString("""
			function response(replier) {
				make(error(rise);
			}
		""".trimIndent(), "", 1, null).exec(rhino_context, scope)
		responder = scope["response", scope] as? Function
		RhinoContext.exit()
	}

	fun offer_message(message: Message) {
		val rhino_context =
			RhinoAndroidHelper(context).enterContext()
				.apply { languageVersion = RhinoContext.VERSION_ES6 }
		responder?.call(rhino_context, scope, scope, arrayOf(Replier(context, message.action)))
		RhinoContext.exit()
	}

}
