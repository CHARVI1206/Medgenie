package com.example.medgenie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private final List<ChatMessage> chatMessages;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        return message.getSender().equals("user") ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
            return new AIViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvUserMessage.setText(message.getMessage());
        } else {
            ((AIViewHolder) holder).tvAIMessage.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // ViewHolder for User Message
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserMessage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
        }
    }

    // ViewHolder for AI Message
    static class AIViewHolder extends RecyclerView.ViewHolder {
        TextView tvAIMessage;

        public AIViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAIMessage = itemView.findViewById(R.id.userChat);
        }
    }
}
