package com.gonevertical.chatterbox.group;

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
import com.gonevertical.chatterbox.room.RoomsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupsActivity extends AppCompatActivity implements EditGroupDialog.AddGroupDialogListener, DeleteGroupDialog.DeleteDialogListener {

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, GroupsActivity.class);
        return in;
    }

    public static class GroupHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public interface GroupClickHandler {
            void onGroupClick();
        }

        private FragmentManager supportFragmentManager;
        private View mView;
        private Group group;
        private GroupClickHandler groupClickHandler;

        public GroupHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            mView = itemView;
        }

        public void setGroup(FragmentManager supportFragmentManager, Group group) {
            this.supportFragmentManager = supportFragmentManager;
            this.group = group;

            TextView field = (TextView) mView.findViewById(R.id.groupName);
            field.setText(group.getName());
        }

        @Override
        public void onClick(View v) {
            fireOnClickGroupEvent();
        }

        @Override
        public boolean onLongClick(View v) {
            Toast.makeText(v.getContext(), "Long: The Group is" + group.getName(), Toast.LENGTH_LONG).show();

            EditGroupDialog dialogFragment = EditGroupDialog.newInstance(getGroup().getName(), getAdapterPosition());
            dialogFragment.show(supportFragmentManager, "Edit Group Dialog Fragment");

            return false;
        }

        public Group getGroup() {
            return group;
        }

        public void setOnClickGroupHandler(GroupClickHandler groupClickHandler) {
            this.groupClickHandler = groupClickHandler;
        }

        protected void fireOnClickGroupEvent() {
            if (groupClickHandler != null) {
                groupClickHandler.onGroupClick();
            }
        }
    }

    private static final String TAG = GroupsActivity.class.getSimpleName();

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayAdapter<String> mDrawerAdapter;

    private DatabaseReference mDatabaseRefGroups;
    private FirebaseRecyclerAdapter<Group, GroupHolder> mRecyclerViewAdapter;
    private SwipeRefreshLayout swipeContainer;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initFirebase();

        createDrawer();

        createGroupsView();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddGroupDialog();
            }
        });
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();

        mDatabaseRefGroups = FirebaseDatabase.getInstance().getReference(AppConstant.DATABASE_GROUPS);

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

    private void createGroupsView() {
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

        mDatabaseRefGroups.addValueEventListener(new ValueEventListener() {
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

        mRecyclerViewAdapter = new FirebaseRecyclerAdapter<Group, GroupHolder>(Group.class, R.layout.group, GroupHolder.class, mDatabaseRefGroups.getRef()) {
            @Override
            public void populateViewHolder(final GroupHolder groupHolder, Group group, final int position) {
                groupHolder.setGroup(getSupportFragmentManager(), group);
                groupHolder.setOnClickGroupHandler(new GroupHolder.GroupClickHandler() {
                    @Override
                    public void onGroupClick() {
                        DatabaseReference groupRef = mRecyclerViewAdapter.getRef(position);

                    }
                });
            }
        };

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.groupsList);
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
                GroupHolder groupHolder = (GroupHolder) viewHolder;

                showAreYouSureDialog(groupHolder);
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
                Toast.makeText(GroupsActivity.this, "Time for an upgrade!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAreYouSureDialog(final GroupHolder groupHolder) {
        DeleteGroupDialog deleteGroupDialog = DeleteGroupDialog.newInstance(groupHolder);
        deleteGroupDialog.show(getSupportFragmentManager(), "Delete Group Dialog Fragment");
    }

    /**
     * guide: http://developer.android.com/guide/topics/ui/dialogs.html
     */
    private void showAddGroupDialog() {
        EditGroupDialog dialogFragment = EditGroupDialog.newInstance();
        dialogFragment.show(getSupportFragmentManager(), "Add Group Dialog Fragment");
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
        getMenuInflater().inflate(R.menu.menu_groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                onActionSettings();
                break;
            case R.id.action_signout:
                onActionSignout();
                break;
            case R.id.action_rooms:
                onActionRooms();
                break;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onActionRooms() {
        startActivity(RoomsActivity.createIntent(this));
        finish();
    }

    private void onActionSettings() {
        Toast.makeText(GroupsActivity.this, "Settings Click", Toast.LENGTH_SHORT).show();
    }

    private void onActionSignout() {
        mAuth.signOut();
        startActivity(MainActivity.createIntent(GroupsActivity.this));
        finish();
    }

    /**
     * Finishing editing dialog
     */
    @Override
    public void onFinishAddDialog(String groupName) {
        //Log.i(TAG, "groupName=" + groupName + " " + addGroup);
        Group group = new Group(groupName);
        mDatabaseRefGroups.push().setValue(group);
    }

    @Override
    public void onFinishEditDialog(String groupName, int adapterIndex) {
        DatabaseReference groupRef = mRecyclerViewAdapter.getRef(adapterIndex);
        groupRef.child("name").setValue(groupName);
    }

    @Override
    public void onDeleteOkDialog(GroupHolder groupHolder) {
        Log.i(TAG, "delete Ok groupName=" + groupHolder.getGroup().getName());
        Toast.makeText(this, "Deleting group " + groupHolder.getGroup().getName(), Toast.LENGTH_LONG).show();

        // TODO delete group
    }

    @Override
    public void onDeleteCancelDialog(GroupHolder groupHolder) {
        Log.i(TAG, "delete Cancel groupName=" + groupHolder.getGroup().getName());
        Toast.makeText(this, "Delete canceled " + groupHolder.getGroup().getName(), Toast.LENGTH_LONG).show();

        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerViewAdapter.cleanup();
    }
}