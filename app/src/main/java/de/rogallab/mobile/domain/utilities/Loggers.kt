package de.rogallab.mobile.domain.utilities

// Logger as function type
typealias LogFunction = (tag: String, message: String) -> Unit

// Default-Implementierungen: no-op oder println
internal var errorLogger: LogFunction = { _, _ -> /* no-op */ }
internal var warningLogger: LogFunction = { _, _ -> /* no-op */ }
internal var infoLogger: LogFunction = { _, _ -> /* no-op */ }
internal var debugLogger: LogFunction = { _, _ -> /* no-op */ }
internal var verboseLogger: LogFunction = { _, _ -> /* no-op */ }
internal var compLogger: LogFunction = { _, _ -> /* no-op */ }

object LogConfig {
   var isInfo: Boolean = false
   var isDebug: Boolean = false
   var isVerbose: Boolean = false
   var isComp: Boolean = false
}

internal fun formatMessage(message: String) =
   String.format("%-110s %s", message, Thread.currentThread().toString())

// Öffentliche Funktionen, die überall genutzt werden dürfen:
fun logError(tag: String, message: String) =
   errorLogger(tag, formatMessage(message))

fun logWarning(tag: String, message: String) =
   warningLogger(tag, formatMessage(message))

fun logInfo(tag: String, message: String) {
   if (LogConfig.isInfo) infoLogger(tag, formatMessage(message))
}

fun logDebug(tag: String, message: String) {
   if (LogConfig.isDebug) debugLogger(tag, formatMessage(message))
}

fun logVerbose(tag: String, message: String) {
   if (LogConfig.isVerbose) verboseLogger(tag, message)
}

fun logComp(tag: String, message: String) {
   if (LogConfig.isComp) compLogger(tag, message)
}
