package com.example.vc_android.helpers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.vc_android.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ImageHelperActivity extends AppCompatActivity {

    private static int REQUEST_PICK_IMAGE= 1000;
    private static int REQUEST_CAPTURE_IMAGE= 1001;
    private ImageView inputImageView;
    private TextView outputTextView;

    //ImageLaber class allow to analyze images and get labels from them, this is a library from "Firebase ML"
    private ImageLabeler imageLabeler;

    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_helper);

        //linking textview and imageview
        inputImageView = findViewById(R.id.imageViewInput);
        outputTextView = findViewById(R.id.textViewOutPut);

        imageLabeler = ImageLabeling.getClient(new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)//only assign a label if overcome this confidence
                .build());


        //Ask for persmission
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            //Check if permission have already give it
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(ImageHelperActivity.class.getSimpleName(),"grant result for "+permissions[0]+" is "+grantResults[0]);
    }

    public void onPickImage(View view){
        //allow the app to request another kind of content from other apps, it could be Gallery, GooglePhotos, OneDrive or another app
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //accept images with whatever extension as ".jpg",".png",etc
        intent.setType("image/*");

        startActivityForResult(intent,REQUEST_PICK_IMAGE);
    }


    //onActivityResult is used in order to get results of secondary activitys which was start from the current activity
    //this function is automatically called when the secondary activity is complet and return a result to the current activity.
    //requestCode:a request code which was used to start the secondary activity. It is used in order to indentify the secondary activity which was complete
    //resultCode: this value say if the activity was succeful complete
    //data : data which return from the secondary activity
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            //verify if this request is from request pick image
            if(requestCode==REQUEST_PICK_IMAGE){
                Uri uri = data.getData();
                Bitmap bitmap = loadFromUri(uri);
                inputImageView.setImageBitmap(bitmap);
                //clasify the image give it as bitmap
                runClassification(bitmap);
            }else if(requestCode==REQUEST_CAPTURE_IMAGE){
                Log.d("ML", "received callback from camera");
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                inputImageView.setImageBitmap(bitmap);
                //clasify the image give it as bitmap
                runClassification(bitmap);
            }
        }
    }

    public void onStarCamera(View view){
        //create a file to share with camera
        photoFile = createPhotoFile();
        Uri fileUri = FileProvider.getUriForFile(this,"com.example.fileprovider",photoFile);

        //create a intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);

        //startActivityForResult
        startActivityForResult(intent,REQUEST_CAPTURE_IMAGE);
    }

    public File createPhotoFile(){
        File photoFileDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"ML_IMAGE_HELPER");

        if(!photoFileDir.exists()){
            photoFileDir.mkdirs();
        }

        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(photoFileDir.getPath()+File.separator+name);
        return file;
    }

    //Get bitmap image from your URI
    private Bitmap loadFromUri(Uri uri){
        Bitmap bitmap=null;
        try{
            if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O_MR1){
                ImageDecoder.Source source=ImageDecoder.createSource(getContentResolver(),uri);
                bitmap=ImageDecoder.decodeBitmap(source);
            }else {
                bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return bitmap;
    }

    private void runClassification(Bitmap bitmap){
        InputImage inputImage = InputImage.fromBitmap(bitmap,0);
        //imageLabeler use machine learning in order to process inputImage get the labels, then pass the result to addOnSuccessListener
        imageLabeler.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {//List<ImageLabel> get the list of label clasificarion find out in the photo
            @Override
            public void onSuccess(@NonNull List<ImageLabel> imageLabels) {
                if(imageLabels.size() > 0){//verify that there is more than zero object labels
                    StringBuilder builder = new StringBuilder();//create a new String
                    //create and save the necesary String format in order to display in the screen
                    for(ImageLabel label: imageLabels){
                        builder.append(label.getText())
                                .append(" : ")
                                .append(label.getConfidence())
                                .append("\n");
                    }
                    outputTextView.setText(builder.toString());//send the text in the screen
                }else{
                    outputTextView.setText("Could not classify");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
}