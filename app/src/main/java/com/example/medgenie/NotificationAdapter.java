package com.example.medgenie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationItem> notificationList;

    public NotificationAdapter(List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem notification = notificationList.get(position);

        holder.title.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());
        holder.timestamp.setText(notification.getTimestamp());

        // Optional: Change icon based on type
        holder.icon.setImageResource(R.drawable.ic_notification);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, timestamp;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iconNotification);
            title = itemView.findViewById(R.id.textTitle);
            message = itemView.findViewById(R.id.textMessage);
            timestamp = itemView.findViewById(R.id.textTimestamp);
        }
    }
}
