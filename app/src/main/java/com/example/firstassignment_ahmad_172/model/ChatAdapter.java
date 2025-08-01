package com.example.firstassignment_ahmad_172.model;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.firstassignment_ahmad_172.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;
    private List<ChatMessage> messages;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if (message.getImageUrl() != null) {
            return message.isUserMessage() ? 3 : 4; // 3 = User Image, 4 = Bot Image
        }
        return message.isUserMessage() ? 1 : 2; // 1 = User Text, 2 = Bot Text
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) { // User Text Message
            view = LayoutInflater.from(context).inflate(R.layout.item_user_message, parent, false);
        } else if (viewType == 2) { // Bot Text Message
            view = LayoutInflater.from(context).inflate(R.layout.item_bot_message, parent, false);
        } else if (viewType == 3) { // User Image Message
            view = LayoutInflater.from(context).inflate(R.layout.item_user_message, parent, false);
        } else { // Bot Image Message (viewType == 4)
            view = LayoutInflater.from(context).inflate(R.layout.item_bot_message, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (message.getImageUrl() != null) {
            // Show Image, Hide Text
            holder.imageMessage.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.GONE);
            Glide.with(context).load(Uri.parse(message.getImageUrl())).into(holder.imageMessage);
        } else {
            // Show Text, Hide Image
            holder.imageMessage.setVisibility(View.GONE);
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(Html.fromHtml(message.getMessage())); // Supports HTML formatting
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ImageView imageMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            imageMessage = itemView.findViewById(R.id.image_message);
        }
    }
}
