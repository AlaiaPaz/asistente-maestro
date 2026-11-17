package com.tudominio.asistente.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tudominio.asistente.data.local.AppDatabase

class DriveSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val unsyncedCount = db.userFactDao().countUnsynced()
        if (unsyncedCount == 0) {
            return Result.success()
        }

        val driveSync = DriveSyncManager(applicationContext)
        return when (driveSync.backupToDrive(db)) {
            is SyncResult.Success -> Result.success()
            is SyncResult.RequireAuth -> Result.Failure()
            is SyncResult.Error -> {
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
    }
}
