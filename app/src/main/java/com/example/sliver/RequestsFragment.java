package com.example.sliver;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class RequestsFragment extends Fragment {
  private View requestFragmentView;
  private RecyclerView myRequestsList;
  private DatabaseReference chatRequestRef, usersRef, contactsRef;
  private FirebaseAuth myAuth;
  private String currentUserId;

  public RequestsFragment() {}

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    requestFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

    myAuth = FirebaseAuth.getInstance();
    currentUserId = myAuth.getCurrentUser().getUid();
    usersRef =
        FirebaseDatabase.getInstance(
                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()
            .child("users");
    chatRequestRef =
        FirebaseDatabase.getInstance(
                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()
            .child("chat_requests");
    contactsRef =
        FirebaseDatabase.getInstance(
                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()
            .child("contacts");

    myRequestsList = requestFragmentView.findViewById(R.id.chat_requests_list);
    myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
    startAdapter();
    return requestFragmentView;
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  private void startAdapter() {
    FirebaseRecyclerOptions<ContactModel> options =
        new FirebaseRecyclerOptions.Builder<ContactModel>()
            .setQuery(chatRequestRef.child(currentUserId), ContactModel.class)
            .build();

    FirebaseRecyclerAdapter<ContactModel, requestsViewHolder> adapter =
        new FirebaseRecyclerAdapter<>(options) {
          @Override
          protected void onBindViewHolder(
              @NonNull requestsViewHolder holder, int position, @NonNull ContactModel model) {
            final String list_user_id = getRef(position).getKey();
            DatabaseReference getRequestTypeRef = getRef(position).child("request_type").getRef();

            getRequestTypeRef.addValueEventListener(
                new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                      String type = snapshot.getValue().toString();
                      if (type.equals("received")) {
                        usersRef
                            .child(list_user_id)
                            .addValueEventListener(
                                new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild("image")) {
                                      final String requestProfileImage =
                                          snapshot.child("image").getValue().toString();
                                      Glide.with(RequestsFragment.this)
                                          .load(requestProfileImage)
                                          .placeholder(R.drawable.default_avatar)
                                          .dontAnimate()
                                          .into(holder.profileImage);
                                    }
                                    Resources res = getResources();
                                    final String requestUsername =
                                        snapshot.child("name").getValue().toString();
                                    final String sentStatus =
                                        res.getString(
                                            R.string.request_user_status, requestUsername);
                                    holder.username.setText(requestUsername);
                                    holder.userStatus.setText(sentStatus);
                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError error) {}
                                });
                        holder.acceptButton.setVisibility(View.VISIBLE);
                        holder.declineButton.setVisibility(View.VISIBLE);

                        holder.acceptButton.setOnClickListener(
                            view ->
                                contactsRef
                                    .child(currentUserId)
                                    .child(list_user_id)
                                    .child("contacts")
                                    .setValue("saved")
                                    .addOnCompleteListener(
                                        task -> {
                                          if (task.isSuccessful()) {
                                            contactsRef
                                                .child(list_user_id)
                                                .child(currentUserId)
                                                .child("contacts")
                                                .setValue("saved")
                                                .addOnCompleteListener(
                                                    task13 -> {
                                                      if (task13.isSuccessful()) {
                                                        chatRequestRef
                                                            .child(currentUserId)
                                                            .child(list_user_id)
                                                            .removeValue()
                                                            .addOnCompleteListener(
                                                                task2 -> {
                                                                  if (task2.isSuccessful()) {
                                                                    chatRequestRef
                                                                        .child(list_user_id)
                                                                        .child(currentUserId)
                                                                        .removeValue();
                                                                    Toast.makeText(
                                                                            getContext(),
                                                                            "New contact added!",
                                                                            Toast.LENGTH_SHORT)
                                                                        .show();
                                                                  }
                                                                });
                                                      }
                                                    });
                                          }
                                        }));
                        holder.declineButton.setOnClickListener(
                            view ->
                                chatRequestRef
                                    .child(currentUserId)
                                    .child(list_user_id)
                                    .removeValue()
                                    .addOnCompleteListener(
                                        task -> {
                                          if (task.isSuccessful()) {
                                            chatRequestRef
                                                .child(list_user_id)
                                                .child(currentUserId)
                                                .removeValue();
                                            Toast.makeText(
                                                    getContext(),
                                                    "Friend request decline!",
                                                    Toast.LENGTH_SHORT)
                                                .show();
                                          }
                                        }));
                      } else {
                        usersRef
                            .child(list_user_id)
                            .addValueEventListener(
                                new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild("image")) {
                                      final String requestProfileImage =
                                          snapshot.child("image").getValue().toString();
                                      Glide.with(RequestsFragment.this)
                                          .load(requestProfileImage)
                                          .placeholder(R.drawable.default_avatar)
                                          .dontAnimate()
                                          .into(holder.profileImage);
                                    }
                                    Resources res = getResources();
                                    final String requestUsername =
                                        snapshot.child("name").getValue().toString();
                                    final String sentStatus =
                                        res.getString(
                                            R.string.request_user_send_status, requestUsername);
                                    holder.username.setText(requestUsername);
                                    holder.userStatus.setText(sentStatus);
                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError error) {}
                                });
                        holder.declineButton.setVisibility(View.VISIBLE);
                        holder.declineButton.setText(R.string.cancel_message_request);
                        holder.declineButton.setOnClickListener(
                            view ->
                                chatRequestRef
                                    .child(currentUserId)
                                    .child(list_user_id)
                                    .removeValue()
                                    .addOnCompleteListener(
                                        task -> {
                                          if (task.isSuccessful()) {
                                            chatRequestRef
                                                .child(list_user_id)
                                                .child(currentUserId)
                                                .removeValue();
                                            Toast.makeText(
                                                    getContext(),
                                                    "Friend request cancelled",
                                                    Toast.LENGTH_SHORT)
                                                .show();
                                          }
                                        }));
                      }
                    }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError error) {}
                });
          }

          @NonNull
          @Override
          public requestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_display_layout, parent, false);
            return new requestsViewHolder(view);
          }
        };
    myRequestsList.setAdapter(adapter);
    adapter.startListening();
  }

  public static class requestsViewHolder extends RecyclerView.ViewHolder {
    TextView username, userStatus;
    CircleImageView profileImage;
    Button acceptButton, declineButton;

    public requestsViewHolder(@NonNull View itemView) {
      super(itemView);
      username = itemView.findViewById(R.id.user_profile_name);
      userStatus = itemView.findViewById(R.id.user_profile_status);
      profileImage = itemView.findViewById(R.id.user_profile_image);
      acceptButton = itemView.findViewById(R.id.request_accept_button);
      declineButton = itemView.findViewById(R.id.request_decline_button);
    }
  }
}
