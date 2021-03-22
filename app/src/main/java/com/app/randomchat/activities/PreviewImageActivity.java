package com.app.randomchat.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.randomchat.Info.Info;
import com.app.randomchat.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.drawee.view.SimpleDraweeView;

public class PreviewImageActivity extends AppCompatActivity implements Info {

    SimpleDraweeView ivImage;
    TextView tvAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);

        ivImage = findViewById(R.id.iv_image);
        tvAge = findViewById(R.id.tv_age);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String imageUri = bundle.get(KEY_IMAGE).toString();
            RequestOptions options = new RequestOptions()
                    .fitCenter();
            Glide.with(this).load(imageUri).apply(options).into(ivImage);
            String age = bundle.getString(KEY_AGE);
            tvAge.setText(age);
        }

    }

    public void back(View view) {
        onBackPressed();
    }
}