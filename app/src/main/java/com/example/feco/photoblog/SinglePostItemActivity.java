package com.example.feco.photoblog;


import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class SinglePostItemActivity extends AppCompatActivity {
    private String blogPostId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private ImageView mImage;
    private TextView mTitle, mDesc;
    private Button mDelButton;
    private String imgPath;
    private String thumbPath;
    private RecyclerView mCommentsRecycler;
    private List<Comments> comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post_item);

        blogPostId = getIntent().getStringExtra("blogPostId");
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.singlepost_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bejegyzés törlése");


        progressBar = (ProgressBar) findViewById(R.id.singlepost_progressbar);
        mImage = (ImageView) findViewById(R.id.singlepost_image);
        mTitle = (TextView) findViewById(R.id.singlepost_title);
        mDesc = (TextView) findViewById(R.id.singlepost_desc);
        mDelButton = (Button) findViewById(R.id.singlepost_postbtn);
        mCommentsRecycler = (RecyclerView)findViewById(R.id.singlepost_comments_recycler);



        firebaseFirestore.collection("Posts").document(blogPostId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try {
                    if (task.isSuccessful()) {
                        thumbPath = task.getResult().getString("thumb").toString();
                        imgPath = task.getResult().getString("image").toString();
                        //Toast.makeText(getApplicationContext(), "url: "+url, Toast.LENGTH_SHORT).show();
                        Glide.with(SinglePostItemActivity.this).load(thumbPath).into(mImage);
                        mTitle.setText(task.getResult().getString("title"));
                        mDesc.setText(task.getResult().getString("desc"));
                        String postUid = task.getResult().getString("uid");
                        if (TextUtils.equals(mAuth.getUid(),postUid)){
                            mDelButton.setEnabled(true);
                            mDelButton.setVisibility(View.VISIBLE);
                        }else{
                            mDelButton.setEnabled(false);
                            mDelButton.setVisibility(View.INVISIBLE);
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText(SinglePostItemActivity.this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        comment_list = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(comment_list);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mCommentsRecycler.setAdapter(commentsRecyclerAdapter);

        Query query = firebaseFirestore.collection("Posts/" + blogPostId + "/Comments")
                .orderBy("timestamp", Query.Direction.ASCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() { //addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>()
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) { //
                if (e == null) {
                    if (!documentSnapshots.isEmpty()) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                Comments commentItem = doc.getDocument().toObject(Comments.class);
                                comment_list.add(commentItem);
                                commentsRecyclerAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                }
            }
        });

        mDelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = thumbPath.indexOf("thumbs%2F")+9;
                int indexJpg = thumbPath.indexOf(".jpg")+4;

                String fajlNev = thumbPath.substring(index, indexJpg);

                Toast.makeText(getApplicationContext(),"i: "+index+"\n"+fajlNev, Toast.LENGTH_SHORT).show();

                StorageReference th = FirebaseStorage.getInstance().getReference().child("Post_images").child("thumbs").child(fajlNev);
                th.delete();
                StorageReference im = FirebaseStorage.getInstance().getReference().child("Post_images").child(fajlNev);
                im.delete();


                firebaseFirestore.collection("Posts").document(blogPostId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {



                        finish();
                    }
                });
            }
        });

    }
}
