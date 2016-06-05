package com.gonevertical.chatterbox.group;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.gonevertical.chatterbox.AppConstant;
import com.gonevertical.chatterbox.BaseActivity;
import com.gonevertical.chatterbox.MainActivity;
import com.gonevertical.chatterbox.R;
import com.gonevertical.chatterbox.room.RoomsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class GroupsActivity extends BaseActivity implements EditGroupDialog.EditGroupDialogListener, DeleteGroupDialog.DeleteDialogListener {

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, GroupsActivity.class);
        return in;
    }

    private static final String TAG = GroupsActivity.class.getSimpleName();

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayAdapter<String> mDrawerAdapter;

    private FirebaseRecyclerAdapter<Boolean, GroupHolder> mRecyclerViewAdapter;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createDrawer();

        createGroupsView();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddGroupDialog();
            }
        });

        doesDefaultGroupExist();
    }

    private void doesDefaultGroupExist() {
        // root/users/userkey/defaults/group/groupKey
        Query defaultGroupQuery = FirebaseDatabase.getInstance().getReference(AppConstant.DB_USERS).child(getUserKey()).child(AppConstant.DB_DEFAULTS).child(AppConstant.DB_GROUP);
        defaultGroupQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    createDefaultGroup();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "::doesDefaultGroupExist? " + databaseError.getMessage());
                // TODO
                Toast.makeText(GroupsActivity.this, "Oops Couldn't find default group", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createDefaultGroup() {
        createGroup("My Group", true);
    }


    /**
     * Link to user groups with reference
     *
     * @param groupKey
     */
    private void linkUserToGroup(final String groupKey, boolean defaultGroup) {
        // root/users/userkey/groups/groupkey/true
        FirebaseDatabase.getInstance().getReference(AppConstant.DB_USERS).child(getUserKey()).child(AppConstant.DB_GROUPS).child(groupKey).setValue(true);

        if (defaultGroup) {
            // root/users/userkey/defaults/group/groupKey
            FirebaseDatabase.getInstance().getReference(AppConstant.DB_USERS).child(getUserKey()).child(AppConstant.DB_DEFAULTS).child(AppConstant.DB_GROUP).setValue(groupKey);
        }
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

        // root/users/userKey/groups
        DatabaseReference drGroups = FirebaseDatabase.getInstance().getReference(AppConstant.DB_USERS).child(getUserKey()).child(AppConstant.DB_GROUPS);
        drGroups.addValueEventListener(new ValueEventListener() {
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

        mRecyclerViewAdapter = new FirebaseRecyclerAdapter<Boolean, GroupHolder>(Boolean.class, R.layout.group, GroupHolder.class, drGroups) {
            @Override
            public void populateViewHolder(final GroupHolder groupHolder, Boolean b, final int position) {
                final String groupKey = mRecyclerViewAdapter.getRef(position).getRef().getKey();

                groupHolder.setGroup(getSupportFragmentManager(), groupKey);
                groupHolder.setOnClickGroupHandler(new GroupHolder.GroupClickHandler() {
                    @Override
                    public void onGroupClick() {
                        navigateToRoom(groupKey);
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

    private void navigateToRoom(String groupKey) {
        Log.i(TAG, "navigateToRoom groupKey=" + groupKey);
        startActivity(RoomsActivity.createIntent(this, groupKey));
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

    private void showAreYouSureDialog(GroupHolder groupHolder) {
        DeleteGroupDialog deleteGroupDialog = DeleteGroupDialog.newInstance(groupHolder);
        deleteGroupDialog.show(getSupportFragmentManager(), "Delete Group Dialog Fragment");
    }

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
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onActionSettings() {
        Toast.makeText(GroupsActivity.this, "Settings Click", Toast.LENGTH_SHORT).show();
    }

    private void onActionSignout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(MainActivity.createIntent(GroupsActivity.this));
    }

    /**
     * Create a concrete group, then link it to the user.
     *
     * @param name         group name
     * @param defaultGroup for the first group only (maybe change this to the first item)
     */
    private void createGroup(String name, final boolean defaultGroup) {
        Group group = new Group(name);
        group.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // add concrete group, then link it to user
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference(AppConstant.DB_GROUPS);
        dr.push().setValue(group, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                String groupKey = databaseReference.getKey();
                if (databaseError == null) {
                    linkUserToGroup(groupKey, defaultGroup);
                } else {
                    // TODO
                    Log.e(TAG, "Error completing creating group.");
                }
            }
        });
    }

    /**
     * Finished adding.
     */
    @Override
    public void onFinishAddDialog(String groupName) {
        //Log.i(TAG, "groupName=" + groupName + " " + addGroup);
        createGroup(groupName, false);
    }

    /**
     * Finished editing.
     */
    @Override
    public void onFinishEditDialog(final String groupName, int adapterIndex) {
        DatabaseReference drGroupLink = mRecyclerViewAdapter.getRef(adapterIndex).getRef();
        drGroupLink.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String groupKey = dataSnapshot.getKey();
                // root/groups/groupKey/name
                FirebaseDatabase.getInstance().getReference(AppConstant.DB_GROUPS).child(groupKey).child("name").setValue(groupName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO
            }
        });
    }

    @Override
    public void onDeleteOkDialog(GroupHolder groupHolder) {
        Log.i(TAG, "delete Ok groupName=" + groupHolder.getGroup().getName());
        Toast.makeText(this, "Deleting group " + groupHolder.getGroup().getName(), Toast.LENGTH_LONG).show();


        // TODO delete group
        // TODO don't delete default group
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
