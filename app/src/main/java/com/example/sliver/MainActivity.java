package com.example.sliver;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseAuth myAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myAuth = FirebaseAuth.getInstance();
        currentUser = myAuth.getCurrentUser();
        rootRef =
                FirebaseDatabase.getInstance(
                                "https://sliver-b6693-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference();

        Toolbar myToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.app_title);

        ViewPager myViewPager = findViewById(R.id.main_tabs_pager);
        TabAccessorAdapter myTabAccessorAdapter =
                new TabAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabAccessorAdapter);

        TabLayout myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser == null) {
            sendUserToLoginActivity();
        } else {
            VerifyUserExistence();
        }
    }

    private void VerifyUserExistence() {
        String currentUserID = myAuth.getCurrentUser().getUid();
        rootRef.child("users")
                .child(currentUserID)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!(snapshot.child("name").exists())) {
                                    sendUserToSettingActivity();
                                    Toast.makeText(
                                                    MainActivity.this,
                                                    "You need an username to continue!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToSettingActivity() {
        Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingIntent);
    }

    private void sendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_find_friends_option) {
            sendUserToFindFriendsActivity();
        }

        if (item.getItemId() == R.id.main_create_group_option) {
            requestNewGroup();
        }

        if (item.getItemId() == R.id.main_settings_option) {
            sendUserToSettingActivity();
        }

        if (item.getItemId() == R.id.main_logout_option) {
            myAuth.signOut();
            sendUserToLoginActivity();
        }
        return true;
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Enter group name:");

        final EditText groupNameInput = new EditText(MainActivity.this);
        groupNameInput.setHint("e.g Family");

        builder.setView(groupNameInput);

        builder.setPositiveButton(
                "Create",
                (dialogInterface, i) -> {
                    String groupName = groupNameInput.getText().toString().trim();
                    if (TextUtils.isEmpty(groupName)) {
                        Toast.makeText(
                                        MainActivity.this,
                                        "Please enter a valid group name!",
                                        Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        createNewGroup(groupName);
                    }
                });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        builder.show();
    }

    private void createNewGroup(String groupName) {
        rootRef.child("groups")
                .child(groupName)
                .setValue("")
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(
                                                MainActivity.this,
                                                groupName + " is created successfully!",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }
}
