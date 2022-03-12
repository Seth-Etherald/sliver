package com.example.sliver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity {
  private ImageButton sendMessageButton, sendImageButton;
  private TextInputLayout userMessageInput;
  private RecyclerView myRecyclerView;

  private String currentGroupName,
      currentUserID,
      currentUserName,
      messageSenderProfilePicture,
      fileType = "",
      myUrl = "";
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
    adapterListener();
    sendMessageButton.setOnClickListener(view -> saveMessageToDatabase());
    sendImageButton.setOnClickListener(
        view -> {
          fileType = "image";
          Intent intentImage = new Intent();
          intentImage.setAction(Intent.ACTION_GET_CONTENT);
          intentImage.setType("image/*");
          startActivityForResult(intentImage, 1);
        });
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  private void adapterListener() {
    groupNameRef.addChildEventListener(
        new ChildEventListener() {
          @Override
          public void onChildAdded(
              @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            ChatModel messages = snapshot.getValue(ChatModel.class);
            messageList.add(messages);
            messageAdapter.notifyDataSetChanged();
            myRecyclerView.smoothScrollToPosition(myRecyclerView.getAdapter().getItemCount());
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

  private void initializeFields() {
    Toolbar myToolbar = findViewById(R.id.group_chat_bar_layout);
    setSupportActionBar(myToolbar);
    getSupportActionBar().setTitle(currentGroupName);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    myToolbar.setNavigationOnClickListener(view -> finish());
    sendMessageButton = findViewById(R.id.send_message_button);
    sendImageButton = findViewById(R.id.group_chat_send_file_button);
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Uri fileUri = data.getData();
    if (fileType.equals("image")) {
      StorageReference storageReference =
          FirebaseStorage.getInstance().getReference().child("Message Images");
      Calendar messageTime = Calendar.getInstance();
      SimpleDateFormat currentTimeFormat =
          new SimpleDateFormat("MMM dd, yyyy (hh:mm a)", Locale.UK);
      String currentTime = currentTimeFormat.format(messageTime.getTime());
      String messageKey = groupNameRef.push().getKey();
      StorageReference filePath = storageReference.child(messageKey + ".jpg");
      StorageTask uploadTask = filePath.putFile(fileUri);
      uploadTask
          .continueWithTask(
              task -> {
                if (!task.isSuccessful()) {
                  throw task.getException();
                }
                return filePath.getDownloadUrl();
              })
          .addOnCompleteListener(
              (OnCompleteListener<Uri>)
                  task -> {
                    if (task.isSuccessful()) {
                      Uri downloadUrl = task.getResult();
                      myUrl = downloadUrl.toString();
                      Map<String, Object> messageTextBody = new HashMap<>();
                      messageTextBody.put("name", currentUserName);
                      messageTextBody.put("message", myUrl);
                      messageTextBody.put("type", fileType);
                      messageTextBody.put("uid", currentUserID);
                      messageTextBody.put("time", currentTime);
                      messageTextBody.put("image", messageSenderProfilePicture);
                      groupNameRef.child(messageKey).updateChildren(messageTextBody);
                    }
                  });
    } else {
      Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
    }
  }
}
