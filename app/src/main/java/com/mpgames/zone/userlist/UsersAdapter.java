package com.mpgames.zone.userlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mpgames.zone.R;
import java.util.ArrayList;

public abstract class UsersAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final ArrayList<User> userArrayList;
    public UsersAdapter(ArrayList<User> userArrayList) {
        this.userArrayList = userArrayList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.user_row,parent,false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        populateViewHolder(holder, userArrayList.get(position), position);
    }
    public abstract void populateViewHolder(ViewHolder viewHolder, final User user, int i);
    @Override
    public int getItemCount() {
        return userArrayList.size();
    }
}
