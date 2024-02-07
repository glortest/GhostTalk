package com.glortest.messenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.glortest.messenger.databinding.RecentChannelBinding;
import com.glortest.messenger.listeners.ChannelListener;
import com.glortest.messenger.models.ChannelModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class RecentChannelsAdapter extends RecyclerView.Adapter<RecentChannelsAdapter.RecentChatViewHolder>{
    private final Context context;
    private final List<ChannelModel> chatMessages;
    private final ChannelListener conversationListener;

    public RecentChannelsAdapter(Context context, List<ChannelModel> chatMessages, ChannelListener conversationListener) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public RecentChannelsAdapter.RecentChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentChannelsAdapter.RecentChatViewHolder(
                RecentChannelBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecentChannelsAdapter.RecentChatViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class RecentChatViewHolder extends RecyclerView.ViewHolder{
        private final RecentChannelBinding binding;
        public RecentChatViewHolder(RecentChannelBinding recentChannelBinding){
            super(recentChannelBinding.getRoot());
            binding = recentChannelBinding;
        }

        private void setData(ChannelModel chatMessage){
            Glide.with(context).load(chatMessage.avatar).into(binding.recentUserImageProfile);
            binding.recentUserName.setText(chatMessage.conversionName);
            if(chatMessage.message != null)
                binding.recentUserMessage.setText(chatMessage.message);
            else
                binding.recentUserMessage.setText("Канал создан");
            if(chatMessage.dateObject != null)
                binding.recentUserDateTime.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(chatMessage.dateObject));
            binding.getRoot().setOnClickListener(view -> {
                ChannelModel user = new ChannelModel();
                user.username = chatMessage.username;
                user.name = chatMessage.conversionName;
                user.avatar = chatMessage.avatar;
                user.admins = chatMessage.admins;
                user.members = chatMessage.members;
                user.bio = chatMessage.bio;
                conversationListener.onChannelConversationClicked(user);
            });
        }
    }
}