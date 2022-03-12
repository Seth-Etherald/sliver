package com.example.sliver;

import static com.bumptech.glide.request.target.Target.SIZE_ORIGINAL;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrivateMessageAdapter
    extends RecyclerView.Adapter<PrivateMessageAdapter.MessageViewHolder> {
  private List<ChatModel> messagesList;
  private FirebaseAuth myAuth;
  private ImageView imageViewNotCurrentUser, imageViewCurrentUser;

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
    holder.receiverUsername.setVisibility(View.GONE);
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
    } else {
      holder.receiverMessageText.setVisibility(View.GONE);
      holder.senderMessageText.setVisibility(View.GONE);
      if (fromUserId.equals(messageSenderId)) {
        imageViewNotCurrentUser.setVisibility(View.GONE);
        holder.notCurrentUserSide.setVisibility(View.GONE);
        holder.messageTime.setVisibility(View.GONE);
        holder.currentUserSide.setVisibility(View.VISIBLE);
        imageViewCurrentUser.setVisibility(View.VISIBLE);
        Glide.with(imageViewCurrentUser.getContext())
            .load(messages.getMessage())
            .override(SIZE_ORIGINAL)
            .into(imageViewCurrentUser);
      } else {
        imageViewCurrentUser.setVisibility(View.GONE);
        holder.currentUserSide.setVisibility(View.GONE);
        holder.notCurrentUserSide.setVisibility(View.VISIBLE);
        imageViewNotCurrentUser.setVisibility(View.VISIBLE);
        Glide.with(imageViewNotCurrentUser.getContext())
            .load(messages.getMessage())
            .override(SIZE_ORIGINAL)
            .into(imageViewNotCurrentUser);
        holder.messageTime.setText(messages.getTime());
      }
    }
  }

  @Override
  public int getItemCount() {
    return messagesList.size();
  }

  public class MessageViewHolder extends RecyclerView.ViewHolder {
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
      imageViewNotCurrentUser = itemView.findViewById(R.id.not_current_user_image_view);
      imageViewCurrentUser = itemView.findViewById(R.id.current_user_image_view);
    }
  }
}
