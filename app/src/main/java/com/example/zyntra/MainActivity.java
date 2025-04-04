package com.example.zyntra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvHomeFeed;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FirebaseFirestore db;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private DocumentSnapshot lastVisible = null;
    private static final int PAGE_SIZE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get the FCM token
                    String token = task.getResult();
                    Log.d("FCM", "FCM Token: " + token);

                    // Save this token to Firestore under the user's profile
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .update("fcmToken", token);
                });

        DrawerLayout drawerLayout = findViewById(R.id.main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        setupBottomNav();

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Settings Unavailable", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        rvHomeFeed = findViewById(R.id.rvHomeFeed);
        rvHomeFeed.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        rvHomeFeed.setAdapter(postAdapter);

        db = FirebaseFirestore.getInstance();

        loadInitialPosts();

        rvHomeFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadMorePosts();
                    }
                }
            }
        });
    }

    private void loadInitialPosts() {
        isLoading = true;
        db.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        postList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                postList.add(post);
                            }
                        }
                        postAdapter.notifyDataSetChanged();

                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        if (queryDocumentSnapshots.size() < PAGE_SIZE) {
                            isLastPage = true;
                        }
                    } else {
                        isLastPage = true;
                    }
                    isLoading = false;
                });
    }

    private void loadMorePosts() {
        if (isLoading || isLastPage || lastVisible == null) return;

        isLoading = true;
        db.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(PAGE_SIZE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                postList.add(post);
                            }
                        }
                        postAdapter.notifyDataSetChanged();

                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        if (queryDocumentSnapshots.size() < PAGE_SIZE) {
                            isLastPage = true;
                        }
                    } else {
                        isLastPage = true;
                    }
                    isLoading = false;
                });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_search) {
                startActivity(new Intent(MainActivity.this, ActivitySearch.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_newPost) {
                startActivity(new Intent(MainActivity.this, NewPost.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_connections) {
                startActivity(new Intent(MainActivity.this, ConnectionsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_notification) {
                startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
