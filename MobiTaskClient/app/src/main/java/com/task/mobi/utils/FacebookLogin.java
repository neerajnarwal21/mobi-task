package com.task.mobi.utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.task.mobi.R;

import org.json.JSONObject;

import java.util.Arrays;


/**
 * Created by Neeraj Narwal on 9/8/16.
 */
public class FacebookLogin extends AppCompatActivity {
    private CallbackManager callbackmanager;
    private GraphRequest request;
    private LoginButton fbLoginButton;
    private SocialLogin socialLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.social_facebook_login);
        socialLogin = SocialLogin.getInstance();
        callbackmanager = CallbackManager.Factory.create();
        fbLoginButton = (LoginButton) findViewById(R.id.fbLoginButton);
        fbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFacebook();
            }
        });
        fbLoginButton.callOnClick();
    }

    public void initFacebook() {
        fbLoginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().logOut();
        fbLoginButton.registerCallback(callbackmanager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        socialLogin.facebookLoginDone(object, response);
                        finish();
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                log("Facebook login cancel");
                finish();
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                log("Facebook error :" + error.toString());
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackmanager.onActivityResult(requestCode, resultCode, data);
    }

    private void log(String s) {
        Utils.debugLog("Facebook Login : >> ", s);
    }
}