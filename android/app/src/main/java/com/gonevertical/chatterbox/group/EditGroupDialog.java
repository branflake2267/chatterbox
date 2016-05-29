package com.gonevertical.chatterbox.group;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.gonevertical.chatterbox.R;

/**
 * http://developer.android.com/guide/topics/ui/dialogs.html
 */
public class EditGroupDialog extends DialogFragment {

    public interface AddGroupDialogListener {
        void onFinishAddDialog(String groupName);

        void onFinishEditDialog(String groupName, int adapterIndex);
    }

    public static EditGroupDialog newInstance() {
        Bundle args = new Bundle();
        args.putString(PARAM_ROOM_NAME, "");
        args.putBoolean(PARAM_ADD_ROOM, true);

        EditGroupDialog fragment = new EditGroupDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public static EditGroupDialog newInstance(String groupName, int adapterIndex) {
        Bundle args = new Bundle();
        args.putString(PARAM_ROOM_NAME, groupName);
        args.putBoolean(PARAM_ADD_ROOM, false);
        args.putInt(PARAM_ADAPTER_INDEX, adapterIndex);

        EditGroupDialog fragment = new EditGroupDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public static String PARAM_ROOM_NAME = "groupName";
    public static String PARAM_ADD_ROOM = "addGroup";
    public static String PARAM_ADAPTER_INDEX = "mAdapterIndex";

    private AddGroupDialogListener mListener;

    private boolean mAddGroup = false;
    private int mAdapterIndex = -1;

    /**
     * Empty constructor required for DialogFragment
     */
    public EditGroupDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.fragment_edit_group, null);

        setRetainInstance(true);

        String groupName = getArguments().getString(PARAM_ROOM_NAME, "");
        mAdapterIndex = getArguments().getInt(PARAM_ADAPTER_INDEX, -1);
        mAddGroup = getArguments().getBoolean(PARAM_ADD_ROOM, false);

        final EditText editGroupName = (EditText) rootView.findViewById(R.id.editGroupName);
        editGroupName.setText(groupName);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = editGroupName.getText().toString();

                if (mAddGroup) {
                    mListener.onFinishAddDialog(groupName);
                } else {
                    mListener.onFinishEditDialog(groupName, mAdapterIndex);
                }

                dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AddGroupDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement " + AddGroupDialogListener.class.getSimpleName());
        }
    }
}
