package com.example.vc_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.vc_android.helpers.ImageHelperActivity;
import com.example.vc_android.image.FlowerIdentificationActivity;
import com.example.vc_android.image.ImageClassificationActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onGoToImageActivity(View view){
        //start the image helper activity
        Intent intent = new Intent(this, ImageClassificationActivity.class);
        startActivity(intent);
    }

    public void onGoToFlowerIdentification(View view){
        //start the image helper activity
        Intent intent = new Intent(this, FlowerIdentificationActivity.class);
        startActivity(intent);
    }
}