package com.example.myapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.squareup.picasso.Picasso;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddIngredient extends AppCompatActivity {
    EditText Name;
    EditText PicUrl;
    Button BtnAddIngredient;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Button btnSelectPhoto;
    ImageView viewImage;
    final int bmpHeight = 160;
    final int bmpWidth = 160;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static  final int GALLERY_CODE = 0;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);
        InitViews();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Ingredients");
        BtnAddIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) viewImage.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                myRef.push().setValue(new Ingredient(Name.getText().toString(), uri.getPath()));
            }
        });
    }
    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(AddIngredient.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")){
                    Intent CameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if(CameraIntent.resolveActivity(getPackageManager()) != null){
                        startActivityForResult(CameraIntent, REQUEST_IMAGE_CAPTURE);
                    }

                }else if (options[item].equals("Choose from Gallery")){
                    Log.i("GalleryCode","g");
                    Intent GalleryIntent = null;
                    GalleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    GalleryIntent.setType("image/*");
                    GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(GalleryIntent,GALLERY_CODE);
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            switch(requestCode){
                case 1:
                    Log.i("CameraCode",""+REQUEST_IMAGE_CAPTURE);
                    Bundle bundle = data.getExtras();
                    Bitmap bmp = (Bitmap) bundle.get("data");
                    Bitmap resized = Bitmap.createScaledBitmap(bmp, bmpWidth,bmpHeight, true);
                    uri = getImageUri(getApplicationContext(), resized);
                    viewImage.setImageBitmap(resized);

                case 0:
                    Log.i("GalleryCode",""+requestCode);
                    Uri ImageURI = data.getData();
                    viewImage.setImageURI(ImageURI);
                    uri = ImageURI;
            }


        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    private void InitViews() {
        Name = findViewById(R.id.IngredientName);
        BtnAddIngredient = findViewById(R.id.AddIngredientBTN);
        mAuth = FirebaseAuth.getInstance();
        btnSelectPhoto = findViewById(R.id.SelectPhoto);
        viewImage = findViewById(R.id.viewImage);
        btnSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }
}