package com.gonevertical.chatterbox.invite;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gonevertical.chatterbox.AppConstant;
import com.gonevertical.chatterbox.BaseActivity;
import com.gonevertical.chatterbox.MainActivity;
import com.gonevertical.chatterbox.R;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InviteResponseGroupActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = InviteResponseGroupActivity.class.getSimpleName();

    private TextView inviteGroupText;
    private Button inviteGroupHomeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_response_group);

        inviteGroupText = (TextView) findViewById(R.id.inviteGroupText);
        inviteGroupHomeBtn = (Button) findViewById(R.id.inviteGroupHomeBtn);

        inviteGroupHomeBtn.setVisibility(View.INVISIBLE);

        handleDeepLink();

        inviteGroupHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MainActivity.createIntent(InviteResponseGroupActivity.this);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if the intent contains an AppInvite and then process the referral information.
        Intent intent = getIntent();
        if (AppInviteReferral.hasReferral(intent)) {
            processReferralIntent(intent);
        }
    }

    private void handleDeepLink() {
        // Build GoogleApiClient with AppInvite API for receiving deep links
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();

        // Check if this app was launched from a deep link. Setting autoLaunchDeepLink to true
        // would automatically launch the deep link if one is found.
        boolean autoLaunchDeepLink = false;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(@NonNull AppInviteInvitationResult result) {
                                Intent intent = result.getInvitationIntent();

                                // TODO trying to figure out a way to test this.
                                // adb shell am start -W -a android.intent.action.VIEW -d "https://gonevertical.com/chatterbox/invite/group/-KJnkQfRjZfAH9-U_U4a?invitation_id=20832144509-9642991a-de62-4d40-ba93-b991208c2d31" com.gonevertical.chatterbox
                                Uri data = getIntent().getData();
                                if (data != null && data.getQueryParameter("invitation_id") != null) {
                                    String link = data.getQueryParameter("invitation_id");

                                    Log.i(TAG, "link=" + link);
                                }

                                if (result.getStatus().isSuccess()) {
                                    // Extract information from the intent
                                    //Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    String invitationId = AppInviteReferral.getInvitationId(intent);

                                    onInviteSuccess(deepLink, invitationId);
                                } else {
                                    onInviteCancelled();
                                }
                            }
                        });
    }


    // [START process_referral_intent]
    private void processReferralIntent(Intent intent) {
        // Extract referral information from the intent
        String invitationId = AppInviteReferral.getInvitationId(intent);
        String deepLink = AppInviteReferral.getDeepLink(intent);

        // Display referral information
        // [START_EXCLUDE]
        Log.d(TAG, "Found Referral: " + invitationId + ":" + deepLink);
        // [END_EXCLUDE]
    }

    private void onInviteSuccess(String deepLink, String invitationId) {
        Log.i(TAG, "onInviteSuccess(): deepLink=" + deepLink + " inviteId=" + invitationId);

        String text = "Loading...";
        inviteGroupText.setText(text);

        DatabaseReference drref = FirebaseDatabase.getInstance().getReference(AppConstant.DB_INVITES).child(invitationId).child(AppConstant.DB_GROUP);
        drref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildren() != null) {
                    DataSnapshot dsgroupKey = dataSnapshot.getChildren().iterator().next();
                    String groupKey = dsgroupKey.getKey();
                    String groupName = (String) dsgroupKey.child("name").getValue();

                    onInviteSuccessFoundInvite(groupKey, groupName);
                } else {
                    onInviteSuccessInviteMissing();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "database error=" + databaseError.getMessage());

                onInviteCancelled();
            }
        });
    }

    private void onInviteSuccessFoundInvite(String groupKey, String groupName) {
        String text = "Would you like to join the group '" + groupName + "'?";
        inviteGroupText.setText(text);

        // TODO enable login
    }

    private void onInviteCancelled() {
        Log.i(TAG, "onInviteCancelled(): The invite was cancelled.");

        String text = "The invite was canceled.";
        inviteGroupText.setText(text);

        // enable button to go home
        inviteGroupHomeBtn.setVisibility(View.VISIBLE);
    }

    private void onInviteSuccessInviteMissing() {
        Log.i(TAG, "onInviteSuccessInviteMissing(): Missing invitation id");

        String text = "You were invited to a group and it's already been taken care of.";
        inviteGroupText.setText(text);

        // enable button to home
        inviteGroupHomeBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed(): message=" + connectionResult.getErrorMessage());
    }

}
