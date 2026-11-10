package dev.portfolio.checklist
import android.app.*
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
class ReminderWorker(context:Context,params:WorkerParameters):Worker(context,params){override fun doWork():Result{val manager=applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)as NotificationManager;manager.createNotificationChannel(NotificationChannel("tasks","Напоминания о задачах",NotificationManager.IMPORTANCE_DEFAULT));val title=inputData.getString("title")?:return Result.failure();val notification=NotificationCompat.Builder(applicationContext,"tasks").setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle("Пора заняться делом").setContentText(title).setAutoCancel(true).build();try{manager.notify(inputData.getLong("id",0).toInt(),notification)}catch(_:SecurityException){return Result.success()};return Result.success()}}
