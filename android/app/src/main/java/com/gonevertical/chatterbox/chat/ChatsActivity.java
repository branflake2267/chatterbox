package com.gonevertical.chatterbox.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.gonevertical.chatterbox.AppConstant;
import com.gonevertical.chatterbox.BaseActivity;
import com.gonevertical.chatterbox.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatsActivity extends BaseActivity {

    public static Intent createIntent(Context context, String groupKey, String roomKey) {
        Intent in = new Intent();
        in.setClass(context, ChatsActivity.class);
        in.putExtra(AppConstant.GROUP_KEY, groupKey);
        in.putExtra(AppConstant.ROOM_KEY, roomKey);
        return in;
    }

    public static class ChatsHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
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

            TextView field = (TextView) mView.findViewById(R.id.chatMessage);
            field.setText(chat.getMessage());
        }

        @Override
        public void onClick(View v) {
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }

        public Chat getChat() {
            return chat;
        }
    }

    private static final String TAG = ChatsActivity.class.getSimpleName();

    private DatabaseReference mQueryRoomChats;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Chat, ChatsHolder> mRecyclerViewAdapter;
    private SwipeRefreshLayout swipeContainer;
    private EditText chatMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String roomKey = getIntent().getStringExtra(AppConstant.ROOM_KEY);
        initFirebase(roomKey);

        setContentView(R.layout.activity_chats);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.chatsSwipeContainer);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                mRecyclerViewAdapter.notifyDataSetChanged();

                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeContainer.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        mQueryRoomChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeContainer.setRefreshing(false);
                    }
                }, 500);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                swipeContainer.setRefreshing(false);
            }
        });

        mRecyclerViewAdapter = new FirebaseRecyclerAdapter<Chat, ChatsHolder>(Chat.class, R.layout.chats, ChatsHolder.class, mQueryRoomChats) {
            @Override
            public void populateViewHolder(ChatsHolder holder, Chat chat, int position) {
                holder.setChat(getSupportFragmentManager(), chat);
            }
        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(false);
        layoutManager.setStackFromEnd(true);
        //layoutManager.setSmoothScrollbarEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.chatsList);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mRecyclerViewAdapter);
        recyclerView.refreshDrawableState();

        Button chatSendButton = (Button) findViewById(R.id.chatSendButton);
        chatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createChat();
            }
        });

        chatMessage = (EditText) findViewById(R.id.chatMessage);
    }

    private void createChat() {
        String message = chatMessage.getText().toString();

        Chat chat = new Chat();
        chat.setMessage(message);
        mQueryRoomChats.push().setValue(chat);

        chatMessage.setText("");
        
        int position = mRecyclerViewAdapter.getItemCount();
        recyclerView.scrollToPosition(position);
    }

    private void initFirebase(String roomKey) {
        DatabaseReference mDatabaseRefMessages = FirebaseDatabase.getInstance().getReference(AppConstant.DATABASE_MESSSAGES);
        mQueryRoomChats = mDatabaseRefMessages.child(roomKey).child(AppConstant.DATABASE_CHATS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerViewAdapter.cleanup();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getGroupKey() {
        return getIntent().getStringExtra(AppConstant.GROUP_KEY);
    }

}
