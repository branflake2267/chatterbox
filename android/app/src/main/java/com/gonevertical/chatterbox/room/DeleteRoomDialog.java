package com.gonevertical.chatterbox.room;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class DeleteRoomDialog extends DialogFragment {

    public interface DeleteDialogListener {
        void onDeleteOkDialog(RoomHolder roomHolder);

        void onDeleteCancelDialog(RoomHolder roomHolder);
    }

    public static DeleteRoomDialog newInstance(RoomHolder roomHolder) {
        DeleteRoomDialog fragment = new DeleteRoomDialog();
        fragment.setRoomHolder(roomHolder);

        return fragment;
    }

    private DeleteDialogListener mListener;
    private RoomHolder mRoomHolder;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = "Are you sure you want to delete the room " + mRoomHolder.getRoom().getName() + "?";

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDeleteOkDialog(mRoomHolder);
                dismiss();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDeleteCancelDialog(mRoomHolder);
                dismiss();
            }
        });
        return builder.create();
    }

    public void setRoomHolder(RoomHolder room) {
        mRoomHolder = room;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DeleteDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement " + DeleteDialogListener.class.getSimpleName());
        }
    }

}