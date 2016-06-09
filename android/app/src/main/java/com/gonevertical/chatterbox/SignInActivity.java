package com.gonevertical.chatterbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.gonevertical.chatterbox.group.GroupsActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends BaseActivity {

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, SignInActivity.class);
        return in;
    }

    private static final String TAG = SignInActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Log.i(TAG, "starting sign in");

        // If logged in, send them to the next activity
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            signedIn();
        } else {
            startSignIn();
        }
    }

    private void startSignIn() {
        FirebaseApp firebaseApp = FirebaseApp.getInstance();
        startActivityForResult(AuthUI.getInstance(firebaseApp)
                        .createSignInIntentBuilder()
                        .setProviders(
                                AuthUI.GOOGLE_PROVIDER,
                                AuthUI.FACEBOOK_PROVIDER)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult .. resultcode=" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user is signed in!
                signedIn();
            } else {
                // user is not signed in. Maybe just wait for the user to press
                // "sign in" again, or show a message
            }
        }
    }

    private void signedIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Toast.makeText(SignInActivity.this, "Sign In: Success " + currentUser.getDisplayName(), Toast.LENGTH_LONG).show();

        Log.i(TAG, "signedIn " + currentUser.getUid());

        startActivity(GroupsActivity.createIntent(this));
        finish();
    }

}
