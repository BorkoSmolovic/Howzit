/*
This file is part of Howzit.
Copyright (C) 2020-2021 Mauka-Makai team: Nickolas Naydenov, Borko Smolovic, Iacopo Marri,
Viking Forsman, Matteo Cecini, Erblin Isaku, Alessandro Bertulli

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; version 2.1.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program; if not, see <https://www.gnu.org/licenses>.

Linking Howzit statically or dynamically with other modules is making a combined work
based on Howzit. Thus, the terms and conditions of the GNU Lesser General Public License
cover the whole combination.

In addition, as a special exception, the copyright holders of Howzit give you permission to combine
Howzit with free software programs or libraries that are released under the GNU LGPL and with code
included in the standard release of AndroidX under the Apache v.2.0 License
(or modified versions of such code, with unchanged license). You may copy and distribute such a system
following the terms of the GNU LGPL for Howzit and the licenses of the other code concerned.

Note that people who make modified versions of Howzit are not obligated to grant this special exception
for their modified versions; it is their choice whether to do so. The GNU General Public License
gives permission to release a modified version without this exception; this exception
also makes it possible to release a modified version which carries forward this exception.

In addition, as a special exception, the copyright holders of Howzit give you permission to combine
Howzit with free software programs or libraries that are released under the GNU LGPL and with code
included in the standard release of Material Components for Android under the Apache v.2.0 License
(or modified versions of such code, with unchanged license). You may copy and distribute such a system
following the terms of the GNU LGPL for Howzit and the licenses of the other code concerned.

Note that people who make modified versions of Howzit are not obligated to grant this special exception
for their modified versions; it is their choice whether to do so. The GNU General Public License
gives permission to release a modified version without this exception; this exception
also makes it possible to release a modified version which carries forward this exception.

In addition, as a special exception, the copyright holders of Howzit give you permission to combine
Howzit with free software programs or libraries that are released under the GNU LGPL and with code
included in the standard release of Lottie under the Apache v.2.0 License (or modified versions
of such code, with unchanged license). You may copy and distribute such a system following the terms
of the GNU LGPL for Howzit and the licenses of the other code concerned.

Note that people who make modified versions of Howzit are not obligated to grant this special exception
for their modified versions; it is their choice whether to do so. The GNU General Public License
gives permission to release a modified version without this exception; this exception
also makes it possible to release a modified version which carries forward this exception.
 */
package com.example.howzit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import static com.example.howzit.SignUp.convertFromBytes;
import static com.example.howzit.SignUp.decodeFile;
import static com.example.howzit.SignUp.generateKey;

/**
 * Activity to sign in. It is the main entry point of the application
 */
public class SignIn extends AppCompatActivity {
    /**
     * Username text field
     */
    private AutoCompleteTextView editTextUsername;

    /**
     * Password text field
     */
    private EditText editTextPassword;
    private LottieAnimationView encryptionAnimation;
    private LottieAnimationView checkmarkAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        encryptionAnimation = findViewById(R.id.encryptionAnimation);
        checkmarkAnimation = findViewById(R.id.checkmarkAnimation);


        if(true){

                File f = new File("qwe.txt");
                String s = f.getAbsolutePath();
                System.out.println("ttt"+s);
                //FileInputStream f = new FileInputStream(new File("qwe.txt"));

            return;
        }

