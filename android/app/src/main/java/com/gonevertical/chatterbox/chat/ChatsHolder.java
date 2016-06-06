package com.gonevertical.chatterbox.chat;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.gonevertical.chatterbox.R;

public class ChatsHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private FragmentManager supportFragmentManager;
    private View mView;
    private Chat chat;

    public ChatsHolder(View itemView) {
        super(itemView);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        mView = itemView;
    }

    public void setChat(FragmentManager supportFragmentManager, Chat chat) {
        this.supportFragmentManager = supportFragmentManager;
        this.chat = chat;

        TextView chatMessage = (TextView) mView.findViewById(R.id.chatMessage);
        chatMessage.setText(chat.getMessage());

        TextView chatAuthor = (TextView) mView.findViewById(R.id.chatAuthor);
        chatAuthor.setText(chat.getAuthor());
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

}