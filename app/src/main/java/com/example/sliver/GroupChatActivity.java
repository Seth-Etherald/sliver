package com.example.sliver;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar myToolbar;
    private ImageButton sendMessageButton;
    private TextInputLayout userMessageInput;
    private ListView myListView;

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
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        displayMessageWithFirebaseUI();
    }

    private void initializeFields() {
        myToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        myListView = findViewById(R.id.list_of_messages);
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

    private void displayMessageWithFirebaseUI() {
        Query query = groupNameRef;
        FirebaseListOptions<ChatModel> options =
                new FirebaseListOptions.Builder<ChatModel>()
                        .setQuery(query, ChatModel.class)
                        .setLayout(R.layout.chat_group_model)
                        .setLifecycleOwner(this)
                        .build();

        FirebaseListAdapter<ChatModel> adapter =
                new FirebaseListAdapter<>(options) {
                    @Override
                    protected void populateView(
                            @NonNull View v, @NonNull ChatModel model, int position) {
                        TextView messageContent = v.findViewById(R.id.message_text);
                        TextView messageName = v.findViewById(R.id.message_user);
                        TextView messageTime = v.findViewById(R.id.message_time);

                        messageContent.setText(model.getMessage());
                        messageName.setText(model.getName());
                        messageTime.setText(model.getTime());
                    }
                };
        myListView.setAdapter(adapter);
    }

    private void saveMessageToDatabase() {
        String message = userMessageInput.getEditText().getText().toString();
        String messageKey = groupNameRef.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Message content is empty", Toast.LENGTH_SHORT).show();
        } else {
            Calendar messageTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat =
                    new SimpleDateFormat("MMM dd, yyyy (hh:mm a)", Locale.UK);
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
