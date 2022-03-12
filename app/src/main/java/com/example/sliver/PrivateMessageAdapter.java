package com.example.sliver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrivateMessageAdapter
    extends RecyclerView.Adapter<PrivateMessageAdapter.MessageViewHolder> {
  private List<ChatModel> messagesList;
  private FirebaseAuth myAuth;

  public PrivateMessageAdapter(List<ChatModel> messagesList) {
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
    holder.profileImage.setVisibility(View.GONE);
    if (fromMessageType.equals("text")) {
      if (fromUserId.equals(messageSenderId)) {
        holder.notCurrentUserSide.setVisibility(View.GONE);
        holder.currentUserSide.setVisibility(View.VISIBLE);
        holder.senderMessageText.setText(messages.getMessage());
      } else {
        holder.currentUserSide.setVisibility(View.GONE);
        holder.notCurrentUserSide.setVisibility(View.VISIBLE);
        holder.receiverMessageText.setText(messages.getMessage());
        holder.messageTime.setText(messages.getTime());
      }
    }
  }

  @Override
  public int getItemCount() {
    return messagesList.size();
  }

  public static class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView senderMessageText, receiverMessageText, receiverUsername, messageTime;
    public CircleImageView profileImage;
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
