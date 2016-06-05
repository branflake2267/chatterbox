package com.gonevertical.chatterbox.room;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gonevertical.chatterbox.AppConstant;
import com.gonevertical.chatterbox.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoomHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private DatabaseReference drRoom;

    public interface RoomClickHandler {
        void onRoomClick();
    }

    private final static String TAG = RoomHolder.class.getSimpleName();

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

    public void setRoom(FragmentManager supportFragmentManager, String roomKey) {
        this.supportFragmentManager = supportFragmentManager;

        if (drRoom == null) {
            drRoom = FirebaseDatabase.getInstance().getReference(AppConstant.DB_ROOMS).child(roomKey);
            drRoom.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "setRoom onDataChange dataSnapshot=" + dataSnapshot);

                    if (dataSnapshot.exists()) {
                        Room room = dataSnapshot.getValue(Room.class);
                        renderRoom(room);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO
                }
            });
        }
    }

    private void renderRoom(Room room) {
        this.room = room;

        TextView field = (TextView) mView.findViewById(R.id.roomName);
        field.setText(room.getName());
    }

    public Room getRoom() {
        return room;
    }

    @Override
    public void onClick(View v) {
        fireOnClickRoomEvent();
    }

    @Override
    public boolean onLongClick(View v) {
        EditRoomDialog dialogFragment = EditRoomDialog.newInstance(room.getName(), getAdapterPosition());
        dialogFragment.show(supportFragmentManager, "Edit Room Dialog Fragment");

        return false;
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