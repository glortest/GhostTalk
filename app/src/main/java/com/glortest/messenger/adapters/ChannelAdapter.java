package com.glortest.messenger.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.glortest.messenger.activities.ImageWatchActivity;
import com.glortest.messenger.databinding.ContainerReceivedImageBinding;
import com.glortest.messenger.databinding.ContainerReceivedMessageBinding;
import com.glortest.messenger.models.ChatMessage;
import com.glortest.messenger.utilities.Constants;

import java.util.List;


public class ChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final List<ChatMessage> chatMessages;
    private String receiverProfileImage;
    private final String senderId;
    private final Context context;

    public void setReceiverProfileImage(String imageUri){
        receiverProfileImage = imageUri;
    }
    public ChannelAdapter(List<ChatMessage> chatMessages, String receiverProfileImage, String senderId, Context context) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Constants.VIEW_TYPE_RECEIVED) {
            return new ChannelAdapter.ReceivedMessageViewHolder(
                    ContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else{
            return new ChannelAdapter.ReceivedImageViewHolder(
                    ContainerReceivedImageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == Constants.VIEW_TYPE_RECEIVED) {
            ((ChannelAdapter.ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage, context);
        }else{
            ((ChannelAdapter.ReceivedImageViewHolder) holder).setData(chatMessages.get(position),receiverProfileImage, context);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).messageType.equals("text")) {
            return Constants.VIEW_TYPE_RECEIVED;
        } else{
            return Constants.VIEW_TYPE_IMAGE_RECEIVED;
        }
    }


    public static class ReceivedImageViewHolder extends RecyclerView.ViewHolder{
        private final ContainerReceivedImageBinding binding;

        public ReceivedImageViewHolder(ContainerReceivedImageBinding containerReceivedMessageBinding){
            super(containerReceivedMessageBinding.getRoot());
            binding = containerReceivedMessageBinding;
        }
        private void setData(ChatMessage chatMessage, String receiverProfileImage, Context context){
            Glide.with(context).load(chatMessage.image).into(binding.containerMessageText);

            binding.containerMessageDateTime.setText(chatMessage.dateTime);
            if (receiverProfileImage != null){
                Glide.with(context).load(receiverProfileImage).into(binding.containerImageProfile);
            }
            binding.containerMessageText.setOnClickListener(view -> {
                Intent imageUser = new Intent(context, ImageWatchActivity.class);
                imageUser.putExtra(Constants.ARGUMENT_IMAGE_PROFILE, chatMessage.image);
                context.startActivity(imageUser);
            });
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ContainerReceivedMessageBinding containerReceivedMessageBinding){
            super(containerReceivedMessageBinding.getRoot());
            binding = containerReceivedMessageBinding;
        }
        private void setData(ChatMessage chatMessage, String receiverProfileImage, Context context){
            binding.containerMessageText.setText(chatMessage.message);
            binding.containerMessageDateTime.setText(chatMessage.dateTime);
            if (receiverProfileImage != null){
                Glide.with(context).load(receiverProfileImage).into(binding.containerImageProfile);
            }
        }
    }
}

