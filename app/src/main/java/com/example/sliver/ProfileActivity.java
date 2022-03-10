package com.example.sliver;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

  private String receiverUserId, currentUserID, REQUEST_STATE_FLAG;

  private CircleImageView userProfileImage;
  private TextView userProfileName, userProfileStatus;
  private Button sendMessageRequestButton, declineMessageRequestButton;
  private DatabaseReference userRef, chatRequestRef, contactsRef;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    userRef =
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
    FirebaseAuth myAuth = FirebaseAuth.getInstance();
    currentUserID = myAuth.getCurrentUser().getUid();
    receiverUserId = getIntent().getExtras().get("visit_user_id").toString();

    initializeFields();
    REQUEST_STATE_FLAG = "new";

    if (receiverUserId.equals(currentUserID)) {
      sendMessageRequestButton.setVisibility(View.GONE);
    }

    retrieveUserInfo();
    sendMessageRequestButton.setOnClickListener(
        view -> {
          sendMessageRequestButton.setEnabled(false);
          if (REQUEST_STATE_FLAG.equals("new")) {
            sendChatRequest();
          }
          if (REQUEST_STATE_FLAG.equals("request_sent")) {
            cancelChatRequest();
          }
          if (REQUEST_STATE_FLAG.equals("request_received")) {
            acceptChatRequest();
          }
          if (REQUEST_STATE_FLAG.equals("friends")) {
            removeSpecificContact();
          }
        });
  }

  private void initializeFields() {
    userProfileImage = findViewById(R.id.visit_profile_image);
    userProfileName = findViewById(R.id.visit_username);
    userProfileStatus = findViewById(R.id.visit_user_status);
    sendMessageRequestButton = findViewById(R.id.send_message_request_button);
    declineMessageRequestButton = findViewById(R.id.decline_message_request_button);
  }

  private void retrieveUserInfo() {
    userRef
        .child(receiverUserId)
        .addValueEventListener(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("image"))) {
                  String userImage = snapshot.child("image").getValue().toString();
                  String userName = snapshot.child("name").getValue().toString();
                  String userStatus = snapshot.child("status").getValue().toString();

                  Glide.with(ProfileActivity.this)
                      .load(userImage)
                      .placeholder(R.drawable.default_avatar)
                      .dontAnimate()
                      .into(userProfileImage);
                  userProfileName.setText(userName);
                  userProfileStatus.setText(userStatus);
                  manageChatRequestStatus();
                } else {
                  String userName = snapshot.child("name").getValue().toString();
                  String userStatus = snapshot.child("status").getValue().toString();
                  userProfileName.setText(userName);
                  userProfileStatus.setText(userStatus);
                  manageChatRequestStatus();
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {}
            });
  }

  private void sendChatRequest() {
    chatRequestRef
        .child(currentUserID)
        .child(receiverUserId)
        .child("request_type")
        .setValue("sent")
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                chatRequestRef
                    .child(receiverUserId)
                    .child(currentUserID)
                    .child("request_type")
                    .setValue("received")
                    .addOnCompleteListener(
                        task1 -> {
                          if (task1.isSuccessful()) {
                            sendMessageRequestButton.setEnabled(true);
                            REQUEST_STATE_FLAG = "request_sent";
                            Toast.makeText(this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                            sendMessageRequestButton.setText(R.string.cancel_message_request);
                          }
                        });
              }
            });
  }

  private void cancelChatRequest() {
    chatRequestRef
        .child(currentUserID)
        .child(receiverUserId)
        .removeValue()
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                chatRequestRef
                    .child(receiverUserId)
                    .child(currentUserID)
                    .removeValue()
                    .addOnCompleteListener(
                        task1 -> {
                          if (task1.isSuccessful()) {
                            sendMessageRequestButton.setEnabled(true);
                            REQUEST_STATE_FLAG = "new";
                            sendMessageRequestButton.setText(R.string.send_message_request);
                            declineMessageRequestButton.setVisibility(View.GONE);
                          }
                        });
              }
            });
  }

  private void acceptChatRequest() {
    contactsRef
        .child(currentUserID)
        .child(receiverUserId)
        .child("contacts")
        .setValue("saved")
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                contactsRef
                    .child(receiverUserId)
                    .child(currentUserID)
                    .child("contacts")
                    .setValue("saved")
                    .addOnCompleteListener(
                        task13 -> {
                          if (task13.isSuccessful()) {
                            chatRequestRef
                                .child(currentUserID)
                                .child(receiverUserId)
                                .removeValue()
                                .addOnCompleteListener(
                                    task12 -> {
                                      if (task12.isSuccessful()) {
                                        chatRequestRef
                                            .child(receiverUserId)
                                            .child(currentUserID)
                                            .removeValue()
                                            .addOnCompleteListener(
                                                task1 -> {
                                                  sendMessageRequestButton.setEnabled(true);
                                                  REQUEST_STATE_FLAG = "friends";
                                                  sendMessageRequestButton.setText(
                                                      R.string.remove_contact);
                                                  declineMessageRequestButton.setVisibility(
                                                      View.GONE);
                                                });
                                      }
                                    });
                          }
                        });
              }
            });
  }

  private void removeSpecificContact() {
    contactsRef
        .child(currentUserID)
        .child(receiverUserId)
        .removeValue()
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                contactsRef
                    .child(receiverUserId)
                    .child(currentUserID)
                    .removeValue()
                    .addOnCompleteListener(
                        task1 -> {
                          if (task1.isSuccessful()) {
                            sendMessageRequestButton.setEnabled(true);
                            REQUEST_STATE_FLAG = "new";
                            sendMessageRequestButton.setText(R.string.send_message_request);
                            declineMessageRequestButton.setVisibility(View.GONE);
                          }
                        });
              }
            });
  }

  private void manageChatRequestStatus() {
    chatRequestRef
        .child(currentUserID)
        .addValueEventListener(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserId)) {
                  String request_type =
                      snapshot.child(receiverUserId).child("request_type").getValue().toString();
                  if (request_type.equals("sent")) {
                    REQUEST_STATE_FLAG = "request_sent";
                    sendMessageRequestButton.setText(R.string.cancel_message_request);
                  } else if (request_type.equals("received")) {
                    REQUEST_STATE_FLAG = "request_received";
                    sendMessageRequestButton.setText(R.string.accept_message_request);
                    declineMessageRequestButton.setVisibility(View.VISIBLE);
                    declineMessageRequestButton.setEnabled(true);
                    declineMessageRequestButton.setOnClickListener(view -> cancelChatRequest());
                  } else {
                    REQUEST_STATE_FLAG = "friends";
                    sendMessageRequestButton.setText(R.string.remove_contact);
                  }
                } else {
                  contactsRef
                      .child(currentUserID)
                      .addListenerForSingleValueEvent(
                          new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                              if (snapshot.hasChild(receiverUserId)) {
                                REQUEST_STATE_FLAG = "friends";
                                sendMessageRequestButton.setText(R.string.remove_contact);
                              }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                          });
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {}
            });
  }
}
