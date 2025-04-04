package com.example.zyntra;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ConnectionsActivity extends AppCompatActivity {

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
    }
    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_search) {
                startActivity(new Intent(ConnectionsActivity.this, ActivitySearch.class));
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
            return false;
        });
    }
}