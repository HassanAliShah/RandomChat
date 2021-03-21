package com.app.randomchat.adapters;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.randomchat.R;
import com.facebook.drawee.view.SimpleDraweeView;

public class TypeRecyclerViewHolder extends RecyclerView.ViewHolder {

    SimpleDraweeView ivUserProfile;
    TextView tvUserName;
    TextView tvLastText;
    ImageButton ibTouchField;

    ImageView photoImageView;
    TextView messageTextView;
    TextView authorTextView;

    LinearLayout layout;

    public TypeRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);

        ivUserProfile = itemView.findViewById(R.id.iv_user_profile);
        tvUserName = itemView.findViewById(R.id.tv_username);
        tvLastText = itemView.findViewById(R.id.tv_latest_txt);
        ibTouchField = itemView.findViewById(R.id.ib_touch_field);
        photoImageView = itemView.findViewById(R.id.photoImageView);
        messageTextView = itemView.findViewById(R.id.messageTextView);
        authorTextView = itemView.findViewById(R.id.nameTextView);
        layout = itemView.findViewById(R.id.layout);
    }

}


