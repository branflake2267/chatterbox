package com.gonevertical.chatterbox;

import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by branflake2267 on 5/30/16.
 */
public class BaseActivity extends AppCompatActivity {

    protected String getUserKey() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
