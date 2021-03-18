package com.app.randomchat.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.app.randomchat.Info.Info;
import com.app.randomchat.Pojo.Message;
import com.app.randomchat.Pojo.Super;
import com.app.randomchat.Pojo.User;
import com.app.randomchat.R;

import java.util.List;


public class TypeRecyclerViewAdapter extends RecyclerView.Adapter<TypeRecyclerViewHolder> implements Info {

    private static final String TAG = "TAG";
    Context context;
    List<Super> listInstances;
    int type;

    public TypeRecyclerViewAdapter(Context context, List<Super> listInstances, int type) {
        this.context = context;
        this.listInstances = listInstances;
        this.type = type;
    }

    @NonNull
    @Override
    public TypeRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: TYPE: " + viewType);
        LayoutInflater li = LayoutInflater.from(context);
        View view = li.inflate(R.layout.rv_user_detail, parent, false);
        return new TypeRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TypeRecyclerViewHolder holder, int position) {
        if (type == TYPE_USER) {
            User user = (User) listInstances.get(position);
            String userName = user.getFirstName() + " " + user.getLastName();
            holder.tvUserName.setText(userName);
//            holder.ivUserProfile.setImageURI(user.getUrlToImage());
            holder.tvLastText.setVisibility(View.GONE);
            return;
        }

        if (type == TYPE_MESSAGE) {
            Message message = (Message) listInstances.get(position);
            Log.i(TAG, "onBindViewHolder: " + message.getFromUserName());
            holder.tvUserName.setText(message.getFromUserName());
            holder.tvUserName.setVisibility(View.VISIBLE);
            holder.tvUserName.setTextColor(context.getColor(R.color.black));
            holder.ivUserProfile.setImageURI(message.getFromUserProfilePic());
            holder.tvLastText.setText(message.getText());

            return;
        }


    }

    @Override
    public int getItemCount() {
        return listInstances.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

}
