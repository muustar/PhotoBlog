package com.example.feco.photoblog;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.eyalbira.loadingdots.LoadingDots;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.roger.catloadinglibrary.CatLoadingView;

public class MainActivity extends AppCompatActivity {


    private Toolbar mainToolbar;
    private FloatingActionButton addPostBtn;
    private BottomNavigationView mainBottomNav;
    public static LoadingDots dot_load;

    // Firebase section
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private String mUser_id = null;

    //fragmentek
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    private static FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //firebase tartozékai
        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        dot_load = (LoadingDots) findViewById(R.id.dot_load);

        // felületi elemek inicializálása
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Photo Blog");

        addPostBtn = findViewById(R.id.add_post_btn);


        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToNewPost = new Intent(MainActivity.this, NewPostActivity.class);
                startActivity(goToNewPost);

            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mainBottomNav = (BottomNavigationView) findViewById(R.id.mainBottomNav);
            //fragmentek


            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();
            replaceFregmant(homeFragment);

            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.bottom_action_home:
                            replaceFregmant(homeFragment);

                            return true;
                        case R.id.bottom_action_notification:
                            replaceFregmant(notificationFragment);
                            return true;
                        case R.id.bottom_action_account:
                            replaceFregmant(accountFragment);
                            return true;
                    }

                    return false;
                }
            });
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {

            // User is signed in
            // nézzük meg h az adatlapot kitöltötte-e, ha nem akkor irányitsuk oda
            mUser_id = mAuth.getCurrentUser().getUid();
            mFirebaseFirestore.collection("Users").document(mUser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            goToSetup();
                        } else {

                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Hiba: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {

            goToLogin();
        }

    }

    private void goToSetup() {
        Intent goToSetup = new Intent(MainActivity.this, SetupActivity.class);
        startActivity(goToSetup);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_logout:
                logOut();
                return true;
            case R.id.action_settings:
                Intent goToSetup = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(goToSetup);
                finish();
                return true;
            case R.id.action_search:
                Intent goToSearch = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(goToSearch);
                return true;
            default:
                return false;

        }

    }

    private void logOut() {
        mAuth.signOut();
        mAuth = null;
        goToLogin();
    }

    private void goToLogin() {
        Intent mainToLogin = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(mainToLogin);
        finish();
    }

    public void replaceFregmant(Fragment fragment) {

        fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }
}