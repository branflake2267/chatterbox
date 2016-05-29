package com.gonevertical.chatterbox.group;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class DeleteGroupDialog extends DialogFragment {

    public interface DeleteDialogListener {
        void onDeleteOkDialog(GroupsActivity.GroupHolder groupHolder);

        void onDeleteCancelDialog(GroupsActivity.GroupHolder groupHolder);
    }

    public static DeleteGroupDialog newInstance(GroupsActivity.GroupHolder groupHolder) {
        DeleteGroupDialog fragment = new DeleteGroupDialog();
        fragment.setGroupHolder(groupHolder);

        return fragment;
    }

    private DeleteDialogListener mListener;
    private GroupsActivity.GroupHolder mGroupHolder;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = "Are you sure you want to delete the group " + mGroupHolder.getGroup().getName() + "?";

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDeleteOkDialog(mGroupHolder);
                dismiss();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDeleteCancelDialog(mGroupHolder);
                dismiss();
            }
        });
        return builder.create();
    }

    public void setGroupHolder(GroupsActivity.GroupHolder group) {
        mGroupHolder = group;
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
