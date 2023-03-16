package com.example.vc_android.image;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.example.vc_android.helpers.ImageHelperActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;

public class ImageClassificationActivity extends ImageHelperActivity {

    //ImageLaber class allow to analyze images and get labels from them, this is a library from "Firebase ML"
    private ImageLabeler imageLabeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageLabeler = ImageLabeling.getClient(new ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)//only assign a label if overcome this confidence
        .build());
    }

    @Override
    protected void runClassification(final Bitmap bitmap) {
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
                    getOutputTextView().setText(builder.toString());//send the text in the screen
                }else{
                    getOutputTextView().setText("Could not classify");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}
