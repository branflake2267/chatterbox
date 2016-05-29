package com.gonevertical.chatterbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.gonevertical.chatterbox.group.GroupsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SignInActivity extends AppCompatActivity {

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, SignInActivity.class);
        return in;
    }

    private static final String TAG = SignInActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private View mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Views
        mRootView = findViewById(R.id.sign_in);

        // Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setTheme(AuthUI.getDefaultTheme())
                        .setProviders(getSelectedProviders())
                        //.setTosUrl(getSelectedTosUrl())
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }

        showSnackbar(R.string.unknown_response);
    }

    private void handleSignInResponse(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
           onAuthSuccess();
            return;
        }

        if (resultCode == RESULT_CANCELED) {
            showSnackbar(R.string.sign_in_cancelled);
            return;
        }

        showSnackbar(R.string.unknown_sign_in_response);
    }

    @Override
    public void onStart() {
        super.onStart();

        // If logged in, send them to the next activity
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess();
        }
    }

    private String[] getSelectedProviders() {
        ArrayList<String> selectedProviders = new ArrayList<>();

        //selectedProviders.add(AuthUI.EMAIL_PROVIDER);
        selectedProviders.add(AuthUI.FACEBOOK_PROVIDER);
        selectedProviders.add(AuthUI.GOOGLE_PROVIDER);

        return selectedProviders.toArray(new String[selectedProviders.size()]);
    }

    private void onAuthSuccess() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Toast.makeText(SignInActivity.this, "Sign In: Success " + currentUser.getDisplayName(), Toast.LENGTH_LONG).show();

        // TODO create group

        startActivity(GroupsActivity.createIntent(this));
        finish();
    }

    private void showSnackbar(int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

}
