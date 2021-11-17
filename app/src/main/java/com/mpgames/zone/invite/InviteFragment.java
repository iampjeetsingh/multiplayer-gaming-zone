package com.mpgames.zone.invite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.mpgames.zone.App;
import com.mpgames.zone.Auth;
import com.mpgames.zone.Database;
import com.mpgames.zone.R;
import com.mpgames.zone.userlist.User;
import com.mpgames.zone.userlist.UsersAdapter;
import com.mpgames.zone.userlist.ViewHolder;

import java.util.ArrayList;

public class InviteFragment extends DialogFragment {
    Context context;
    RecyclerView recyclerView;
    Database database;
    DatabaseReference reference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();;
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.fragment_invite, container, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        recyclerView = view.findViewById(R.id.recyclerView);
        reference = Database.getUsersRef();
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(usersAdapter);
        App.setFriendsListener(() -> {
            userArrayList.clear();
            userArrayList.addAll(App.getFriends());
            usersAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private final ArrayList<User> userArrayList = new ArrayList<>();
    private final UsersAdapter usersAdapter = new UsersAdapter(userArrayList) {
        @Override
        public void populateViewHolder(ViewHolder viewHolder, User user, int i) {
            viewHolder.setName(user.getName());
            viewHolder.setUserName(user.getUserName());
            viewHolder.setDescription("last seen "+ user.getLastSeen());
            viewHolder.setImage(user.getPhotoUrl());
            viewHolder.setStatusIndicator(user.isOnline(),user.isInRoom());
            final Button invitebtn = viewHolder.getInviteButton();
            invitebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Invite invite = new Invite();
                    invite.sentTo(user);
                    invitebtn.setEnabled(false);
                    invite.setResponseListener(new Invite.ResponseListener() {
                        @Override
                        public void onInviteAccepted() {
                            invitebtn.setEnabled(true);
                            Database.getInviteRef(user.getUserName()).removeValue();
                            Toast.makeText(context,"Invite Accepted by "+ user.getUserName(),Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onInviteRejected() {
                            invitebtn.setEnabled(true);
                            Toast.makeText(context,"Invite Rejected by "+ user.getUserName(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    };
}
