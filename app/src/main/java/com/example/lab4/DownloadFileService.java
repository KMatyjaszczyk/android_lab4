package com.example.lab4;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class DownloadFileService extends IntentService {
    private static final String TAG = DownloadFileService.class.getSimpleName();
    private static final String ACTION_DOWNLOAD_FILE = "com.example.lab4.download_file";
    private static final String NOTIFICATION_CHANNEL_ID = "com.example.lab4.notification_channel";
    private static final String NOTIFICATION_CHANNEL_NAME = "com.example.lab4.notification_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String URL_KEY = "com.example.lab4.url";
    private static final String BYTES_DOWNLOADED_KEY = "com.example.lab4.bytes_downloaded";
    private static final int BLOCK_SIZE = 1024;

    private NotificationManager mNotificationManager;
    private int mBytesDownloaded = 0;

    public static void startService(Context context, String url) {
        Intent intent = new Intent(context, DownloadFileService.class);
        intent.setAction(ACTION_DOWNLOAD_FILE);
        intent.putExtra(URL_KEY, url);
        context.startService(intent);
    }

    public DownloadFileService() {
        super("DownloadFileService");
    }

    /**
     * @param name
     * @deprecated
     */
    public DownloadFileService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mBytesDownloaded = 0;
        prepareNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_DOWNLOAD_FILE.equals(action)) {
                String url = intent.getStringExtra(URL_KEY);
                processDownloadingFile(url);

            } else {
                throw new UnsupportedOperationException("Wrong action provided");
            }
        }
    }

    private void processDownloadingFile(String urlString) {
        FileOutputStream toFileStream = null;
        URLConnection connection = null;
        try {
            URL url = new URL(urlString);
            File tempFile = new File(url.getFile());
            File outputFile = new File(getFilePath(tempFile.getName()));
            if (outputFile.exists()) {
                outputFile.delete();
            }

            InputStream fromWebStream;
            connection = (HttpsURLConnection) url.openConnection();

            DataInputStream reader = new DataInputStream(connection.getInputStream());
            toFileStream = new FileOutputStream(outputFile.getPath());

            byte[] buffer = new byte[BLOCK_SIZE];
            int bytesDownloaded = reader.read(buffer, 0, BLOCK_SIZE);
            while (bytesDownloaded != -1) {
                toFileStream.write(buffer, 0, bytesDownloaded);
                mBytesDownloaded += bytesDownloaded;
                bytesDownloaded = reader.read(buffer, 0, BLOCK_SIZE);
                Log.d(TAG, String.format("Downloaded portion of %d bytes. Bytes downloaded: %d", bytesDownloaded, mBytesDownloaded));
                mNotificationManager.notify(NOTIFICATION_ID, createNotification());
            }

        } catch (IOException e) {
            Log.e(TAG, "Exception during file download");
            e.printStackTrace();
        }


    }

    private String getFilePath(String fileName) {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File downloadsDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File newFile = new File(downloadsDirectory, fileName);
        return newFile.getPath();
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra(BYTES_DOWNLOADED_KEY, mBytesDownloaded);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder
                .setContentTitle("Downloading file")
                .setContentText(String.format(Locale.getDefault(),"%d bytes downloaded", mBytesDownloaded))
                .setContentIntent(pendingIntent)
                // TODO setProgress() may be implemented
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(true); // TODO in lab it was checked whether the file hash already been downloaded

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
        }

        return notificationBuilder.build();
    }

    private void prepareNotificationChannel() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String appName = getString(R.string.app_name);
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    appName, NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
