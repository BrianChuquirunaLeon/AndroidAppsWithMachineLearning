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
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageHelperActivity extends AppCompatActivity {

    private static int REQUEST_PICK_IMAGE= 1000;
    private static int REQUEST_CAPTURE_IMAGE= 1001;

    private ImageView inputImageView;
    private TextView outputTextView;
//    private File photoFile;
    private Uri cameraUri;
    private final String SAMPLE_CROPPED_IMG_NAME = "sampleCropImg.jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_helper);

        //linking textview and imageview
        inputImageView = findViewById(R.id.imageViewInput);
        outputTextView = findViewById(R.id.textViewOutPut);

        checkPermissions();

    }
    //Ask for persmission
    private void checkPermissions(){
        //External Storage permissions

        //Verify if device is a version over 24 API
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            //Check if permission have already give it
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //If we can't permissions then ask for it
                if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //If we can't permissions then ask for it
                    requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE}, 5);
                }else{
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 5);
                }
            }else{
                if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //If we can't permissions then ask for it
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 5);
                }
            }
        }

//        //Camera permissions
//        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            //If we can't permissions then ask for it
//            requestPermissions(new String[]{Manifest.permission.CAMERA}, 5);
//        }
    }

//onRequestPermissionsResult es un metodo del S.O. de android el cual podemos sobreescribir para que una ves hemos aceptado
// los permisos podemos hacer algo mas luego de ello.
//    @Override
//    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        Log.d(ImageHelperActivity.class.getSimpleName(),"grant result for "+permissions[0]+" is "+grantResults[0]);
//    }




    //onActivityResult is used in order to get results of secondary activitys which was start from the current activity
    //this function is automatically called when the secondary activity is complet and return a result to the current activity.
    //requestCode:a request code which was used to start the secondary activity. It is used in order to indentify the secondary activity which was complete
    //resultCode: this value say if the activity was succeful complete
    //data : data which return from the secondary activity
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Bitmap bitmap = null;
            //verify if this request is from request pick image
            if(requestCode==REQUEST_PICK_IMAGE){
                final Uri uri = data.getData();
                startCrop(uri);//crop image
            }else if(requestCode==REQUEST_CAPTURE_IMAGE){
                Log.i("ML", "received callback from camera");
//                bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                startCrop(cameraUri);//crop image
            }else if(requestCode== UCrop.REQUEST_CROP){
                final Uri imageUriResultCrop = UCrop.getOutput(data);
                if(imageUriResultCrop != null){
                    bitmap = loadFromUri(imageUriResultCrop);
                }
            }else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
            }

            Bitmap _argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap argbBitmap;
            //Si la imagen es del tamaño de entrada de la CNN no se hace nada
            if (_argbBitmap.getHeight()==256 && _argbBitmap.getWidth()==256){
                argbBitmap = _argbBitmap;
            }else{//Pero en caso la imagen no sea del mismo ancho y alto de la capa de entrada de la CNN entrenada, entonces hacemos un resize
                argbBitmap = Bitmap.createScaledBitmap(_argbBitmap,256,256,true);
            }
            inputImageView.setImageBitmap(argbBitmap);
            //clasify the image give it as bitmap
            runClassification(argbBitmap);

        }
    }

    public void onPickImage(View view){
        //allow the app to request another kind of content from other apps, it could be Gallery, GooglePhotos, OneDrive or another app
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //accept images with whatever extension as ".jpg",".png",etc
        intent.setType("image/*");

        startActivityForResult(intent,REQUEST_PICK_IMAGE);
    }

    public void onStarCamera(View view){
        //create a file to share with camera
        File photoFile = createPhotoFile();
        cameraUri = FileProvider.getUriForFile(this,"com.example.fileprovider",photoFile);

        //create a intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,cameraUri);

        //startActivityForResult
        startActivityForResult(intent,REQUEST_CAPTURE_IMAGE);
    }

    private void startCrop(@NonNull Uri uri){

        UCrop.of(uri, Uri.fromFile(new File(getCacheDir(),this.SAMPLE_CROPPED_IMG_NAME)))
                .withAspectRatio(1, 1)
                .withMaxResultSize(256, 256)
                .start(this);
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



    protected void runClassification(Bitmap bitmap){

    }

    protected TextView getOutputTextView(){
        return outputTextView;
    }

    protected ImageView getInputImageView(){
        return inputImageView;
    }

}