package com.example.zyntra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupBottomNav();
        RecyclerView rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        List<NotificationModel> notificationList = new ArrayList<>();
        NotificationAdapter adapter = new NotificationAdapter(this, notificationList);
        rvNotifications.setAdapter(adapter);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("notifications")
                .document(currentUserId)
                .collection("user_notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        notificationList.clear();
                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                            NotificationModel notification = snapshot.toObject(NotificationModel.class);
                            notificationList.add(notification);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

    }
    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_messages) {
                startActivity(new Intent(NotificationsActivity.this, MessagesActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_newPost) {
                startActivity(new Intent(NotificationsActivity.this, NewPost.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_connections) {
                startActivity(new Intent(NotificationsActivity.this, ConnectionsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (itemId == R.id.nav_home) {
                startActivity(new Intent(NotificationsActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}