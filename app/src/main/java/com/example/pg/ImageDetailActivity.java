package com.example.pg;



import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageDetailActivity extends AppCompatActivity {

    ImageView imageDetail , backBtn;
    ProgressDialog mProgressDialog;
    OutputStream outputStream ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Download Image");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        backBtn = findViewById(R.id.backBtn);
        imageDetail = findViewById(R.id.imageDetail);


        Intent intent = getIntent();

        final String imageUrl = intent.getStringExtra("image_url");

        Picasso.get().load(imageUrl).into(imageDetail);

        imageDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BitmapDrawable drawable = (BitmapDrawable)imageDetail.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                File filePath = Environment.getExternalStorageDirectory();
                File dir = new File(filePath.getAbsolutePath()+"/Firebase App/");
                dir.mkdir();
                File file = new File(dir , System.currentTimeMillis()+".jpg");

                try {
                    outputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);
                Toast.makeText(ImageDetailActivity.this, "image save to internal gallery", Toast.LENGTH_SHORT).show();
                try {
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}