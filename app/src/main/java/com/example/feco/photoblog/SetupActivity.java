package com.example.feco.photoblog;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.roger.catloadinglibrary.CatLoadingView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {
    public CatLoadingView mCatView;
    private int REQ_CODE_FOR_GALLERY = 10;
    private Toolbar setupToolbar;
    private CircleImageView setupImage;
    private EditText mSetupName;
    private EditText mStatusText;
    private Button mSetupBtn;
    private String mOriginUsername, mStatus;
    //private ProgressBar mProgressbar, mPrbarCircle;
    private Uri mImageUri = null;
    private String mImgThumbURL;
    private Boolean isChanged = false; // akkor igaz ha új profil kép lett választva, különben hamis, mert felesleges feltölteni ujra a profilképet amikor az nem változott.

    //firebase
    private FirebaseAuth mAuth;
    private StorageReference mStorageReference;
    private FirebaseFirestore mFirebaseFirestore;
    private String user_id;
    private Uri mUjImageUri;
    private ImageView mImgBlue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mCatView = new CatLoadingView();

        mAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        if (mAuth != null) {
            user_id = mAuth.getCurrentUser().getUid();
        } else {
            goToMain();
        }

        setupToolbar = (Toolbar) findViewById(R.id.setup_toolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Photo Blog");
        mSetupName = (EditText) findViewById(R.id.setup_name);
        mSetupBtn = (Button) findViewById(R.id.setup_btn);
        mStatusText = (EditText) findViewById(R.id.setup_status_text);
        mStatus = "";
        setupImage = findViewById(R.id.setup_image);

        // ha már van adat akkor betöltjük
        //mPrbarCircle.setVisibility(View.VISIBLE);
        //mImgBlue.setVisibility(View.VISIBLE);
        mCatView.show(getSupportFragmentManager(), "");
        mFirebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {
                        // tehát van már user adat beállítva, akkor töltsük be a felületre
                        mOriginUsername = task.getResult().getString("name");
                        String img_url = task.getResult().getString("image");
                        mImageUri = Uri.parse(img_url);
                        mImgThumbURL = task.getResult().getString("image_thumb");
                        mStatus = task.getResult().getString("status");

                        //amíg a kép betöltődik ne legyen üres a tér ezért használunk egy Placeholdert
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.mipmap.default_image);
                        Glide.with(SetupActivity.this)
                                .load(mImgThumbURL)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        //mPrbarCircle.setVisibility(View.INVISIBLE);
                                        //mImgBlue.setVisibility(View.INVISIBLE);
                                        mCatView.dismiss();
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        //mPrbarCircle.setVisibility(View.INVISIBLE);
                                        //mImgBlue.setVisibility(View.INVISIBLE);
                                        mCatView.dismiss();
                                        return false;
                                    }
                                })
                                .into(setupImage);


                        mSetupName.setText(mOriginUsername);
                        mStatusText.setText(mStatus);

                    } else {
                        //mPrbarCircle.setVisibility(View.INVISIBLE);
                        //mImgBlue.setVisibility(View.INVISIBLE);
                        mCatView.dismiss();
                    }
                } else {
                    //mPrbarCircle.setVisibility(View.INVISIBLE);
                    //mImgBlue.setVisibility(View.INVISIBLE);
                    mCatView.dismiss();
                    Toast.makeText(SetupActivity.this, "Hiba: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // kép kiválasztásához az engedélyeket kezeljük
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(SetupActivity.this, "Nincs enegdély", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

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

        mSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String profilname = mSetupName.getText().toString().trim();
                final String status = mStatusText.getText().toString().trim();
                if (!TextUtils.isEmpty(profilname) && mImageUri != null) {
                    //feltöltés
                    //Toast.makeText(SetupActivity.this, "feltöltés", Toast.LENGTH_SHORT).show();
                    //mProgressbar.setVisibility(View.VISIBLE);
                    mCatView.show(getSupportFragmentManager(), "");

                    storeFirestore(profilname, status);


                }
            }
        });


    }

    private void storeFirestore(final String profilname, final String status) {

        if (isChanged) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap compressedImageFile = null;
            try {
                compressedImageFile = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImageUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 10, baos); // saját beállítás 10
            byte[] ujImage = baos.toByteArray();

            UploadTask uploadTask = mStorageReference.child("Profile_images/thumbs").child(user_id + ".jpg").putBytes(ujImage);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        mImgThumbURL = task.getResult().getDownloadUrl().toString();

                        // feltöltöttük a thumb-öt és mostmár mehet többi

                        StorageReference file_path = mStorageReference.child("Profile_images").child(user_id + ".jpg");
                        file_path.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            //uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    //mProgressbar.setVisibility(View.INVISIBLE);
                                    String img_download_url = task.getResult().getDownloadUrl().toString();
                                    Map<String, String> usermap = new HashMap<>();
                                    usermap.put("name", profilname);
                                    usermap.put("image", img_download_url);
                                    usermap.put("image_thumb", mImgThumbURL);
                                    usermap.put("status", status);


                                    mFirebaseFirestore.collection("Users").document(user_id).set(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                isChanged = false;

                                                // notification

                                                NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                                Notification notify=new Notification.Builder
                                                        (getApplicationContext()).setContentTitle("Teszt").setContentText("Setup befejeződött").
                                                        setContentTitle("subject").setSmallIcon(R.drawable.cat).build();

                                                notify.flags |= Notification.FLAG_AUTO_CANCEL;
                                                notif.notify(0, notify);



                                                goToMain();
                                            } else {
                                                Toast.makeText(SetupActivity.this, "Hiba: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });


                                    //goToMain();
                                    //feltöltés kész, megyünk vissza a főlapra
                                } else {
                                    //hiba
                                    Toast.makeText(SetupActivity.this, "Hiba: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    //mProgressbar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }
            });


        } else {
            if (!TextUtils.equals(profilname, mOriginUsername) || !TextUtils.equals(status, mStatus)) {
                Map<String, String> usermap = new HashMap<>();
                usermap.put("name", profilname);
                usermap.put("image", mImageUri.toString());
                usermap.put("image_thumb", mImgThumbURL);
                usermap.put("status", status);
                mFirebaseFirestore.collection("Users").document(user_id).set(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            isChanged = false;
                            goToMain();
                        } else {
                            Toast.makeText(SetupActivity.this, "Hiba: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                mCatView.dismiss();
                goToMain();
            }
        }
    }

    private void BringImagePicker() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQ_CODE_FOR_GALLERY);

    }

    private void goToMain() {
        Intent gotoMain = new Intent(SetupActivity.this, MainActivity.class);
        startActivity(gotoMain);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_FOR_GALLERY && resultCode == RESULT_OK) {
            mUjImageUri = data.getData();
            CropImage.activity(mUjImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(SetupActivity.this);
        } else {
            isChanged = false;
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                setupImage.setImageURI(mImageUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                isChanged = false;
            }
        } else {
            isChanged = false;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToMain();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setup_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_deleteuser) {
            goToDeleteProfile();
            return true;
        }
        return false;
    }

    private void goToDeleteProfile() {
        Intent goToDelete = new Intent(SetupActivity.this, DeleteProfileActivity.class);
        startActivity(goToDelete);
        finish();
    }


}
