package com.example.sliver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {
    private View contactsView;
    private RecyclerView myContactsList;

    private DatabaseReference contactRef, usersRef;
    private FirebaseAuth myAuth;
    private String currentUserId;

    public ContactsFragment() {}

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        myContactsList = contactsView.findViewById(R.id.contact_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        myAuth = FirebaseAuth.getInstance();
        currentUserId = myAuth.getCurrentUser().getUid();

        contactRef =
                FirebaseDatabase.getInstance(
                                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference()
                        .child("contacts")
                        .child(currentUserId);
        usersRef =
                FirebaseDatabase.getInstance(
                                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference()
                        .child("users");

        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactModel> options =
                new FirebaseRecyclerOptions.Builder<ContactModel>()
                        .setQuery(contactRef, ContactModel.class)
                        .build();

        FirebaseRecyclerAdapter<ContactModel, ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<>(options) {
                    @Override
                    protected void onBindViewHolder(
                            @NonNull ContactsViewHolder holder,
                            int position,
                            @NonNull ContactModel model) {
                        String userIds = getRef(position).getKey();
                        usersRef.child(userIds)
                                .addValueEventListener(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(
                                                    @NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("image")) {
                                                    String profileImage =
                                                            snapshot.child("image")
                                                                    .getValue()
                                                                    .toString();
                                                    String username =
                                                            snapshot.child("name")
                                                                    .getValue()
                                                                    .toString();
                                                    String userStatus =
                                                            snapshot.child("status")
                                                                    .getValue()
                                                                    .toString();

                                                    holder.username.setText(username);
                                                    holder.userStatus.setText(userStatus);
                                                    Glide.with(ContactsFragment.this)
                                                            .load(profileImage)
                                                            .placeholder(R.drawable.default_avatar)
                                                            .dontAnimate()
                                                            .into(holder.profileImage);
                                                } else {
                                                    String username =
                                                            snapshot.child("name")
                                                                    .getValue()
                                                                    .toString();
                                                    String userStatus =
                                                            snapshot.child("status")
                                                                    .getValue()
                                                                    .toString();

                                                    holder.username.setText(username);
                                                    holder.userStatus.setText(userStatus);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                            }
                                        });
                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(
                            @NonNull ViewGroup parent, int viewType) {
                        View view =
                                LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.user_display_layout, parent, false);
                        return new ContactsViewHolder(view);
                    }
                };
        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView username, userStatus;
        CircleImageView profileImage;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}
