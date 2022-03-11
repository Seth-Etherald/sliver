package com.example.sliver;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GroupChatActivity extends AppCompatActivity {
  private ImageButton sendMessageButton;
  private TextInputLayout userMessageInput;
  private RecyclerView myRecyclerView;

  private String currentGroupName, currentUserID, currentUserName, messageSenderProfilePicture;
  private DatabaseReference usersRef, groupNameRef, groupMessageKeyRef;
  private List<ChatModel> messageList = new ArrayList<>();
  private GroupMessageAdapter messageAdapter;

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

    sendMessageButton.setOnClickListener(view -> saveMessageToDatabase());
  }

  @Override
  protected void onStart() {
    super.onStart();
    groupNameRef.addChildEventListener(
        new ChildEventListener() {
          @Override
          public void onChildAdded(
              @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            ChatModel messages = snapshot.getValue(ChatModel.class);
            messageList.add(messages);
            messageAdapter.notifyDataSetChanged();
          }

          @Override
          public void onChildChanged(
              @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

          @Override
          public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

          @Override
          public void onChildMoved(
              @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }

  @Override
  protected void onPause() {
    super.onPause();
    messageList.clear();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    messageList.clear();
  }

  private void initializeFields() {
    Toolbar myToolbar = findViewById(R.id.group_chat_bar_layout);
    setSupportActionBar(myToolbar);
    getSupportActionBar().setTitle(currentGroupName);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    myToolbar.setNavigationOnClickListener(view -> finish());
    sendMessageButton = findViewById(R.id.send_message_button);
    userMessageInput = findViewById(R.id.input_group_message);
    myRecyclerView = findViewById(R.id.group_message_list);
    messageAdapter = new GroupMessageAdapter(messageList);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    myRecyclerView.setLayoutManager(linearLayoutManager);
    myRecyclerView.setAdapter(messageAdapter);
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
      groupMessageKeyRef
          .updateChildren(messageInfoMap)
          .addOnCompleteListener(task -> userMessageInput.getEditText().setText(""));
    }
  }
}
