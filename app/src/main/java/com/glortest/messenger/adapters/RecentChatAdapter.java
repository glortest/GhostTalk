package com.glortest.messenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.glortest.messenger.databinding.RecentUserBinding;
import com.glortest.messenger.listeners.ConversationListener;
import com.glortest.messenger.models.ChatMessage;
import com.glortest.messenger.models.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class RecentChatAdapter extends RecyclerView.Adapter<RecentChatAdapter.RecentChatViewHolder>{
    private final Context context;
    private final List<ChatMessage> chatMessages;
    private final ConversationListener conversationListener;

    public RecentChatAdapter(Context context, List<ChatMessage> chatMessages, ConversationListener conversationListener) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public RecentChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentChatViewHolder(
                RecentUserBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecentChatViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class RecentChatViewHolder extends RecyclerView.ViewHolder{
        private final RecentUserBinding binding;
        public RecentChatViewHolder(RecentUserBinding recentUserBinding){
            super(recentUserBinding.getRoot());
            binding = recentUserBinding;
        }

        private void setData(ChatMessage chatMessage){
            Glide.with(context).load(chatMessage.conversionImage).into(binding.recentUserImageProfile);
            binding.recentUserName.setText(chatMessage.conversionName);
            binding.recentUserMessage.setText(chatMessage.message);;
            binding.getRoot().setOnClickListener(view -> {
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                conversationListener.onConversationClicked(user);
            });
        }
    }
}

