package com.tudominio.asistente.device

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.provider.AlarmClock
import com.tudominio.asistente.core.router.AssistantAction
import com.tudominio.asistente.data.local.AppDatabase
import com.tudominio.asistente.data.local.UserFactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class ToolDispatcher(
    private val context: Context,
    private val db: AppDatabase
) {
    suspend fun execute(action: AssistantAction): String {
        return try {
            when (action) {
                is AssistantAction.SetAlarm -> setAlarm(action)
                is AssistantAction.OpenApp -> openApp(action.appName)
                is AssistantAction.SaveNote -> saveQuickNote(action.content)
            }
        } catch (e: Exception) {
            "No pude ejecutar la acción física: ${e.localizedMessage}"
        }
    }

    private fun setAlarm(action: AssistantAction.SetAlarm): String {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, action.label)
            putExtra(AlarmClock.EXTRA_HOUR, action.hour)
            putExtra(AlarmClock.EXTRA_MINUTES, action.minute)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            val formattedMinute = String.format(Locale.getDefault(), "%02d", action.minute)
            return "Alarma programada para las ${action.hour}:$formattedMinute (${action.label})."
        }
        return "No encontré una aplicación de Reloj compatible para programar la alarma."
    }

    private fun openApp(appName: String): String {
        val packageName = findPackageNameForApp(appName)
        if (packageName.isNullOrBlank()) {
            return "No encontré instalada ninguna aplicación llamada '$appName'."
        }
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            "Abriendo $appName..."
        } else {
            "Encontré '$appName', pero no tiene una pantalla principal para abrir."
        }
    }

    private suspend fun saveQuickNote(content: String): String = withContext(Dispatchers.IO) {
        db.userFactDao().upsert(
            UserFactEntity(
                factKey = "Nota_${System.currentTimeMillis()}",
                factValue = content,
                category = "NOTA_RAPIDA"
            )
        )
        "Nota guardada en tu memoria local: \"$content\""
    }

    private fun findPackageNameForApp(appName: String): String? {
        val query = appName.lowercase(Locale.getDefault()).trim()
        if (query.isEmpty()) return null
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val installedApps: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(launcherIntent, 0)
        }
        val labeledApps = installedApps.map { info ->
            val label = info.loadLabel(pm).toString().lowercase(Locale.getDefault()).trim()
            label to info.activityInfo.packageName
        }
        labeledApps.firstOrNull { (label, _) -> label == query }?.let { return it.second }
        labeledApps.filter { (label, _) -> label.contains(query) }
            .minByOrNull { (label, _) -> label.length }
            ?.let { return it.second }
        return when (query) {
            "correo", "mail" -> "com.google.android.gm"
            "navegador", "internet" -> "com.android.chrome"
            "mapas" -> "com.google.android.apps.maps"
            else -> null
        }
    }
}
