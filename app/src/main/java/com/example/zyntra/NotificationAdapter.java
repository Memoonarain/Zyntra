package com.example.zyntra;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final Context context;
    private final List<NotificationModel> notificationList;

    public NotificationAdapter(Context context, List<NotificationModel> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);
        holder.txtSenderName.setText(notification.getTitle());
        holder.txtNotificationBody.setText(notification.getMessage());

        // Convert timestamp to readable date
        String formattedTime = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
               .format(new Date(notification.getTimeStamp())); // No split
        holder.txtNotificationTime.setText(formattedTime);

        holder.itemView.setOnClickListener(v -> {
            if (notification.getPostId() != null && !notification.getPostId().isEmpty()) {
                Intent intent = new Intent(context, PostActivity.class);
                intent.putExtra("postId", notification.getPostId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSenderName, txtNotificationBody, txtNotificationTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSenderName = itemView.findViewById(R.id.txtSenderName);
            txtNotificationBody = itemView.findViewById(R.id.txtNotificationBody);
            txtNotificationTime = itemView.findViewById(R.id.txtNotificationTime);
        }
    }
}
