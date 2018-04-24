package com.example.feco.photoblog;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CommentsActivity extends AppCompatActivity {
    private Toolbar commentToolbar;

    private EditText comment_field;
    private ImageView comment_post_btn;
    private RecyclerView comment_listView;
    private String blog_post_id;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private List<Comments> comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();

        commentToolbar = findViewById(R.id.comments_toolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");

        comment_listView = findViewById(R.id.comments_list);
        comment_field = findViewById(R.id.comments_field);
        comment_post_btn = findViewById(R.id.comments_post_btn);


        blog_post_id = getIntent().getStringExtra("blog_post_id");


        //Recyclerview Firebase list

        comment_list = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(comment_list);
        comment_listView.setLayoutManager(new LinearLayoutManager(this));
        comment_listView.setAdapter(commentsRecyclerAdapter);

        firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() { //addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>()
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


        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String comment_text = comment_field.getText().toString().trim();
                if (!TextUtils.isEmpty(comment_text)) {


                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", comment_text);
                    commentsMap.put("user_id", currentUserId);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());


                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(CommentsActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                comment_field.setText("");
                            }
                        }
                    });


                }
            }
        });

    }
}
