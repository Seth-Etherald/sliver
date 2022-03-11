package com.example.sliver;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

  private View privateChatView;
  private RecyclerView chatsList;
  private DatabaseReference privateChatRef, usersRef;
  private FirebaseAuth myAuth;
  private String currentUserId, profileImage;

  public ChatsFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    privateChatView = inflater.inflate(R.layout.fragment_chats, container, false);

    myAuth = FirebaseAuth.getInstance();
    currentUserId = myAuth.getCurrentUser().getUid();

    privateChatRef =
        FirebaseDatabase.getInstance(
                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()
            .child("contacts")
            .child(currentUserId);
    usersRef =
        FirebaseDatabase.getInstance(
                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()
            .child("users");

    chatsList = privateChatView.findViewById(R.id.private_chat_list);
    chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

    return privateChatView;
  }

  @Override
  public void onStart() {
    super.onStart();
    FirebaseRecyclerOptions<ContactModel> options =
        new FirebaseRecyclerOptions.Builder<ContactModel>()
            .setQuery(privateChatRef, ContactModel.class)
            .build();

    FirebaseRecyclerAdapter<ContactModel, ChatsViewHolder> adapter =
        new FirebaseRecyclerAdapter<>(options) {
          @Override
          protected void onBindViewHolder(
              @NonNull ChatsViewHolder holder, int position, @NonNull ContactModel model) {
            String userIds = getRef(position).getKey();
            Resources res = getResources();
            usersRef
                .child(userIds)
                .addValueEventListener(
                    new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                          if (snapshot.hasChild("image")) {
                            profileImage = snapshot.child("image").getValue().toString();

                            Glide.with(ChatsFragment.this)
                                .load(profileImage)
                                .placeholder(R.drawable.default_avatar)
                                .dontAnimate()
                                .into(holder.profileImage);
                          }
                          String username = snapshot.child("name").getValue().toString();
                          String userStatus = snapshot.child("status").getValue().toString();
                          String lastSeenStatus =
                              res.getString(R.string.last_seen_status, "Date Time");
                          holder.username.setText(username);
                          holder.userStatus.setText(lastSeenStatus);
                          holder.itemView.setOnClickListener(
                              view -> {
                                Intent privateChatIntent =
                                    new Intent(getContext(), PrivateChatActivity.class);
                                privateChatIntent.putExtra("visit_user_id", userIds);
                                privateChatIntent.putExtra("visit_user_name", username);
                                privateChatIntent.putExtra(
                                    "visit_user_profile_image", profileImage);
                                startActivity(privateChatIntent);
                              });
                        }
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError error) {}
                    });
          }

          @NonNull
          @Override
          public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_display_layout, parent, false);
            return new ChatsViewHolder(view);
          }
        };
    chatsList.setAdapter(adapter);
    adapter.startListening();
  }

  public static class ChatsViewHolder extends RecyclerView.ViewHolder {
    TextView username, userStatus;
    CircleImageView profileImage;

    public ChatsViewHolder(@NonNull View itemView) {
      super(itemView);
      username = itemView.findViewById(R.id.user_profile_name);
      userStatus = itemView.findViewById(R.id.user_profile_status);
      profileImage = itemView.findViewById(R.id.user_profile_image);
    }
  }
}
