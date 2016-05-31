package com.gonevertical.chatterbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends BaseActivity {

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, MainActivity.class);
        return in;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}
