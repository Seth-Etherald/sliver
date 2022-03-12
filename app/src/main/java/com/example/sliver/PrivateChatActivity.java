package com.example.sliver;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrivateChatActivity extends AppCompatActivity {

  private String messageReceiverId,
      messageReceiverName,
      messageReceiverProfileImage,
      messageSenderId;
  private TextView privateChatUsername, privateChatUserStatus;
  private CircleImageView privateChatProfileImage;
  private ImageButton privateChatButton;
  private TextInputLayout privateChatInput;
  private FirebaseAuth myAuth;
  private DatabaseReference rootRef;
  private List<ChatModel> messageList = new ArrayList<>();
  private LinearLayoutManager linearLayoutManager;
  private PrivateMessageAdapter messageAdapter;
  private RecyclerView privateChatView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_private_chat);

    myAuth = FirebaseAuth.getInstance();
    messageSenderId = myAuth.getCurrentUser().getUid();
    rootRef =
        FirebaseDatabase.getInstance(
                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference();

    messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
    messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
    messageReceiverProfileImage =
        getIntent().getExtras().get("visit_user_profile_image").toString();

    initializeFields();
    privateChatButton.setOnClickListener(view -> sendMessage());
  }

  private void initializeFields() {
    Toolbar myToolbar = findViewById(R.id.private_chat_toolbar);
    privateChatProfileImage = findViewById(R.id.private_chat_profile_image);
    privateChatUsername = findViewById(R.id.private_chat_username);
    privateChatUserStatus = findViewById(R.id.private_chat_last_seen);
    privateChatButton = findViewById(R.id.private_chat_send_message_button);
    privateChatInput = findViewById(R.id.input_private_message);
    privateChatView = findViewById(R.id.private_message_list);
    setSupportActionBar(myToolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setTitle(null);
    myToolbar.setNavigationOnClickListener(view -> finish());
    privateChatUsername.setText(messageReceiverName);
    Glide.with(PrivateChatActivity.this)
        .load(messageReceiverProfileImage)
        .placeholder(R.drawable.default_avatar)
        .dontAnimate()
        .into(privateChatProfileImage);

    messageAdapter = new PrivateMessageAdapter(messageList);
    linearLayoutManager = new LinearLayoutManager(this);
    privateChatView.setLayoutManager(linearLayoutManager);
    privateChatView.setAdapter(messageAdapter);
  }

  @Override
  protected void onStart() {
    super.onStart();
    checkUserState();
    rootRef
        .child("messages")
        .child(messageSenderId)
        .child(messageReceiverId)
        .addChildEventListener(
            new ChildEventListener() {
              @Override
              public void onChildAdded(
                  @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatModel messages = snapshot.getValue(ChatModel.class);
                messageList.add(messages);
                messageAdapter.notifyDataSetChanged();
                privateChatView.smoothScrollToPosition(privateChatView.getAdapter().getItemCount());
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

  private void sendMessage() {
    String messageText = privateChatInput.getEditText().getText().toString();
    if (!TextUtils.isEmpty(messageText)) {
      String messageSenderRef = "messages/" + messageSenderId + "/" + messageReceiverId;
      String messageReceiverRef = "messages/" + messageReceiverId + "/" + messageSenderId;
      Calendar messageTime = Calendar.getInstance();
      SimpleDateFormat currentTimeFormat =
          new SimpleDateFormat("MMM dd, yyyy (hh:mm a)", Locale.UK);
      String currentTime = currentTimeFormat.format(messageTime.getTime());
      DatabaseReference userMessageKeyRef =
          rootRef.child("messages").child(messageSenderId).child(messageReceiverId).push();
      String messageId = userMessageKeyRef.getKey();
      Map<String, String> messageTextBody = new HashMap<>();
      messageTextBody.put("message", messageText);
      messageTextBody.put("type", "text");
      messageTextBody.put("uid", messageSenderId);
      messageTextBody.put("time", currentTime);
      Map<String, Object> messageBodyDetails = new HashMap<>();
      messageBodyDetails.put(messageSenderRef + "/" + messageId, messageTextBody);
      messageBodyDetails.put(messageReceiverRef + "/" + messageId, messageTextBody);
      rootRef
          .updateChildren(messageBodyDetails)
          .addOnCompleteListener(task -> privateChatInput.getEditText().setText(""));
    }
  }

  private void checkUserState() {
    Resources res = getResources();
    rootRef
        .child("users")
        .child(messageReceiverId)
        .child("user_state")
        .addValueEventListener(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userState = snapshot.child("state").getValue().toString();
                if (userState.equals("online")) {
                  privateChatUserStatus.setText(R.string.state_online);
                } else {
                  String time = snapshot.child("time").getValue().toString();
                  String lastSeenStatus = res.getString(R.string.last_seen_status, time);
                  privateChatUserStatus.setText(lastSeenStatus);
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {}
            });
  }
}
