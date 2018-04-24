package com.example.feco.photoblog;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.FragmentTransitionSupport;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentContainer;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {
    private FirebaseAuth mAuth;
    private String currentUserId = null;
    private FirebaseFirestore firebaseFirestore;
    private List<BlogPost> blog_list;
    private List<Users> user_list;
    private Context context;


    public BlogRecyclerAdapter(List<BlogPost> blog_list, List<Users> user_list) {
        this.blog_list = blog_list;
        this.user_list = user_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        context = parent.getContext();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final String blogPostId = blog_list.get(position).BlogPostId;

        holder.setIsRecyclable(false);
        holder.setDescText(blog_list.get(position).getDesc());
        holder.setTitleView(blog_list.get(position).getTitle());
        //holder.setTitleView(blogPostId);
        holder.setImage(blog_list.get(position).getThumb());
        holder.setUserData(user_list.get(position).getName(), user_list.get(position).getImage_thumb());

        //klikkelhető cardView

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToSinglePostItemAct = new Intent(context, SinglePostItemActivity.class);
                goToSinglePostItemAct.putExtra("blogPostId", blogPostId);
                context.startActivity(goToSinglePostItemAct);
            }
        });

        //Get Comments count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e == null) {

                    if (documentSnapshots.isEmpty()) {
                        holder.setBlogCommentCount(0);
                    } else {
                        holder.setBlogCommentCount(documentSnapshots.size());
                    }
                }
            }

        });


        //Comments feature
        holder.blogCommentLinearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goTOComments = new Intent(context, CommentsActivity.class);
                goTOComments.putExtra("blog_post_id", blogPostId);
                context.startActivity(goTOComments);
            }
        });

        //Get Likes count

        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e == null) {
                    if (documentSnapshots.isEmpty()) {
                        holder.blogLikeText.setText("0 Likes");
                    } else {
                        holder.blogLikeText.setText(String.valueOf(documentSnapshots.size()) + " Likes");
                    }
                }

            }
        });


        //Get likes
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e == null) {
                    if (documentSnapshot.exists()) {
                        holder.blogLikeBtn.setImageResource(R.mipmap.action_like_accent);

                    } else {
                        holder.blogLikeBtn.setImageResource(R.mipmap.action_like_grey);
                    }
                }
            }
        });


        //Like feature
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {

                            Map<String, Object> likeMap = new HashMap<>();
                            likeMap.put("timestamp", FieldValue.serverTimestamp());
                            //holder.blogLikeBtn.setImageResource(R.mipmap.action_like_accent);
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likeMap);


                        } else {
                            //holder.blogLikeBtn.setImageResource(R.mipmap.action_like_grey);
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();

                        }
                    }
                });

            }
        });

        // a dátum valamiért a posztolás után nem jön vissza, ezért így kezeljük le
        String dateString;
        try {
            long milliseconds = blog_list.get(position).getTimestamp().getTime();
            dateString = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date(milliseconds));
        } catch (Exception e) {
            dateString = "most";
        }
        holder.setBlogDate(dateString);

        //animáció
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_blogitem);
        holder.mView.startAnimation(animation);

        // kattintás a képen
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent singleImageI = new Intent(context, SingleImageActivity.class);
                singleImageI.putExtra("img_uri", blog_list.get(position).getImage());
                context.startActivity(singleImageI);
            }
        });

    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private FirebaseFirestore firebaseFirestore;
        private FirebaseAuth mAuth;
        private View mView;
        private TextView descView;
        private TextView titleView;
        private ImageView image;
        private CircleImageView profilePics;
        private TextView username;
        private TextView blogDate;

        private ImageView blogLikeBtn;
        private TextView blogLikeText;
        private LinearLayout blogCommentLinearlayout;
        private TextView blogCommentCount;


        public ViewHolder(View itemView) {
            super(itemView);
            mAuth = FirebaseAuth.getInstance();
            firebaseFirestore = FirebaseFirestore.getInstance();

            mView = itemView;
            blogLikeBtn = (ImageView) mView.findViewById(R.id.blog_like);
            blogLikeText = (TextView) mView.findViewById(R.id.blog_like_text);
            blogCommentLinearlayout = (LinearLayout) mView.findViewById(R.id.blog_comment_layout);
            image = (ImageView) mView.findViewById(R.id.blog_image);

        }

        public void setDescText(String text) {
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(text);
        }

        public void setTitleView(String title) {
            titleView = mView.findViewById(R.id.blog_title);
            titleView.setText(title);
        }

        public void setImage(String url) {


            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.mipmap.full_placeholder);
            Glide.with(mView.getContext())
                    .applyDefaultRequestOptions(placeHolderOptions)
                    .load(url)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            //mPrbarCircle.setVisibility(View.INVISIBLE);
                            //mImgBlue.setVisibility(View.INVISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            //mPrbarCircle.setVisibility(View.INVISIBLE);
                            //mImgBlue.setVisibility(View.INVISIBLE);
                            return false;
                        }
                    })
                    .into(image);


        }

        public void setUserData(String uname, String userImg) {
            profilePics = (CircleImageView) mView.findViewById(R.id.blog_userImage);
            username = (TextView) mView.findViewById(R.id.blog_username);


            username.setText(uname);
            // legyen egy placeholder, amig betölti a képet
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.mipmap.default_image);
            Glide.with(mView.getContext())
                    .applyDefaultRequestOptions(placeHolderOption)
                    .load(userImg)
                    .into(profilePics);
        }


        public void setBlogDate(String date) {
            blogDate = (TextView) mView.findViewById(R.id.blog_date);
            blogDate.setText(date);
        }

        public void setBlogCommentCount(int count) {
            blogCommentCount = mView.findViewById(R.id.blog_comment_txt);
            String txt = "" + count + " Comments";
            blogCommentCount.setText(txt);
        }

    }
}
