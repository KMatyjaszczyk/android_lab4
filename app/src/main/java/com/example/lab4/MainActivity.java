package com.example.lab4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

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
}