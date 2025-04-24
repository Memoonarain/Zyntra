package com.example.zyntra;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chatId;
    private String otherUserId;
    private String currentUserId;
    
    private RecyclerView rvMessages;
    private EditText edtMessage;
    private ImageButton btnSendMessage;
    private ImageView btnBack;
    private CircleImageView imgUserProfilePic;
    private TextView txtUserName;
    
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Get chat ID and other user ID from intent
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        
        // Initialize views
        rvMessages = findViewById(R.id.rvMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnBack = findViewById(R.id.btnBack);
        imgUserProfilePic = findViewById(R.id.imgUserProfilePic);
        txtUserName = findViewById(R.id.txtUserName);
        
        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUserId);
        rvMessages.setAdapter(messageAdapter);
        
        // Set up click listeners
        btnBack.setOnClickListener(v -> finish());
        
        btnSendMessage.setOnClickListener(v -> {
            String messageText = edtMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
                edtMessage.setText("");
            }
        });
        
        // Load chat details and messages
        loadOtherUserInfo();
        checkOrCreateChat();
        loadMessages();
    }
    
    private void loadOtherUserInfo() {
        db.collection("users").document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String fullName = (firstName != null ? firstName : "") + 
                                " " + (lastName != null ? lastName : "");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        
                        txtUserName.setText(fullName);
                        
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(ChatActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.icon_profile)
                                    .into(imgUserProfilePic);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error loading user info", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void checkOrCreateChat() {
        db.collection("chats").document(chatId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Chat doesn't exist yet, create it
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("chatId", chatId);
                        chatData.put("user1Id", currentUserId);
                        chatData.put("user2Id", otherUserId);
                        chatData.put("lastMessage", "");
                        chatData.put("lastMessageSenderId", "");
                        chatData.put("seen", true);
                        
                        // Add participants map for easy querying
                        Map<String, Boolean> participants = new HashMap<>();
                        participants.put(currentUserId, true);
                        participants.put(otherUserId, true);
                        chatData.put("participants", participants);
                        
                        db.collection("chats").document(chatId)
                                .set(chatData)
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ChatActivity.this, "Error creating chat", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Update seen status if there are unseen messages from the other user
                        String lastMessageSenderId = documentSnapshot.getString("lastMessageSenderId");
                        boolean seen = documentSnapshot.getBoolean("seen") != null ? 
                                documentSnapshot.getBoolean("seen") : true;
                        
                        if (lastMessageSenderId != null && lastMessageSenderId.equals(otherUserId) && !seen) {
                            db.collection("chats").document(chatId)
                                    .update("seen", true);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error checking chat", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadMessages() {
        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    
                    if (value != null) {
                        messageList.clear();
                        
                        for (QueryDocumentSnapshot document : value) {
                            Message message = document.toObject(Message.class);
                            
                            // Mark received messages as seen
                            if (message.getSenderId().equals(otherUserId) && !message.isSeen()) {
                                db.collection("chats").document(chatId)
                                        .collection("messages")
                                        .document(message.getMessageId())
                                        .update("seen", true);
                            }
                            
                            messageList.add(message);
                        }
                        
                        messageAdapter.notifyDataSetChanged();
                        
                        if (messageList.size() > 0) {
                            rvMessages.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }
    
    private void sendMessage(String messageText) {
        DocumentReference messageRef = db.collection("chats").document(chatId)
                .collection("messages").document();
        
        String messageId = messageRef.getId();
        
        Message message = new Message(messageId, chatId, currentUserId, otherUserId, messageText);
        
        messageRef.set(message)
                .addOnSuccessListener(aVoid -> {
                    // Update the chat with the last message
                    Map<String, Object> chatUpdate = new HashMap<>();
                    chatUpdate.put("lastMessage", messageText);
                    chatUpdate.put("lastMessageSenderId", currentUserId);
                    chatUpdate.put("lastMessageTimestamp", FieldValue.serverTimestamp());
                    chatUpdate.put("seen", false);
                    
                    db.collection("chats").document(chatId)
                            .update(chatUpdate)
                            .addOnFailureListener(e -> {
                                Toast.makeText(ChatActivity.this, "Error updating chat", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                });
    }
} 