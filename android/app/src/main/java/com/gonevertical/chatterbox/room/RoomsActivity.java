package com.gonevertical.chatterbox.room;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.gonevertical.chatterbox.AppConstant;
import com.gonevertical.chatterbox.MainActivity;
import com.gonevertical.chatterbox.R;
import com.gonevertical.chatterbox.chat.ChatsActivity;
import com.gonevertical.chatterbox.group.GroupsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoomsActivity extends AppCompatActivity implements EditRoomDialog.AddRoomDialogListener, DeleteRoomDialog.DeleteDialogListener {

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, RoomsActivity.class);
        return in;
    }

    public static class RoomHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public interface RoomClickHandler {
            void onRoomClick();
        }

        private FragmentManager supportFragmentManager;
        private View mView;
        private Room room;
        private RoomClickHandler roomClickHandler;

        public RoomHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            mView = itemView;
        }

        public void setRoom(FragmentManager supportFragmentManager, Room room) {
            this.supportFragmentManager = supportFragmentManager;
            this.room = room;

            TextView field = (TextView) mView.findViewById(R.id.roomName);
            field.setText(room.getName());
        }

        @Override
        public void onClick(View v) {
            fireOnClickRoomEvent();
        }

        @Override
        public boolean onLongClick(View v) {
            Toast.makeText(v.getContext(), "Long: The Room is" + room.getName(), Toast.LENGTH_LONG).show();

            EditRoomDialog dialogFragment = EditRoomDialog.newInstance(getRoom().getName(), getAdapterPosition());
            dialogFragment.show(supportFragmentManager, "Edit Room Dialog Fragment");

            return false;
        }

        public Room getRoom() {
            return room;
        }

        public void setOnClickRoomHandler(RoomClickHandler roomClickHandler) {
            this.roomClickHandler = roomClickHandler;
        }

        protected void fireOnClickRoomEvent() {
            if (roomClickHandler != null) {
                roomClickHandler.onRoomClick();
            }
        }
    }

    private static final String TAG = RoomsActivity.class.getSimpleName();

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayAdapter<String> mDrawerAdapter;

    private DatabaseReference mDatabaseRefRooms;
    private FirebaseRecyclerAdapter<Room, RoomHolder> mRecyclerViewAdapter;
    private SwipeRefreshLayout swipeContainer;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initFirebase();

        createDrawer();

        createRoomsView();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddRoomDialog();
            }
        });
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();

        mDatabaseRefRooms = FirebaseDatabase.getInstance().getReference(AppConstant.DATABASE_ROOMS);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                updateUI(firebaseAuth);
            }
        });
    }

    private void updateUI(FirebaseAuth firebaseAuth) {

    }

    private void createRoomsView() {
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
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

        mDatabaseRefRooms.addValueEventListener(new ValueEventListener() {
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

        mRecyclerViewAdapter = new FirebaseRecyclerAdapter<Room, RoomHolder>(Room.class, R.layout.room, RoomHolder.class, mDatabaseRefRooms.getRef()) {
            @Override
            public void populateViewHolder(final RoomHolder roomHolder, Room room, final int position) {
                roomHolder.setRoom(getSupportFragmentManager(), room);
                roomHolder.setOnClickRoomHandler(new RoomHolder.RoomClickHandler() {
                    @Override
                    public void onRoomClick() {
                        DatabaseReference roomRef = mRecyclerViewAdapter.getRef(position);
                        navigateToChatActivity(roomRef);
                    }
                });
            }
        };

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.roomsList);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mRecyclerViewAdapter);
        recyclerView.refreshDrawableState();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Remove swiped item from list and notify the RecyclerView
                RoomHolder roomHolder = (RoomHolder) viewHolder;

                showAreYouSureDialog(roomHolder);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void createDrawer() {
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        addDrawerItems();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void addDrawerItems() {
        String[] osArray = {"Android", "iOS", "Windows", "OS X", "Linux"};
        mDrawerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mDrawerAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(RoomsActivity.this, "Time for an upgrade!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToChatActivity(DatabaseReference roomRef) {
        Intent intent = new Intent(getApplicationContext(), ChatsActivity.class);
        intent.putExtra(AppConstant.ROOM_KEY, roomRef.getKey());
        startActivity(intent);
    }

    private void showAreYouSureDialog(final RoomHolder roomHolder) {
        DeleteRoomDialog deleteRoomDialog = DeleteRoomDialog.newInstance(roomHolder);
        deleteRoomDialog.show(getSupportFragmentManager(), "Delete Room Dialog Fragment");
    }

    /**
     * guide: http://developer.android.com/guide/topics/ui/dialogs.html
     */
    private void showAddRoomDialog() {
        EditRoomDialog dialogFragment = EditRoomDialog.newInstance();
        dialogFragment.show(getSupportFragmentManager(), "Add Room Dialog Fragment");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rooms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                onSettings();
                break;
            case R.id.action_signout:
                onSignOut();
                break;
            case R.id.action_groups:
                onGroups();
                break;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onGroups() {
        startActivity(GroupsActivity.createIntent(this));
        finish();
    }

    private void onSettings() {
        // TODO
    }

    private void onSignOut() {
        mAuth.signOut();
        startActivity(MainActivity.createIntent(RoomsActivity.this));
        finish();
    }

    /**
     * Finishing editing dialog
     */
    @Override
    public void onFinishAddDialog(String roomName) {
        //Log.i(TAG, "roomName=" + roomName + " " + addRoom);
        Room room = new Room(roomName);
        mDatabaseRefRooms.push().setValue(room);
    }

    @Override
    public void onFinishEditDialog(String roomName, int adapterIndex) {
        DatabaseReference roomRef = mRecyclerViewAdapter.getRef(adapterIndex);
        roomRef.child("name").setValue(roomName);
    }

    @Override
    public void onDeleteOkDialog(RoomHolder roomHolder) {
        Log.i(TAG, "delete Ok roomName=" + roomHolder.getRoom().getName());
        Toast.makeText(this, "Deleting room " + roomHolder.getRoom().getName(), Toast.LENGTH_LONG).show();

        // TODO delete room
    }

    @Override
    public void onDeleteCancelDialog(RoomHolder roomHolder) {
        Log.i(TAG, "delete Cancel roomName=" + roomHolder.getRoom().getName());
        Toast.makeText(this, "Delete canceled " + roomHolder.getRoom().getName(), Toast.LENGTH_LONG).show();

        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerViewAdapter.cleanup();
    }
}
