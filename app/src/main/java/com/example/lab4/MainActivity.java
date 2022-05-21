package com.example.lab4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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