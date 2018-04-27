package com.example.feco.photoblog;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {
    private static final int MAX_LENGTH_FOR_RANDOM_STRING = 20;
    private static final int REQ_CODE_FOR_GALLERY = 10;
    private Toolbar mainToolbar;
    private ProgressBar mProgressbar;
    private ImageButton mImage;
    private EditText mTitle, mDescription;
    private Button mPostBtn;
    private Uri mImageUri = null;
    private String imageURL = null;
    private String imageThumbURL = null;

    //Firebase
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mAuth;
    private StorageReference mStorageReference;
    private String mUser_id = null;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        mainToolbar = (Toolbar) findViewById(R.id.newpost_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Új post hozzáadása");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        if (mAuth != null) {
            mUser_id = mAuth.getCurrentUser().getUid();
        }


        mProgressbar = (ProgressBar) findViewById(R.id.newpost_progressbar);
        mTitle = (EditText) findViewById(R.id.newpost_title);
        mDescription = (EditText) findViewById(R.id.newpost_desc);
        mImage = (ImageButton) findViewById(R.id.newpost_imgBtn);
        mPostBtn = (Button) findViewById(R.id.newpost_postbtn);

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // kép kiválasztásához az engedélyeket kezeljük
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(NewPostActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //Toast.makeText(NewPostActivity.this, "Nincs enegdély", Toast.LENGTH_SHORT).show();
                        myToast(NewPostActivity.this,"Nincs engedély");
                        ActivityCompat.requestPermissions(NewPostActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {
                        //Toast.makeText(SetupActivity.this, "Engedély ok", Toast.LENGTH_SHORT).show();
                        //elindítjuk a kép kivágót

                        BringImagePicker();

                    }

                } else {
                    BringImagePicker();
                }
            }
        });

        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = mTitle.getText().toString().trim();
                final String desc = mDescription.getText().toString().trim();


                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(desc) && mImageUri != null) {
                    mProgressbar.setVisibility(View.VISIBLE);
                    mPostBtn.setEnabled(false);
                    // ha minden ki van töltve akkor feltöltjük a képet, hogy megkaphassuk az URL-jét

                    //final String randomName = randomString();
                    final String randomName = UUID.randomUUID().toString();
                    Log.d("FECO", randomName);
                    StorageReference filePath = mStorageReference.child("Post_images").child(randomName + ".jpg");
                    filePath.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                imageURL = task.getResult().getDownloadUrl().toString();

                                // ---- ebben a szakaszban készítjük el és töltjük fel a thumpnailt
                                File newImageFile = new File(mImageUri.getPath());

                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)  // ezt valójában nem is használjuk
                                            //.setMaxWidth(200)
                                            //.setMaxHeight(200)
                                            //.setQuality(10)
                                            .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 20, baos); // saját beállítás 20
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTaskThumb = mStorageReference.child("Post_images/thumbs")
                                        .child(randomName + ".jpg")
                                        .putBytes(thumbData);

                                uploadTaskThumb.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        imageThumbURL = taskSnapshot.getDownloadUrl().toString();

                                        //String title, String desc, String image, String thumb, String uid, Date timestamp
                                        Map<String, Object> post = new HashMap<>();
                                        post.put("title", title);
                                        post.put("desc", desc);
                                        post.put("image", imageURL);
                                        post.put("thumb", imageThumbURL);
                                        post.put("uid", mUser_id);
                                        post.put("timestamp", FieldValue.serverTimestamp());

                                        /*BlogPost blogPost = new BlogPost();
                                        blogPost.setTitle(title);
                                        blogPost.setDesc(desc);
                                        blogPost.setImage(imageURL);
                                        blogPost.setThumb(imageThumbURL);
                                        blogPost.setUid(mUser_id);
                                        blogPost.setTimestamp(FieldValue.serverTimestamp());*/

                                        mFirebaseFirestore.collection("Posts").add(post).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                mProgressbar.setVisibility(View.INVISIBLE);
                                                mPostBtn.setEnabled(true);
                                                goToMain();
                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(NewPostActivity.this, "Hiba a feltöltés során", Toast.LENGTH_SHORT).show();
                                        Log.d("FECO", "hiba a thumb feltöltése során");
                                    }
                                });

                                // ---- ebben a szakaszban készítjük el és töltjük fel a thumpnailt -- VÉGE


                            }
                        }
                    });

                }

            }
        });

    }

    private void goToMain() {
        Intent goToMain = new Intent(NewPostActivity.this, MainActivity.class);
        startActivity(goToMain);
        finish();
    }

    private void BringImagePicker() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQ_CODE_FOR_GALLERY);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_FOR_GALLERY && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                    .setInitialCropWindowPaddingRatio(0)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mImage.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

            }
        }
    }

    public void myToast(Context ctx, String showText) {
        //custom toast
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_container));
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(showText);
        Toast toast = new Toast(ctx);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
