package com.servicehub.adapter;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.servicehub.model.Message;
import java.util.List;
import app.hub.R;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;
    private static final int VIEW_TYPE_TYPING = 3;

    private final List<Message> messages;

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.isTypingIndicator()) {
            return VIEW_TYPE_TYPING;
        } else if (message.isSentByUser()) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_AI;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new MessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_TYPING) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_typing, parent, false);
            return new TypingViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessageViewHolder) {
            ((MessageViewHolder) holder).bind(messages.get(position));
        } else if (holder instanceof TypingViewHolder) {
            ((TypingViewHolder) holder).startAnimation();
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        void bind(Message message) {
            messageTextView.setText(message.getText());
        }
    }

    static class TypingViewHolder extends RecyclerView.ViewHolder {
        private final View dot1;
        private final View dot2;
        private final View dot3;

        public TypingViewHolder(@NonNull View itemView) {
            super(itemView);
            dot1 = itemView.findViewById(R.id.typingDot1);
            dot2 = itemView.findViewById(R.id.typingDot2);
            dot3 = itemView.findViewById(R.id.typingDot3);
        }

        void startAnimation() {
            // Animate dot 1
            ObjectAnimator anim1 = ObjectAnimator.ofFloat(dot1, "alpha", 0.4f, 1f, 0.4f);
            anim1.setDuration(600);
            anim1.setRepeatCount(ObjectAnimator.INFINITE);
            anim1.setStartDelay(0);
            anim1.start();

            // Animate dot 2
            ObjectAnimator anim2 = ObjectAnimator.ofFloat(dot2, "alpha", 0.4f, 1f, 0.4f);
            anim2.setDuration(600);
            anim2.setRepeatCount(ObjectAnimator.INFINITE);
            anim2.setStartDelay(200);
            anim2.start();

            // Animate dot 3
            ObjectAnimator anim3 = ObjectAnimator.ofFloat(dot3, "alpha", 0.4f, 1f, 0.4f);
            anim3.setDuration(600);
            anim3.setRepeatCount(ObjectAnimator.INFINITE);
            anim3.setStartDelay(400);
            anim3.start();
        }
    }
}
