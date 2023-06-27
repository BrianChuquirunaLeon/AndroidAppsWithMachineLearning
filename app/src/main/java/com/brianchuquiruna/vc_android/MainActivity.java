package com.brianchuquiruna.vc_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.brianchuquiruna.vc_android.R;
import com.brianchuquiruna.vc_android.image.LeafDiseaseIdentificationActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onGoToLeafDiseaseIdentification(View view){
        //start the image helper activity
        Intent intent = new Intent(this, LeafDiseaseIdentificationActivity.class);
        startActivity(intent);
    }
}