package com.glortest.messenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.glortest.messenger.R;
import com.glortest.messenger.databinding.SubscriberBinding;
import com.glortest.messenger.listeners.SubListener;
import com.glortest.messenger.models.User;

import java.util.List;


public class SubscribersAdapter  extends RecyclerView.Adapter<SubscribersAdapter.RecentChatViewHolder>{
    private final Context context;
    private final List<User> chatMessages;
    private final SubListener subListener;

    public SubscribersAdapter(Context context, List<User> chatMessages, SubListener subListener) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.subListener = subListener;
    }

    @NonNull
    @Override
    public SubscribersAdapter.RecentChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubscribersAdapter.RecentChatViewHolder(
                SubscriberBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SubscribersAdapter.RecentChatViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class RecentChatViewHolder extends RecyclerView.ViewHolder{
        private final SubscriberBinding binding;
        public RecentChatViewHolder(SubscriberBinding recentChannelBinding){
            super(recentChannelBinding.getRoot());
            binding = recentChannelBinding;
        }

        private void setData(User chatMessage){
            Glide.with(context).load(chatMessage.image).into(binding.userImageProfile);
            binding.userName.setText(chatMessage.name);
            binding.userPhoneNumber.setText(chatMessage.username);
            if(chatMessage.admins.contains(chatMessage.username)){
                binding.userSetAdmin.setColorFilter(binding.getRoot().getResources().getColor(R.color.text_hint));
            }else{
                binding.userSetAdmin.setColorFilter(binding.getRoot().getResources().getColor(R.color.icon_orange));
            }
            if(!chatMessage.admins.contains(chatMessage.username)) {
                binding.userSetAdmin.setOnClickListener(view -> {
                    subListener.onConversationClicked(chatMessage, "admin");
                });
            }else{
                binding.userSetAdmin.setOnClickListener(view -> {
                    subListener.onConversationClicked(chatMessage, "unadmin");
                });
            }
            binding.userKickFromChannel.setOnClickListener(view -> {
                subListener.onConversationClicked(chatMessage, "kick");
            });
        }
    }
}