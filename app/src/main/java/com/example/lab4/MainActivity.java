package com.example.lab4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 2137;

    private EditText mTextUrl;
    private Button mButtonReceiveInformation;
    private TextView mFileSizeValue;
    private TextView mFileTypeValue;
    private Button mButtonDownloadFile;
    private TextView mBytesDownloadedValue;
    private ProgressBar mBytesDownloadedProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectLayoutElementsWithFields();
        addListenerForReceivingFileInfo();
        addListenerForDownloadingFile();
    }

    private void connectLayoutElementsWithFields() {
        mTextUrl = findViewById(R.id.editTextUrl);
        mButtonReceiveInformation = findViewById(R.id.buttonReceiveInformation);
        mFileSizeValue = findViewById(R.id.textViewFileSizeValue);
        mFileTypeValue = findViewById(R.id.textViewFileTypeValue);
        mButtonDownloadFile = findViewById(R.id.buttonDownloadFile);
        mBytesDownloadedValue = findViewById(R.id.textViewBytesDownloadedValue);
        mBytesDownloadedProgress = findViewById(R.id.progressBarBytesDownloaded);
    }

    private void addListenerForReceivingFileInfo() {
        mButtonReceiveInformation.setOnClickListener(view -> {
            ReceiveFileInfoTask fileSizeTask = new ReceiveFileInfoTask();
            fileSizeTask.execute(mTextUrl.getText().toString());
        });
    }

    private void addListenerForDownloadingFile() {
        mButtonDownloadFile.setOnClickListener(this::processStartingDownloadFileService);
    }

    private void processStartingDownloadFileService(View view) {
        Log.d(TAG, "Prepare for starting download file service");
        boolean appHasWriteExternalStoragePermission =
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;

        if (appHasWriteExternalStoragePermission) {
            startDownloadFileService();
        } else {
            processAskingForPermission();
        }
    }

    private void processAskingForPermission() {
        boolean appShouldAskForWriteExternalStoragePermission = ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (appShouldAskForWriteExternalStoragePermission) {
            CharSequence message = getResources().getText(R.string.askForWriteExternalStoragePermissionMessage);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
        askForWriteExternalStoragePermission();
    }

    private void askForWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            startDownloadFileServiceIfPossible(permissions, grantResults);
            return;
        }
        throw new UnsupportedOperationException("Unknown request code...");
    }

    private void startDownloadFileServiceIfPossible(@NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean appHasWriteExternalStoragePermission = permissions.length > 0
                && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (appHasWriteExternalStoragePermission) {
            startDownloadFileService();
            return;
        }
        throw new UnsupportedOperationException("Permission not granted...");
    }

    private void startDownloadFileService() {
        DownloadFileService.startService(MainActivity.this, mTextUrl.getText().toString());
        Log.d(TAG, "Download file service started");
    }

    class ReceiveFileInfoTask extends AsyncTask<String, Void, FileInfo> {
        @Override
        protected FileInfo doInBackground(String... strings) {
            FileInfo result = null;
            URLConnection connection = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpsURLConnection) url.openConnection();
                return new FileInfo(connection.getContentLength(), connection.getContentType());
            } catch (MalformedURLException e) {
                result = handleException("URL error", "Bad URL provided to address field", e);
            }
            catch (SecurityException e) {
                result = handleException("Permission error", "Application doesn't have permission to connect with the Internet", e);
            } catch (IOException e) {
                result = handleException("Task error", "Cannot receive file size", e);
            }
            return result;
        }

        private FileInfo handleException(String messageTag, String message, Throwable exception) {
            Log.e(messageTag, message, exception);
            exception.printStackTrace();
            return new FileInfo(0, "none", true, message);
        }

        @Override
        protected void onPostExecute(FileInfo result) {
            super.onPostExecute(result);

            if (result.hasError()) {
                Toast.makeText(MainActivity.this, result.getErrorMessage(), Toast.LENGTH_LONG).show();
            }

            mFileSizeValue.setText(String.valueOf(result.getSize()));
            mFileTypeValue.setText(result.getType());
        }
    }
}