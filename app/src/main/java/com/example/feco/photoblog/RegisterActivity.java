package com.example.feco.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class RegisterActivity extends AppCompatActivity {

    private EditText mReg_email, mReg_passwd, mReg_ConfirmPasswd;
    private Button mRegBtn, mRegAlreadyHaveAccBtn;
    private ProgressBar mProgressBar;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mReg_email = (EditText) findViewById(R.id.reg_email);
        mReg_passwd = (EditText) findViewById(R.id.reg_passwd);
        mReg_ConfirmPasswd = (EditText) findViewById(R.id.reg_passwd_again);
        mRegBtn = (Button) findViewById(R.id.reg_btn);
        mRegAlreadyHaveAccBtn = (Button) findViewById(R.id.reg_reg_btn);
        mProgressBar = (ProgressBar) findViewById(R.id.reg_progressbar);


        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mReg_email.getText().toString().trim();
                String passwd = mReg_passwd.getText().toString().trim();
                String passwd_confirm = mReg_ConfirmPasswd.getText().toString().trim();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(passwd) && !TextUtils.isEmpty(passwd_confirm)) {
                    if (TextUtils.equals(passwd, passwd_confirm)) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email, passwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //siekres regisztrálás
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    goRegToSetupProfile();
                                } else {
                                    String e = task.getException().getMessage();
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(RegisterActivity.this, "Hiba: " + e, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Nem egyezik meg a két jelszó", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mRegAlreadyHaveAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void goRegToSetupProfile() {
        Intent goSetup = new Intent(RegisterActivity.this, SetupActivity.class);
        startActivity(goSetup);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goRegToMain();
        }
    }

    private void goRegToMain() {
        // be van lépve valaki menjünk a main oldalra
        Intent intentMain = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intentMain);
        finish();
    }
}
