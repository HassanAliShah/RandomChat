package com.app.randomchat.adapters;

import android.view.View;
import android.widget.ImageButton;
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

    public TypeRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);

        ivUserProfile = itemView.findViewById(R.id.iv_user_profile);
        tvUserName = itemView.findViewById(R.id.tv_username);
        tvLastText = itemView.findViewById(R.id.tv_latest_txt);
        ibTouchField = itemView.findViewById(R.id.ib_touch_field);

    }

}


