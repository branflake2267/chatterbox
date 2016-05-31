package com.gonevertical.chatterbox.room;

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
public class EditRoomDialog extends DialogFragment {

    public interface EditRoomDialogListener {
        void onFinishAddDialog(String roomName);

        void onFinishEditDialog(String roomName, int adapterIndex);
    }

    public static EditRoomDialog newInstance() {
        Bundle args = new Bundle();
        args.putString(PARAM_ROOM_NAME, "");
        args.putBoolean(PARAM_ADD_ROOM, true);

        EditRoomDialog fragment = new EditRoomDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public static EditRoomDialog newInstance(String roomName, int adapterIndex) {
        Bundle args = new Bundle();
        args.putString(PARAM_ROOM_NAME, roomName);
        args.putBoolean(PARAM_ADD_ROOM, false);
        args.putInt(PARAM_ADAPTER_INDEX, adapterIndex);

        EditRoomDialog fragment = new EditRoomDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public static String PARAM_ROOM_NAME = "roomName";
    public static String PARAM_ADD_ROOM = "addRoom";
    public static String PARAM_ADAPTER_INDEX = "mAdapterIndex";

    private EditRoomDialogListener mListener;

    private boolean mAddRoom = false;
    private int mAdapterIndex = -1;

    /**
     * Empty constructor required for DialogFragment
     */
    public EditRoomDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.fragment_edit_room, null);

        setRetainInstance(true);

        String roomName = getArguments().getString(PARAM_ROOM_NAME, "");
        mAdapterIndex = getArguments().getInt(PARAM_ADAPTER_INDEX, -1);
        mAddRoom = getArguments().getBoolean(PARAM_ADD_ROOM, false);

        final EditText editRoomName = (EditText) rootView.findViewById(R.id.editRoomName);
        editRoomName.setText(roomName);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String roomName = editRoomName.getText().toString();

                if (mAddRoom) {
                    mListener.onFinishAddDialog(roomName);
                } else {
                    mListener.onFinishEditDialog(roomName, mAdapterIndex);
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
            mListener = (EditRoomDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement " + EditRoomDialogListener.class.getSimpleName());
        }
    }
}
