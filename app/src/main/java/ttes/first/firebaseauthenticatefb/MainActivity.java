package ttes.first.firebaseauthenticatefb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="FacebookLogin";
    private static final int SIGNIN_CODE=12345;
    private CallbackManager cb_Manager;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try
        {
            PackageInfo info=getPackageManager().getPackageInfo("ttes.first.firebaseauthenticatefb", PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures)
            {
                MessageDigest md= MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }

        }
        catch (NoSuchAlgorithmException e)
        {}
            catch(PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
            }

//        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);
        fAuth=FirebaseAuth.getInstance();
        cb_Manager=CallbackManager.Factory.create();
        LoginButton loginButton=findViewById(R.id.login_button);
        loginButton.setReadPermissions("email","public_profile");
        loginButton.registerCallback(cb_Manager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG,"Facebook:onSuccess:"+loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG,"Facebook:onCancle");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG,"Facebook:Error");
            }
        });
    }
    private void handleFacebookAccessToken(AccessToken token)
    {
        Log.d(TAG,"HandlerFacebookAccessToken:"+token);
        AuthCredential credential= FacebookAuthProvider.getCredential(token.getToken());
        fAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Log.d(TAG,"SignInWithCredential:success");
                    FirebaseUser user=fAuth.getCurrentUser();
                    Toast.makeText(MainActivity.this,"Authentication Success:",Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        Log.d(TAG,"SignInWithCredential:failure",task.getException());
                        Toast.makeText(MainActivity.this,"Authentication failed:",Toast.LENGTH_SHORT).show();

                    }
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        cb_Manager.onActivityResult(requestCode,resultCode,data);
    }
    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseUser user=fAuth.getCurrentUser();
        if(user!=null)
        {
            Log.d(TAG,"Currently Signed in:"+user.getEmail());
            Toast.makeText(MainActivity.this,"Currently Logged in:"+user.getEmail(),Toast.LENGTH_SHORT).show();
        }
    }
}
