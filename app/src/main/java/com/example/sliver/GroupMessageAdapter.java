package com.example.sliver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessageAdapter
    extends RecyclerView.Adapter<GroupMessageAdapter.MessageViewHolder> {
  private List<ChatModel> messagesList;
  private FirebaseAuth myAuth;
  private CircleImageView profileImage;

  public GroupMessageAdapter(List<ChatModel> messagesList) {
    this.messagesList = messagesList;
  }

  @NonNull
  @Override
  public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_model_1, parent, false);

    myAuth = FirebaseAuth.getInstance();
    return new MessageViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
    String messageSenderId = myAuth.getCurrentUser().getUid();
    ChatModel messages = messagesList.get(position);

    String fromUserId = messages.getUid();
    String fromMessageType = messages.getType();

    DatabaseReference usersRef =
        FirebaseDatabase.getInstance(
                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()
            .child("users")
            .child(fromUserId);
    usersRef.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.hasChild("image")) {
              String profileImageLink = snapshot.child("image").getValue().toString();
              Glide.with(profileImage.getContext())
                  .load(profileImageLink)
                  .placeholder(R.drawable.default_avatar)
                  .dontAnimate()
                  .into(profileImage);
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
    if (fromMessageType.equals("text")) {
      if (fromUserId.equals(messageSenderId)) {
        profileImage.setVisibility(View.GONE);
        holder.notCurrentUserSide.setVisibility(View.GONE);
        holder.senderMessageText.setText(messages.getMessage());
      } else {
        holder.receiverUsername.setText(messages.getName());
        holder.currentUserSide.setVisibility(View.GONE);
        holder.receiverMessageText.setText(messages.getMessage());
        holder.messageTime.setText(messages.getTime());
        Glide.with(profileImage.getContext())
            .load(messages.getImage())
            .placeholder(R.drawable.default_avatar)
            .dontAnimate()
            .into(profileImage);
      }
    }
  }

  @Override
  public int getItemCount() {
    return messagesList.size();
  }

  public class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView senderMessageText, receiverMessageText, receiverUsername, messageTime;
    LinearLayout notCurrentUserSide, currentUserSide;

    public MessageViewHolder(@NonNull View itemView) {
      super(itemView);
      profileImage = itemView.findViewById(R.id.group_chat_profile_image);
      notCurrentUserSide = itemView.findViewById(R.id.chat_not_current_user_side);
      currentUserSide = itemView.findViewById(R.id.sender_message_side);
      receiverUsername = itemView.findViewById(R.id.message_user);
      receiverMessageText = itemView.findViewById(R.id.message_text);
      messageTime = itemView.findViewById(R.id.message_time);
      senderMessageText = itemView.findViewById(R.id.sender_message_content);
    }
  }
}
