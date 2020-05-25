package com.mad.p03.np2020.routine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener{

    EditText et_Email, et_Password;
    Button btn_register;
    Button btn_login;
    String TAG = "LOGIN_ACITVITY";
    FirebaseAuth mAuth;
    String email, password;
    TextView txtError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.i(TAG, "user at login page");
        mAuth = FirebaseAuth.getInstance();

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        et_Email = findViewById(R.id.editEmail);
        et_Password = findViewById(R.id.editPassword);
        btn_login = findViewById(R.id.buttonLogin);
        btn_register = findViewById(R.id.buttonRegister);
        txtError = findViewById(R.id.txtError);

        et_Email.setOnClickListener(this);
        et_Password.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        btn_register.setOnClickListener(this);

        et_Email.setOnFocusChangeListener(this);
        et_Password.setOnFocusChangeListener(this);

        et_Email.setOnKeyListener(this);
        et_Password.setOnKeyListener(this);
    }

    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editEmail:
                ShowKeyboard(et_Email);
                break;
            case R.id.editPassword:
                ShowKeyboard(et_Password);
                break;
            case R.id.buttonLogin:
                email = et_Email.getText().toString();
                password = et_Password.getText().toString();
                firebaseAuthWithGoogle(email, password);
                break;
            case R.id.buttonRegister:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
        }
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.editEmail:
                if (!hasFocus) {
                    HideKeyboard(et_Email);
                }
                break;
            case R.id.editPassword:
                if (!hasFocus) {
                    HideKeyboard(et_Password);
                }
                break;
        }
    }

    //Soft Keyboard methods
    private void ShowKeyboard(EditText taskInput) {
        Log.i(TAG, "Show soft keyboard");
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert mgr != null;
        mgr.showSoftInput(taskInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void firebaseAuthWithGoogle(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            txtError.setVisibility(View.VISIBLE);
                        }

                    }
                });
    }

    private void HideKeyboard(EditText taskInput) {
        Log.i(TAG, "Hide soft keyboard");
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert mgr != null;
        mgr.hideSoftInputFromWindow(taskInput.getWindowToken(), 0);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.i(TAG, event.toString());
        switch (v.getId()) {
            case R.id.editEmail:
                if (keyCode==KeyEvent.KEYCODE_ENTER) {
                    Log.i(TAG,"Enter is clicked");
                    HideKeyboard(et_Email);
                    break;
                }
            case R.id.editPassword:
                if (keyCode==KeyEvent.KEYCODE_ENTER) {
                    Log.i(TAG,"Enter is clicked");
                    HideKeyboard(et_Password);
                    break;
                }
        }
        return true;
    }
}