        // Give suggestions on existing user names
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getCurrentUsers());
        editTextUsername.setAdapter(adapter);

        // Redirect to contact list if signed in
        redirectIfSignedIn(this);
    }

    private void redirectIfSignedIn(Context context){
        // Check if user is defined
        User u = ((LoggedUser) this.getApplication()).getLoggedUser();
        if(u != null){
            Toast.makeText(context,"welcome " + u.getUsername(),Toast.LENGTH_LONG).show();
            Intent myIntent = new Intent(SignIn.this, ContactsActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(myIntent);
            finish();
        }
    }

    public void playEncryptionAnimation(){
        encryptionAnimation.setVisibility(View.VISIBLE);
        encryptionAnimation.setProgress(0);
        encryptionAnimation.bringToFront();
        encryptionAnimation.playAnimation();
    }

    public void stopEncryptionAnimation(){
        encryptionAnimation.setVisibility(View.INVISIBLE);
        encryptionAnimation.pauseAnimation();
    }

    public void startSuccessAnimation(){
        checkmarkAnimation.bringToFront();
        checkmarkAnimation.setProgress(0);
        checkmarkAnimation.setVisibility(View.VISIBLE);
        checkmarkAnimation.playAnimation();
    }

    public User userLogin(String username, String password) {
        User u = null;

        try {
            //decrypting storage and extracting user class
            String filePath = this.getFilesDir() + "/" + username;
            File file = new File( filePath );
            byte[] fileBytes = new byte[(int) file.length()];
            BufferedInputStream bis = new BufferedInputStream(this.openFileInput(username));
            bis.read(fileBytes);
            byte[] yourKey = generateKey(password);
            byte[] decodedData = decodeFile(yourKey, fileBytes);
            u = (User) convertFromBytes(decodedData);

            //if account is not found we notify user
            if(!u.getPassword().equals(password)){
                return null;
            }
        }catch (FileNotFoundException e){
            Log.e("SignInException1", e.toString());
            return null;
        }catch (Exception ex){
            Log.e("SignInException2", ex.toString());
            return null;
        }
        //save logged user globally
        return u;
    }

    public void signIn(View view) {
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        Boolean userExsists = Arrays.asList(getCurrentUsers()).contains(username);

        if(!userExsists) {
            Toast.makeText(this, "Username or password is incorrect!", Toast.LENGTH_LONG).show();
            return;
        } else {
            SignInAsyncTask s = new SignInAsyncTask(this);
            s.execute(username, password);
        }
    }

    private class SignInAsyncTask extends AsyncTask<String, Void, User> {
        private WeakReference<SignIn> activityWeakReference;
        private User user = null;
        SignInAsyncTask(SignIn activity) {
            activityWeakReference = new WeakReference<SignIn>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SignIn activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            playEncryptionAnimation();
        }

        @Override
        protected User doInBackground(String... strings) {
            String username = strings[0];
            String password = strings[1];
            user = userLogin(username, password);
            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            SignIn activity = activityWeakReference.get();

            if (activity == null || activity.isFinishing()) {
                return;
            } else if(user != null) {
                //startSuccessAnimation();
                ((LoggedUser) activity.getApplication()).setLoggedUser(user);
                Toast.makeText(activity,"welcome " + user.getUsername(),Toast.LENGTH_LONG).show();
                Intent myIntent = new Intent(SignIn.this, ContactsActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(myIntent);
                finish();
            }else{
                Toast.makeText(activity,"Username or password is incorrect!",Toast.LENGTH_LONG).show();
                editTextPassword.setText("");
                stopEncryptionAnimation();
            }
        }
    }

    /**
     * Starts the {@link SignUp} activity, via the usual Intent mechanism.
     * It is bounded to the relative Button from the XML layout file
     *
     * @param view
     */
    public void signUp(View view) {
        Intent myIntent = new Intent(SignIn.this, SignUp.class);
        startActivity(myIntent);
    }

    public String[] getCurrentUsers(){
        String path = this.getFilesDir().toString();
        File directory = new File(path);
        File[] files = directory.listFiles();

        String[] users = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            users[i] = files[i].getName();
        }
        return users;
    }

    public void toggleVisibility(View view) {
        int hidden = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        int visible = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        int id = view.getId();

        EditText editText;
        if(id == R.id.toggleVisibility1) {
            editText = editTextPassword;
        } else {
            Log.e("visible", "Unknown caller: " + getResources().getResourceName(view.getId()));
            return;
        }

        String password = editText.getText().toString();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (editText.getInputType() == hidden) {
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,R.drawable.ic_baseline_visibility_24,0);
            editText.setInputType(visible);
        } else {
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,R.drawable.ic_baseline_visibility_off_24,0);
            editText.setInputType(hidden);
        }
        editText.setTypeface(Typeface.DEFAULT);
        editText.setText(password);
        editText.setSelection(start,end);
    }
}