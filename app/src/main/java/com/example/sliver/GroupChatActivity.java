package com.example.sliver;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {
  private ImageButton sendMessageButton;
  private TextInputLayout userMessageInput;
  private ListView myListView;

  private String currentGroupName, currentUserID, currentUserName, messageSenderProfilePicture;
  private DatabaseReference usersRef, groupNameRef, groupMessageKeyRef;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group_chat);

    currentGroupName = getIntent().getExtras().get("groupName").toString();

    FirebaseAuth myAuth = FirebaseAuth.getInstance();
    currentUserID = myAuth.getCurrentUser().getUid();
    usersRef =
        FirebaseDatabase.getInstance(
                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()
            .child("users");
    groupNameRef =
        FirebaseDatabase.getInstance(
                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()
            .child("groups")
            .child(currentGroupName);

    initializeFields();

    getUserInfo();

    sendMessageButton.setOnClickListener(
        view -> {
          saveMessageToDatabase();
          userMessageInput.getEditText().setText("");
        });
  }

  @Override
  protected void onStart() {
    super.onStart();

    displayMessageWithFirebaseUI();
  }

  private void initializeFields() {
    Toolbar myToolbar = findViewById(R.id.group_chat_bar_layout);
    setSupportActionBar(myToolbar);
    getSupportActionBar().setTitle(currentGroupName);
    sendMessageButton = findViewById(R.id.send_message_button);
    userMessageInput = findViewById(R.id.input_group_message);
    myListView = findViewById(R.id.list_of_messages);
  }

  private void getUserInfo() {
    usersRef
        .child(currentUserID)
        .addValueEventListener(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                  currentUserName = snapshot.child("name").getValue().toString();
                  if (snapshot.hasChild("image")) {
                    messageSenderProfilePicture = snapshot.child("image").getValue().toString();
                  }
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {}
            });
  }

  private void displayMessageWithFirebaseUI() {
    Query query = groupNameRef;
    FirebaseListOptions<ChatModel> options =
        new FirebaseListOptions.Builder<ChatModel>()
            .setQuery(query, ChatModel.class)
            .setLayout(R.layout.chat_model_1)
            .setLifecycleOwner(this)
            .build();

    FirebaseListAdapter<ChatModel> adapter =
        new FirebaseListAdapter<>(options) {
          @Override
          protected void populateView(@NonNull View v, @NonNull ChatModel model, int position) {
            CircleImageView profileImage = v.findViewById(R.id.group_chat_profile_image);
            LinearLayout messageSenderSide = v.findViewById(R.id.sender_message_side);
            TextView messageContent = v.findViewById(R.id.message_text);
            TextView messageName = v.findViewById(R.id.message_user);
            TextView messageTime = v.findViewById(R.id.message_time);
            TextView messageSenderContent = v.findViewById(R.id.sender_message_content);

            messageContent.setText(model.getMessage());
            messageName.setText(model.getName());
            messageTime.setText(model.getTime());
            if (!model.getUid().equals(currentUserID)) {
              Glide.with(GroupChatActivity.this)
                  .load(model.getImage())
                  .placeholder(R.drawable.default_avatar)
                  .dontAnimate()
                  .into(profileImage);
              messageSenderContent.setVisibility(View.GONE);
              messageSenderSide.setVisibility(View.GONE);
              messageContent.setText(model.getMessage());
              messageName.setText(model.getName());
              messageTime.setText(model.getTime());
            } else {
              profileImage.setVisibility(View.GONE);
              LinearLayout notCurrentUserSide = v.findViewById(R.id.chat_not_current_user_side);
              notCurrentUserSide.setVisibility(View.GONE);
              messageSenderContent.setText(model.getMessage());
            }
          }
        };
    myListView.setAdapter(adapter);
  }

  private void saveMessageToDatabase() {
    String message = userMessageInput.getEditText().getText().toString();
    String messageKey = groupNameRef.push().getKey();

    if (!TextUtils.isEmpty(message)) {
      Calendar messageTime = Calendar.getInstance();
      SimpleDateFormat currentTimeFormat =
          new SimpleDateFormat("MMM dd, yyyy (hh:mm a)", Locale.UK);
      String currentTime = currentTimeFormat.format(messageTime.getTime());

      HashMap<String, Object> groupMessageKey = new HashMap<>();
      groupNameRef.updateChildren(groupMessageKey);

      groupMessageKeyRef = groupNameRef.child(messageKey);

      HashMap<String, Object> messageInfoMap = new HashMap<>();
      messageInfoMap.put("name", currentUserName);
      messageInfoMap.put("message", message);
      messageInfoMap.put("time", currentTime);
      messageInfoMap.put("uid", currentUserID);
      messageInfoMap.put("image", messageSenderProfilePicture);
      messageInfoMap.put("type", "text");
      groupMessageKeyRef.updateChildren(messageInfoMap);
    }
  }
}
