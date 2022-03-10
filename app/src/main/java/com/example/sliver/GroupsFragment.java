package com.example.sliver;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GroupsFragment extends Fragment {

  private final ArrayList<String> listOfGroups = new ArrayList<>();
  private View groupFragmentView;
  private ArrayAdapter<String> arrayAdapter;
  private DatabaseReference groupRef;
  private ListView listView;

  public GroupsFragment() {}

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

    groupRef =
        FirebaseDatabase.getInstance(
                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()
            .child("groups");

    initializeFields();

    retrieveAndDisplayGroups();

    listView.setOnItemClickListener(
        (adapterView, view, position, id) -> {
          String currentGroupName = adapterView.getItemAtPosition(position).toString();
          Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
          groupChatIntent.putExtra("groupName", currentGroupName);
          startActivity(groupChatIntent);
        });

    return groupFragmentView;
  }

  private void initializeFields() {
    listView = groupFragmentView.findViewById(R.id.list_view);
    arrayAdapter =
        new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, listOfGroups);
    listView.setAdapter(arrayAdapter);
  }

  private void retrieveAndDisplayGroups() {
    groupRef.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            Set<String> set = new HashSet<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
              set.add(dataSnapshot.getKey());
            }
            listOfGroups.clear();
            listOfGroups.addAll(set);
            arrayAdapter.notifyDataSetChanged();
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });
  }
}
