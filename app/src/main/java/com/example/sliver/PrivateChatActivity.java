package com.example.sliver;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrivateChatActivity extends AppCompatActivity {

  private String messageReceiverId, messageReceiverName, messageReceiverProfileImage;
  private TextView privateChatUsername, privateChatUserStatus;
  private CircleImageView privateChatProfileImage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_private_chat);

    messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
    messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
    messageReceiverProfileImage =
        getIntent().getExtras().get("visit_user_profile_image").toString();

    initializeFields();
    privateChatUsername.setText(messageReceiverName);
    Glide.with(PrivateChatActivity.this)
        .load(messageReceiverProfileImage)
        .placeholder(R.drawable.default_avatar)
        .dontAnimate()
        .into(privateChatProfileImage);
  }

  private void initializeFields() {
    Toolbar myToolbar = findViewById(R.id.private_chat_toolbar);
    privateChatProfileImage = findViewById(R.id.private_chat_profile_image);
    privateChatUsername = findViewById(R.id.private_chat_username);
    privateChatUserStatus = findViewById(R.id.private_chat_last_seen);
    setSupportActionBar(myToolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setTitle(null);
    myToolbar.setNavigationOnClickListener(view -> finish());
  }
}
