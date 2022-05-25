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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

public class DownloadFileService extends IntentService {
    private static final String TAG = DownloadFileService.class.getSimpleName();
    private static final String ACTION_DOWNLOAD_FILE = "com.example.lab4.download_file";
    private static final String NOTIFICATION_CHANNEL_ID = "com.example.lab4.notification_channel";
    private static final String NOTIFICATION_CHANNEL_NAME = "com.example.lab4.notification_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String URL_KEY = "com.example.lab4.url";
    private static final String BYTES_DOWNLOADED_KEY = "com.example.lab4.bytes_downloaded";
    private static final int BLOCK_SIZE = 1024;
    private static final int END_OF_FILE_CODE = -1;

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

    public DownloadFileService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mBytesDownloaded = 0;
        prepareNotificationManager();
        startForeground(NOTIFICATION_ID, createNotification());

        if (intent != null) {
            prepareForDownloadingFile(intent);
        }
    }

    private void prepareNotificationManager() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String appName = getString(R.string.app_name);
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, appName, NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private Notification createNotification() {
        PendingIntent pendingIntent = prepareNotificationIntents();
        return buildNotification(pendingIntent);
    }

    private PendingIntent prepareNotificationIntents() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra(BYTES_DOWNLOADED_KEY, mBytesDownloaded);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification buildNotification(PendingIntent pendingIntent) {
        Notification.Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder
                .setContentTitle("Downloading file")
                .setContentText(String.format(Locale.getDefault(),"%d bytes downloaded", mBytesDownloaded))
                .setContentIntent(pendingIntent)
                // TODO setProgress() may be implemented - it may be hard because of -1 as result of connection.getContentLength()
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(true); // TODO in lab it was checked whether the file hash already been downloaded

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
        }

        return notificationBuilder.build();
    }

    private void prepareForDownloadingFile(@NonNull Intent intent) {
        String action = intent.getAction();

        if (ACTION_DOWNLOAD_FILE.equals(action)) {
            String url = intent.getStringExtra(URL_KEY);
            processDownloadingFile(url);
            return;
        }
        throw new UnsupportedOperationException("Wrong action provided");
    }

    private void processDownloadingFile(String urlString) {
        try {
            URL url = new URL(urlString);
            InputStream reader = receiveReader(url);
            OutputStream writer = receiveWriter(url);

            downloadFile(reader, writer);
        } catch (IOException e) {
            handleFileDownloadException(e);
        }
    }

    @NonNull
    private InputStream receiveReader(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        return new DataInputStream(connection.getInputStream());
    }

    @NonNull
    private OutputStream receiveWriter(URL url) throws FileNotFoundException {
        File tempFile = new File(url.getFile());
        File outputFile = new File(getPathname(tempFile));

        if (outputFile.exists()) {
            deletePreviousFile(outputFile);
        }
        return new FileOutputStream(outputFile.getPath());
    }

    @NonNull
    private String getPathname(File fileName) {
        return Environment.getExternalStorageDirectory() + File.separator + fileName.getName();
    }

    private void deletePreviousFile(File outputFile) {
        boolean fileWasDeleted = outputFile.delete();
        if (fileWasDeleted) {
            Log.d(TAG, String.format(Locale.getDefault(), "Previous version of file %s was deleted", outputFile.getName()));
        }
    }

    private void downloadFile(InputStream reader, OutputStream writer) throws IOException {
        byte[] buffer = new byte[BLOCK_SIZE];
        int newBytesDownloaded = reader.read(buffer, 0, BLOCK_SIZE);

        while (newBytesDownloaded != END_OF_FILE_CODE) {
            writer.write(buffer, 0, newBytesDownloaded);
            mBytesDownloaded += newBytesDownloaded;
            newBytesDownloaded = reader.read(buffer, 0, BLOCK_SIZE);

            Log.d(TAG, String.format("Downloaded portion of %d bytes. Bytes downloaded: %d", newBytesDownloaded, mBytesDownloaded));
            mNotificationManager.notify(NOTIFICATION_ID, createNotification());
        }
    }

    private void handleFileDownloadException(IOException e) {
        Log.e(TAG, "Exception during file download");
        e.printStackTrace();
    }
}
