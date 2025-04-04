package com.example.zyntra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ActivitySearch extends AppCompatActivity {
    private EditText searchBar;
    private ImageView searchIcon;
    private RecyclerView rvPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        searchBar = findViewById(R.id.searchBar);
        searchIcon = findViewById(R.id.search_icon);
        rvPosts = findViewById(R.id.rvPosts);

        // Setup RecyclerView
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        rvPosts.setAdapter(postAdapter);

        db = FirebaseFirestore.getInstance();

        setupBottomNav();

        // Click listener for search icon
        searchIcon.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            if (!query.isEmpty()) {
                Toast.makeText(this, "Searching for "+query, Toast.LENGTH_SHORT).show();
                searchPosts(query);
            } else {
                Toast.makeText(this, "Enter something to search", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchPosts(String query) {
        db.collection("Posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();  // Clear old results
                    boolean found = false;
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        if (snapshot.contains("postText")) {  // Check if postText exists
                            String postText = snapshot.getString("postText");
                            if (postText != null && postText.toLowerCase().contains(query.toLowerCase())) {
                                Post post = snapshot.toObject(Post.class);
                                postList.add(post);
                                post.setPostId(snapshot.getId());
                                found = true;
                            }
                        }
                    }
                    if (found) {
                        Toast.makeText(this, "Matching Search Result Found!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No Matching Search Result!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("SearchActivity", "Error fetching posts", e));
    }


    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ActivitySearch.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_newPost) {
                startActivity(new Intent(ActivitySearch.this, NewPost.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_connections) {
                startActivity(new Intent(ActivitySearch.this, ConnectionsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_notification) {
                startActivity(new Intent(ActivitySearch.this, NotificationsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
