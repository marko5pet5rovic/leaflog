package com.markopetrovic.leaflog.services.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks.await
import com.markopetrovic.leaflog.R
import com.markopetrovic.leaflog.data.models.LocationBase
import com.markopetrovic.leaflog.data.repository.LocationRepository
import com.markopetrovic.leaflog.di.AppContainer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.seconds

private const val NOTIFICATION_CHANNEL_ID = "NEW_PLANTS_CHANNEL"
private const val FOREGROUND_NOTIFICATION_ID = 100
private const val RADIUS = 50f

class NotificationService : Service() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var locationIDs = mutableListOf<String>()

    private val locationRepository: LocationRepository by lazy {
        AppContainer.locationRepository
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification())

        startLocationMonitoring()

        return START_STICKY
    }

    @Throws(SecurityException::class)
    private suspend fun getLatestLocation(): Location? {
        val fusedLocationClient =LocationServices.getFusedLocationProviderClient(this)
        return fusedLocationClient.lastLocation.await()
    }
    private suspend fun getClosePlants(location: Location): List<LocationBase> {
        return locationRepository.getLocationsWithinRadius(
            currentLat = location.latitude,
            currentLon = location.longitude,
            radiusMeters = RADIUS
        ).first()
    }

    private fun startLocationMonitoring() {
        scope.launch {
            while (true) {
                try {
                    val location = getLatestLocation()!!
                    val fetchedLocations = getClosePlants(location)
                    val meantime = locationIDs.isNotEmpty()
                    Log.d(TAG, fetchedLocations.size.toString())

                    for (location in fetchedLocations) {
                        if (!locationIDs.contains(location.id)) {

                            locationIDs.add(location.id)
                            if (meantime) showNotification(location);
                        }
                    }
                } catch (e: Exception) { /* Handle security exception */ }
                delay(10.seconds)
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun buildForegroundNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("LeafLog notifikacioni servis aktivan")
            .setContentText("Aplikacija proverava nove biljke u pozadini.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "New plants",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(location: LocationBase) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(location.name)
            .setContentText(location.description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationId = location.id.hashCode()

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
}