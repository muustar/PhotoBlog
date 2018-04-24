package com.example.feco.photoblog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<Comments> comment_list;
    private FirebaseFirestore firebaseFirestore;
    private int selectedItem = -1;


    public CommentsRecyclerAdapter(List<Comments> comment_list) {
        this.comment_list = comment_list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        return new CommentsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentsRecyclerAdapter.ViewHolder holder, int position) {
        holder.setCommentComment(comment_list.get(position).getMessage());

        // kép és user név lekérdezés
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").document(comment_list.get(position).getUser_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try {
                    if (task.isSuccessful()) {
                        String username = task.getResult().getString("name");
                        String img_thumb_url = task.getResult().getString("image_thumb");
                        holder.setCommentUsername(username);
                        holder.setCommentPic(img_thumb_url);

                    }
                } catch (Exception e) {
                    holder.setCommentUsername("törölt");
                }
            }
        });


        String dateString;
        try {
            //dátum beállítás
            long milliseconds = comment_list.get(position).getTimestamp().getTime();
            dateString = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date(milliseconds));

        } catch (Exception e) {
            dateString = "most";
        }
        holder.setCommentdate(dateString);

        //animáció
        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        holder.mView.startAnimation(animation);


        // az utolsó bejegyzésre ugrás
        if(selectedItem == position)
            holder.mView.setSelected(true);

    }

    public void setSelectedItem ( int position)
    {
        selectedItem = position;
    }

    @Override
    public int getItemCount() {
        if (comment_list != null) {
            return comment_list.size();
        } else {
            return 0;
        }


    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        private CircleImageView commentPic;
        private TextView commentUsername;
        private TextView commentComment;
        private TextView commentdate;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }


        public void setCommentPic(String url) {
            commentPic = mView.findViewById(R.id.comment_list_pic);
            Glide.with(mView.getContext()).load(url).into(commentPic);
        }


        public void setCommentUsername(String user) {
            commentUsername = mView.findViewById(R.id.comment_list_username);
            commentUsername.setText(user);
        }


        public void setCommentComment(String message) {
            commentComment = mView.findViewById(R.id.comment_list_comment);
            commentComment.setText(message);
        }

        public void setCommentdate(String date) {
            commentdate = mView.findViewById(R.id.comment_list_date);
            commentdate.setText(date);
        }
    }
}
