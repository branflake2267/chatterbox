package com.gonevertical.chatterbox.group;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.gonevertical.chatterbox.AppConstant;
import com.gonevertical.chatterbox.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public interface GroupClickHandler {
        void onGroupClick();
    }

    private final static String TAG = GroupHolder.class.getSimpleName();

    private DatabaseReference drGroup;
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

    public void setGroup(FragmentManager supportFragmentManager, String groupKey) {
        this.supportFragmentManager = supportFragmentManager;

        if (drGroup == null) {
            drGroup = FirebaseDatabase.getInstance().getReference(AppConstant.DB_GROUPS).child(groupKey);
            drGroup.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Log.i(TAG, "setGroup onDataChange dataSnapshot=" + dataSnapshot);

                    if (dataSnapshot.exists()) {
                        Group group = dataSnapshot.getValue(Group.class);
                        renderGroup(group);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO
                }
            });
        }
    }

    private void renderGroup(Group group) {
        this.group = group;

        TextView field = (TextView) mView.findViewById(R.id.groupName);
        field.setText(group.getName());
    }

    public Group getGroup() {
        return group;
    }

    @Override
    public void onClick(View v) {
        fireOnClickGroupEvent();
    }

    @Override
    public boolean onLongClick(View v) {
        EditGroupDialog dialogFragment = EditGroupDialog.newInstance(group.getName(), getAdapterPosition());
        dialogFragment.show(supportFragmentManager, "Edit Group Dialog Fragment");

        return false;
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