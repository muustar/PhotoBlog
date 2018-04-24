package com.example.feco.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteProfileActivity extends AppCompatActivity {
    private Toolbar deleteToolbar;
    private EditText mDeleteEmail, mDeletePasswd;
    private Button mDeleteSubmitBtn;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private StorageReference mStorageReference;
    private FirebaseFirestore mFirebaseFirestore;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        mAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        user_id = mAuth.getCurrentUser().getUid();

        deleteToolbar = (Toolbar) findViewById(R.id.delete_toolbar);
        setSupportActionBar(deleteToolbar);
        getSupportActionBar().setTitle("Fiók törlése");

        mDeleteEmail = (EditText) findViewById(R.id.delete_email);
        mDeletePasswd = (EditText) findViewById(R.id.delete_passwd);
        mDeleteSubmitBtn = (Button) findViewById(R.id.delete_btn);
        mProgressBar = (ProgressBar) findViewById(R.id.delete_progressbar);


        mDeleteSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mDeleteEmail.getText().toString().trim();
                String passwd = mDeletePasswd.getText().toString().trim();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(email, passwd);

                // Prompt the user to re-provide their sign-in credentials
                mAuth.getCurrentUser().reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    felhasznaloiProfilTorlese();
                                }
                            }
                        });
            }
        });

    }


    private void felhasznaloiProfilTorlese() {
        try {
            StorageReference profilkep_utvonal = mStorageReference.child("Profile_images").child(user_id + ".jpg");
            profilkep_utvonal.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("FECO", "fénykép törölve");
                    } else {
                        Log.d("FECO", "fénykép már törölve volt");
                    }
                }
            });
            mFirebaseFirestore.collection("Users").document(user_id).delete();
            mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(DeleteProfileActivity.this, "Felhasználó törölve", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        goToLogin();
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(DeleteProfileActivity.this, "Hiba: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void goToLogin() {
        Intent goToLogin = new Intent(DeleteProfileActivity.this, LoginActivity.class);
        startActivity(goToLogin);
        finish();
    }
}
