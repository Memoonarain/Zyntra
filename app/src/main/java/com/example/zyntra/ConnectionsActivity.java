package com.example.zyntra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private Button btnShowFollowed;
    private TextView txtTitle;
    private boolean showingFollowedUsers = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connections);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Initialize UI components
        rvUsers = findViewById(R.id.rvUsers);
        btnShowFollowed = findViewById(R.id.btnShowFollowed);
        txtTitle = findViewById(R.id.txtTitle);

        // Set up RecyclerView
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);
        rvUsers.setAdapter(userAdapter);

        // Set up button click listener
        btnShowFollowed.setOnClickListener(v -> {
            if (!showingFollowedUsers) {
                loadFollowedUsers();
                btnShowFollowed.setText("Show All Users");
                txtTitle.setText("Following");
                showingFollowedUsers = true;
            } else {
                loadAllUsers();
                btnShowFollowed.setText("View Followed");
                txtTitle.setText("Suggestions");
                showingFollowedUsers = false;
            }
        });

        // Set up bottom navigation
        setupBottomNav();

        // Load all users initially
        loadAllUsers();
    }

    private void loadAllUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    processUsersList(queryDocumentSnapshots, false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ConnectionsActivity.this, "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFollowedUsers() {
        db.collection("users").document(currentUserId)
                .collection("following")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        userList.clear();
                        userAdapter.notifyDataSetChanged();
                        Toast.makeText(ConnectionsActivity.this, "You are not following anyone yet", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> followedUserIds = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getString("userId");
                        if (userId != null) {
                            followedUserIds.add(userId);
                        }
                    }

                    if (!followedUserIds.isEmpty()) {
                        db.collection("users")
                                .whereIn("__name__", followedUserIds)
                                .get()
                                .addOnSuccessListener(userSnapshots -> {
                                    processUsersList(userSnapshots, true);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ConnectionsActivity.this, "Error loading followed users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ConnectionsActivity.this, "Error loading following data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void processUsersList(QuerySnapshot queryDocumentSnapshots, boolean isFollowed) {
        userList.clear();
        for (DocumentSnapshot document : queryDocumentSnapshots) {
            String userId = document.getId();
            
            // Skip current user's profile
            if (userId.equals(currentUserId)) {
                continue;
            }
            
            String firstName = document.getString("firstName");
            String lastName = document.getString("lastName");
            String email = document.getString("email");
            String profileImageUrl = document.getString("profileImageUrl");
            String bio = document.getString("bio");
            String gender = document.getString("gender");
            String birthDate = document.getString("birthDate");

            User user = new User(userId, firstName, lastName, email, profileImageUrl, bio, gender, birthDate);
            user.setFollowed(isFollowed);
            userList.add(user);
        }
        userAdapter.notifyDataSetChanged();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_connections);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_messages) {
                startActivity(new Intent(ConnectionsActivity.this, MessagesActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_newPost) {
                startActivity(new Intent(ConnectionsActivity.this, NewPost.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_home) {
                startActivity(new Intent(ConnectionsActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_notification) {
                startActivity(new Intent(ConnectionsActivity.this, NotificationsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_connections) {
                // Already on connections screen, do nothing
                return true;
            }
            return false;
        });
    }
}