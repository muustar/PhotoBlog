package com.example.feco.photoblog;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private static final int LIMIT = 3;
    private RecyclerView blog_list_view;

    private List<BlogPost> blog_list;
    private List<Users> user_list;

    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private FirebaseAuth mAuth;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        blog_list = new ArrayList<>();
        user_list = new ArrayList<>();

        blog_list_view = view.findViewById(R.id.blog_list_view);
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list, user_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        blog_list_view.setAdapter(blogRecyclerAdapter);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1); //direction 1, akkor az alját értük el  ha -1 akkor a tetejét
                    Boolean reachedTop = !recyclerView.canScrollVertically(-1);
                    if (reachedBottom && !isFirstPageFirstLoad) {
                        //loadMorePost();
                    }
                }
            });

            Query all = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING);

            all.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.d("FECO", "Hiba: " + e);
                        return;
                    }

                    for (DocumentSnapshot doc : documentSnapshots) {
                        String blogPostId = doc.getId();
                        BlogPost blogPost = doc.toObject(BlogPost.class).withId(blogPostId);
                        blog_list.add(blogPost);
                        Log.d("FECO", "blogUserId " + blogPost.getTitle());

                        String blogUserId = doc.getString("uid");

                        DocumentReference userRef = firebaseFirestore.collection("Users").document(blogUserId);
                        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot doc = task.getResult();
                                    Users user = doc.toObject(Users.class);
                                    user_list.add(user);
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        });

                    }


                }
            });

            /*
            // ˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇˇ

            Query first = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp")
                    .limit(2);

            first.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {

                            DocumentSnapshot lastVisile = documentSnapshots.getDocuments()
                                    .get(documentSnapshots.size() - 1);

                            for ()


                            Query next = firebaseFirestore.collection("Posts")
                                    .orderBy("timestamp")
                                    .startAfter(lastVisile)
                                    .limit(2);
                        }
                    });

            // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
            */


            /*
            Query firstQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(LIMIT);


            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() { //getActivity()
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (e == null) {
                        if (isFirstPageFirstLoad) {


                            lastVisible = documentSnapshots.getDocuments()
                                    .get(documentSnapshots.size() - 1);
                            isFirstPageFirstLoad = false;
                        }


                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                String blogUserId = doc.getDocument().getString("uid");

                                firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Users user = task.getResult().toObject(Users.class);


                                            if (isFirstPageFirstLoad) {

                                                blog_list.add(blogPost);
                                                user_list.add(user);

                                            } else {

                                                blog_list.add(0, blogPost);
                                                user_list.add(0, user);

                                            }
                                            blogRecyclerAdapter.notifyDataSetChanged();

                                        }
                                    }
                                });


                            }

                            if (doc.getType() == DocumentChange.Type.REMOVED) {
                                String blogPostId = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                //blog_list.remove(blogPost);
                                //blogRecyclerAdapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "törölve lett egy post", Toast.LENGTH_SHORT).show();
                            }

                        }


                    } else {
                        Toast.makeText(getContext(), "hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            */

        }

        return view;
    }


    public void loadMorePost() {
        Toast.makeText(getContext(), "mégtöbb", Toast.LENGTH_SHORT).show();
        Query nextQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(LIMIT);

        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {  // a kijeletkezéskor történő crasht, megoldotta a "getActivity()"
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e == null) {
                    if (!documentSnapshots.isEmpty()) {

                        lastVisible = documentSnapshots.getDocuments()
                                .get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);


                                String blogUserId = doc.getDocument().getString("uid");

                                firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Users user = task.getResult().toObject(Users.class);

                                            blog_list.add(blogPost);
                                            user_list.add(user);

                                            blogRecyclerAdapter.notifyDataSetChanged();

                                        }
                                    }
                                });

                            }

                        }

                    }
                } else {
                    Toast.makeText(getContext(), "hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
