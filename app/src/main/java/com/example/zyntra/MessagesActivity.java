package com.example.zyntra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    private RecyclerView rvChats;
    private TextView txtEmptyChats;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_messages);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Initialize views
        rvChats = findViewById(R.id.rvChats);
        txtEmptyChats = findViewById(R.id.txtEmptyChats);

        // Set up RecyclerView
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatList);
        rvChats.setAdapter(chatAdapter);

        // Set up bottom navigation
        setupBottomNav();

        // Load chats
        loadChats();
    }

    private void loadChats() {
        // Find chats where current user is either user1 or user2
        db.collection("chats")
                .whereEqualTo("participants." + currentUserId, true)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        chatList.clear();
                        List<String> userIds = new ArrayList<>();

                        // First, get all chats and the other users' IDs
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Chat chat = document.toObject(Chat.class);
                            String otherUserId;

                            if (currentUserId.equals(chat.getUser1Id())) {
                                otherUserId = chat.getUser2Id();
                            } else {
                                otherUserId = chat.getUser1Id();
                            }

                            userIds.add(otherUserId);
                            chatList.add(chat);
                        }

                        // Then fetch all user info in one batch
                        if (!userIds.isEmpty()) {
                            db.collection("users")
                                    .whereIn(FieldPath.documentId(), userIds)
                                    .get()
                                    .addOnSuccessListener(userDocs -> {
                                        // Create a map of userId -> user info
                                        for (DocumentSnapshot userDoc : userDocs) {
                                            String userId = userDoc.getId();
                                            String firstName = userDoc.getString("firstName");
                                            String lastName = userDoc.getString("lastName");
                                            String fullName = (firstName != null ? firstName : "") + 
                                                    " " + (lastName != null ? lastName : "");
                                            String profileImageUrl = userDoc.getString("profileImageUrl");

                                            // Update the chat objects with user info
                                            for (Chat chat : chatList) {
                                                String chatOtherUserId;
                                                if (currentUserId.equals(chat.getUser1Id())) {
                                                    chatOtherUserId = chat.getUser2Id();
                                                } else {
                                                    chatOtherUserId = chat.getUser1Id();
                                                }

                                                if (userId.equals(chatOtherUserId)) {
                                                    chat.setOtherUserName(fullName);
                                                    chat.setOtherUserProfileImage(profileImageUrl);
                                                }
                                            }
                                        }

                                        // Update adapter and visibility
                                        updateVisibility();
                                        chatAdapter.notifyDataSetChanged();
                                    });
                        } else {
                            updateVisibility();
                            chatAdapter.notifyDataSetChanged();
                        }
                    } else {
                        chatList.clear();
                        updateVisibility();
                        chatAdapter.notifyDataSetChanged();
                    }
                });

        // Fall back to loading followed users if no existing chats
        if (chatList.isEmpty()) {
            loadFollowedUsers();
        }
    }

    private void loadFollowedUsers() {
        db.collection("users").document(currentUserId)
                .collection("following")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
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
                                .whereIn(FieldPath.documentId(), followedUserIds)
                                .get()
                                .addOnSuccessListener(userSnapshots -> {
                                    for (DocumentSnapshot userDoc : userSnapshots) {
                                        // If no chats exist with this user, create a placeholder chat
                                        String userId = userDoc.getId();
                                        
                                        // Check if this user is already in the chat list
                                        boolean userExists = false;
                                        for (Chat chat : chatList) {
                                            if (chat.getUser1Id().equals(userId) || chat.getUser2Id().equals(userId)) {
                                                userExists = true;
                                                break;
                                            }
                                        }
                                        
                                        if (!userExists) {
                                            String firstName = userDoc.getString("firstName");
                                            String lastName = userDoc.getString("lastName");
                                            String fullName = (firstName != null ? firstName : "") + 
                                                    " " + (lastName != null ? lastName : "");
                                            String profileImageUrl = userDoc.getString("profileImageUrl");
                                            
                                            Chat newChat = new Chat(currentUserId + "_" + userId, currentUserId, userId);
                                            newChat.setLastMessage("No messages yet");
                                            newChat.setOtherUserName(fullName);
                                            newChat.setOtherUserProfileImage(profileImageUrl);
                                            newChat.setLastMessageSenderId(currentUserId);
                                            chatList.add(newChat);
                                        }
                                    }
                                    
                                    updateVisibility();
                                    chatAdapter.notifyDataSetChanged();
                                });
                    }
                });
    }

    private void updateVisibility() {
        if (chatList.isEmpty()) {
            rvChats.setVisibility(View.GONE);
            txtEmptyChats.setVisibility(View.VISIBLE);
        } else {
            rvChats.setVisibility(View.VISIBLE);
            txtEmptyChats.setVisibility(View.GONE);
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_messages);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MessagesActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_newPost) {
                startActivity(new Intent(MessagesActivity.this, NewPost.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_connections) {
                startActivity(new Intent(MessagesActivity.this, ConnectionsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_notification) {
                startActivity(new Intent(MessagesActivity.this, NotificationsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_messages) {
                // Already on messages screen, do nothing
                return true;
            }
            return false;
        });
    }
}