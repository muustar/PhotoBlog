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

public class LoginActivity extends AppCompatActivity {

    private EditText mLoginEmail, mLoginPasswd;
    private Button mLoginBtn, mLoginRegBtn;
    private ProgressBar mLoginProgressbar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginEmail = (EditText)findViewById(R.id.reg_email);
        mLoginPasswd = (EditText)findViewById(R.id.reg_passwd_again);
        mLoginBtn = (Button)findViewById(R.id.login_btn);
        mLoginRegBtn = (Button)findViewById(R.id.login_reg_btn);
        mLoginProgressbar = (ProgressBar)findViewById(R.id.login_progressbar);

        mAuth = FirebaseAuth.getInstance();


        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mLoginEmail.getText().toString().trim();
                String passw = mLoginPasswd.getText().toString().trim();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(passw)){
                 mLoginProgressbar.setVisibility(View.VISIBLE);

                 mAuth.signInWithEmailAndPassword(email,passw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task) {
                         if (task.isSuccessful()){
                             //sikeres belépés
                             mLoginProgressbar.setVisibility(View.INVISIBLE);
                             logIn();
                         }else{
                             //sikertelen
                             mLoginProgressbar.setVisibility(View.INVISIBLE);
                             //Toast.makeText(LoginActivity.this, "Sikertelen belépés", Toast.LENGTH_SHORT).show();
                             String errorMessage = task.getException().getMessage();
                             Toast.makeText(LoginActivity.this, "Hiba: "+errorMessage, Toast.LENGTH_SHORT).show();
                         }
                     }
                 });
                }
            }
        });



        mLoginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegActivity();
            }
        });



    }

    private void goToRegActivity() {
        Intent intentLogintoReg = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intentLogintoReg);

    }

    private void logIn() {
        Intent intentMain = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intentMain);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            // valaki be van lépve, küldjük a mainActivityre
            logIn();
        }
    }
}
