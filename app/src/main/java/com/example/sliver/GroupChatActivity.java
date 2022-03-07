package com.example.sliver;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar myToolbar;
    private ImageButton sendMessageButton;
    private TextInputLayout userMessageInput;
    private ListView myListView;
    private ScrollView myScrollView;
    private TextView displayTextMessages;

    private String currentGroupName, currentUserID, currentUserName, currentTime;

    private FirebaseAuth myAuth;
    private DatabaseReference usersRef, groupNameRef, groupMessageKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        myAuth = FirebaseAuth.getInstance();
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
                    myScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupNameRef.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(
                            @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()) {
                            displayMessages(snapshot);
                        }
                    }

                    @Override
                    public void onChildChanged(
                            @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()) {
                            displayMessages(snapshot);
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(
                            @NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        myScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private void initializeFields() {
        myToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        myScrollView = findViewById(R.id.my_scroll_view);
        displayTextMessages = findViewById(R.id.group_chat_text_display);
    }

    private void getUserInfo() {
        usersRef.child(currentUserID)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    currentUserName = snapshot.child("name").getValue().toString();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
    }

    private void displayMessages(DataSnapshot snapshot) {
        Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            displayTextMessages.append(chatName + ":\n" + chatMessage + "\n" + chatTime + "\n\n\n");
            myScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void saveMessageToDatabase() {
        String message = userMessageInput.getEditText().getText().toString();
        String messageKey = groupNameRef.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Message content is empty", Toast.LENGTH_SHORT).show();
        } else {
            Calendar messageTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat =
                    new SimpleDateFormat("hh:mm a \t\t MMM dd, yyyy", Locale.UK);
            currentTime = currentTimeFormat.format(messageTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("time", currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }
}
