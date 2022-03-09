package com.example.sliver;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private RecyclerView findFriendsRecyclerList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        usersRef =
                FirebaseDatabase.getInstance(
                                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference()
                        .child("users");

        initializeFields();
    }

    private void initializeFields() {
        findFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        Toolbar myToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.find_friends_title);
        myToolbar.setNavigationOnClickListener(view -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactModel> options =
                new FirebaseRecyclerOptions.Builder<ContactModel>()
                        .setQuery(usersRef, ContactModel.class)
                        .build();
        FirebaseRecyclerAdapter<ContactModel, findFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<>(options) {
                    @Override
                    protected void onBindViewHolder(
                            @NonNull findFriendsViewHolder holder,
                            int position,
                            @NonNull ContactModel model) {
                        holder.username.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Glide.with(FindFriendsActivity.this)
                                .load(model.getImage())
                                .placeholder(R.drawable.default_avatar)
                                .dontAnimate()
                                .into(holder.profileImage);

                        holder.itemView.setOnClickListener(
                                view -> {
                                    String visit_user_id =
                                            getRef(holder.getBindingAdapterPosition()).getKey();

                                    Intent profileIntent =
                                            new Intent(
                                                    FindFriendsActivity.this,
                                                    ProfileActivity.class);
                                    profileIntent.putExtra("visit_user_id", visit_user_id);
                                    startActivity(profileIntent);
                                });
                    }

                    @NonNull
                    @Override
                    public findFriendsViewHolder onCreateViewHolder(
                            @NonNull ViewGroup parent, int viewType) {
                        View view =
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.user_display_layout, parent, false);
                        return new findFriendsViewHolder(view);
                    }
                };
        findFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class findFriendsViewHolder extends RecyclerView.ViewHolder {
        TextView username, userStatus;
        CircleImageView profileImage;

        public findFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}
