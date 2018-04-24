package com.example.feco.photoblog;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

public class SearchActivity extends AppCompatActivity {

    private EditText mSearchFiled;
    private ImageView mSearchBtn;
    private RecyclerView mSearchResultRecyclerV;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter<Users, UsersViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchBtn = (ImageView) findViewById(R.id.search_image_btn);
        mSearchFiled = (EditText) findViewById(R.id.search_field);
        mSearchResultRecyclerV = (RecyclerView) findViewById(R.id.search_recyclerV);
        mSearchResultRecyclerV.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mSearchResultRecyclerV.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = mSearchFiled.getText().toString();
                firebaseUserSearch(searchText);

            }
        });

        mSearchFiled.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //if (!TextUtils.isEmpty(s)) {
                    firebaseUserSearch("" + s);
                //}
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void firebaseUserSearch(String searchText) {
        Query query = firebaseFirestore.collection("Users")
                .orderBy("name")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff"); //nem tudom miez

        FirestoreRecyclerOptions<Users> response = new FirestoreRecyclerOptions.Builder<Users>()
                .setQuery(query, Users.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Users, UsersViewHolder>(response) {
            @Override
            protected void onBindViewHolder(UsersViewHolder holder, int position, Users model) {
                holder.setDetails(
                        getApplicationContext(),
                        model.getImage_thumb(),
                        model.getName(),
                        model.getStatus()
                );
            }


            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.search_list_layout, parent, false);
                return new UsersViewHolder(v);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
            }
        };


        adapter.notifyDataSetChanged();
        mSearchResultRecyclerV.setAdapter(adapter);
        adapter.startListening();


    }


    @Override
    protected void onStop() {
        super.onStop();
        try {
            adapter.stopListening();
        } catch (Exception e) {

        }
    }

    // View Holder Class

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDetails(Context ctx, String img, String username, String status) {
            ImageView mImage = (ImageView) mView.findViewById(R.id.searchlist_profilimg);
            TextView mUsername = (TextView) mView.findViewById(R.id.searchlist_username);
            TextView mStatus = (TextView) mView.findViewById(R.id.searchlist_status);

            Glide.with(ctx).load(img).into(mImage);
            mUsername.setText(username);
            mStatus.setText(status);
        }
    }
}
