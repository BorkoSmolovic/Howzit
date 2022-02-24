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

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.howzit.messages.QueryMessage;
import com.example.howzit.messages.SignUpMessage;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SignUp extends AppCompatActivity {

    // GUI variables
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextPasswordConfirm;
    private TextView hint1;
    private TextView hint2;
    private TextView textViewSignup;
    private LottieAnimationView loadingAnimation;
    private LottieAnimationView signInAnimation;

    // User variables
    private Key publicKey = null;
    private Key privateKey = null;
    String IP;
    int ID;
    byte[] QR;
    User u = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordConfirm = findViewById(R.id.editTextPasswordConfirm);

        textViewSignup = findViewById(R.id.textViewSignIn);
        loadingAnimation = (LottieAnimationView)findViewById(R.id.loadingAnimation);
        loadingAnimation.bringToFront();
        loadingAnimation.setProgress(0);
        signInAnimation = (LottieAnimationView)findViewById(R.id.signupAnimation);


        hint1 = findViewById(R.id.hint1);
        hint2 = findViewById(R.id.hint2);

        // Change color and hint text depending on password strength
        PasswordStrengthEstimator estimator1 = new PasswordStrengthEstimator();
        editTextPassword.addTextChangedListener(estimator1);
        estimator1.strengthLevel.observe(this, strengthLevel ->
                displayStrengthLevel(strengthLevel, editTextPassword, hint1));

        // Change color and hint text depending on confirm password strength
        PasswordStrengthEstimator estimator2 = new PasswordStrengthEstimator();
        editTextPasswordConfirm.addTextChangedListener(estimator2);
        estimator2.strengthLevel.observe(this, strengthLevel ->
                displayStrengthLevel(strengthLevel, editTextPasswordConfirm, hint2));

        // Get user IP
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        try {
            IP = InetAddress.getByAddress(
                    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array())
                    .getHostAddress();
        } catch (UnknownHostException e) {
            Toast.makeText(this,"Sign up require access to the device IP!",Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    /**
     * Changes the color of the password field.
     *
     * @param strengthLevel Should be "Strong", "Medium" or "Weak"
     * @param editText      The password field
     * @param textView      {@code textView} to show the strength level
     */
    private void displayStrengthLevel(String strengthLevel, EditText editText, TextView textView) {
        if(strengthLevel.equals("Strong")){
            textView.setTextColor(ContextCompat.getColor(this, R.color.strong_password));
            textView.setText(strengthLevel);
            editText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.strong_password), PorterDuff.Mode.SRC_IN);
        } else if(strengthLevel.equals("Medium")){
            textView.setTextColor(ContextCompat.getColor(this, R.color.medium_password));
            textView.setText(strengthLevel);
            editText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.medium_password), PorterDuff.Mode.SRC_IN);
        } else if(strengthLevel.equals("Weak")) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.weak_password));
            textView.setText(strengthLevel);
            editText.getBackground().setColorFilter(ContextCompat.getColor(this,R.color.weak_password), PorterDuff.Mode.SRC_IN);
        } else{
            textView.setTextColor(ContextCompat.getColor(this, R.color.invalid_password));
            textView.setText(strengthLevel);
            editText.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.invalid_password), PorterDuff.Mode.SRC_IN);
        }
    }

    public void startLoadingAnimation(){
        textViewSignup.setVisibility(View.INVISIBLE);
        loadingAnimation.setVisibility(View.VISIBLE);
        signInAnimation.setClickable(false);
        loadingAnimation.playAnimation();
    }

    public void stopLoadingAnimation(){
        loadingAnimation.setVisibility(View.INVISIBLE);
        textViewSignup.setVisibility(View.VISIBLE);
        signInAnimation.setClickable(true);
        loadingAnimation.pauseAnimation();
    }


    /**
     * Do the actual sign up process, and redirects to {@link SignIn} activity.
     * Generates a ID for the user, checks the entered fields, including the password,
     * performs the actual registration. If everything went well it starts the {@link SignIn} activity.
     *
     * @param view
     */
    public void signUp(View view) {
        //startLoadingAnimation();

        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        String passwordConfirm = editTextPasswordConfirm.getText().toString();
        File file = new File(this.getFilesDir(), username);

        //generate random ID for the user, from 0 to 10000. next step is to ask the discovery server
        // double random = Math.random() * 9999 + 1;
        // int ID = (int)random;


        if(username.isEmpty()){
            Toast.makeText(this,"Please enter a username!",Toast.LENGTH_LONG).show();
            stopLoadingAnimation();
        } else if(file.exists() && !file.isDirectory()){
            Toast.makeText(this,"Username '" + username + "' has already been taken. Please choose another one.",Toast.LENGTH_LONG).show();
            stopLoadingAnimation();
        } else if(password.isEmpty() || passwordConfirm.isEmpty()) {
            Toast.makeText(this,"Please enter a password!",Toast.LENGTH_LONG).show();
            stopLoadingAnimation();
        } else if(!password.equals(passwordConfirm)){
            Toast.makeText(this,"Password confirmation did not match",Toast.LENGTH_LONG).show();
            stopLoadingAnimation();
        } else if(password.length()<6){
            Toast.makeText(this,"Password must contain at least six characters",Toast.LENGTH_LONG).show();
            stopLoadingAnimation();
        } else {
            // If the new user is successfully created go to sign in activity
            CreateUserAsyncTask task = new CreateUserAsyncTask(this);
            task.execute(username,password,IP);
        }
    }


    public void toggleVisibility(View view) {
        int hidden = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        int visible = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        int id = view.getId();

        EditText editText;
        if(id == R.id.toggleVisibility1) {
            editText = editTextPassword;
        } else if (id == R.id.toggleVisibility2) {
            editText = editTextPasswordConfirm;
        } else {
            Log.e("visible", "Unknown caller: " + view.getId() );
            return;
        }

        String password = editText.getText().toString();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if(editText.getInputType() == hidden) {
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

/* -------------- Functions related to user creation -------------- */

    private class CreateUserAsyncTask extends AsyncTask<String, Void, User> {
        private WeakReference<SignUp> activityWeakReference;
        Boolean userWasCreated;

        CreateUserAsyncTask(SignUp activity) {
            activityWeakReference = new WeakReference<SignUp>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SignUp activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            startLoadingAnimation();
        }

        @Override
        protected User doInBackground(String... strings) {
            String username = strings[0];
            String password = strings[1];
            String IP = strings[2];
            return createUser(username,password,IP);
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            SignUp activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            } else if (user != null) {
                // Clear input in edit text to prevent color bug
                editTextUsername.setText("");
                editTextPassword.setText("");
                editTextPasswordConfirm.setText("");

                // Inform user that account was successfully created
                Toast.makeText(activity,"Registration successful",Toast.LENGTH_LONG).show();
                Intent myIntent = new Intent(SignUp.this, SignIn.class);
                startActivity(myIntent);
                stopLoadingAnimation();
                finish();
            }else{
                Toast.makeText(activity,"Registration unsuccessful",Toast.LENGTH_LONG).show();
                stopLoadingAnimation();
            }
        }
    }

    public User createUser(String username, String password, String IP){
        Key publicKey;
        Key privateKey;
        byte[] QR;

        // Create a keypair
        long  timeKeypair =   System.nanoTime();
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
        } catch (Exception ex) {
            Toast.makeText(this,"Could not create user keys",Toast.LENGTH_LONG).show();
            Log.e("CreateError", "Error while creating keys");
            return null;
        }
        Log.e("TIME", "timeKeypair: " + (System.nanoTime() - timeKeypair)/1000000);

        // Create a user id
        /*long  timeUserId =   System.nanoTime();
        double random = Math.random() * 9999 + 1;
        int ID = (int)random;
        Log.e("TIME", "timeUserId: " + (timeUserId - System.nanoTime())/1000000);*/

        SignUpMessage signUpMessage = new SignUpMessage("");
        SendToServer signup = new SendToServer(signUpMessage);
        Thread signUpThread = new Thread(signup);
        signUpThread.start();

        try{
            //wait for query thread to end and get reply
            System.out.println("WAITING FOR SIGN UP THREAD TO FINISH");
            signUpThread.join();
            System.out.println("SIGN UP THREAD FINISHED");
        } catch(Exception e){ e.printStackTrace(); }
        String test = signup.getReply();
        try {
            //get the ip from the query reply
            ID = Integer.parseInt(test);
        } catch (NumberFormatException e) {
            Log.e("Sign Up ID parsing Error", test);
            return null;
        }


        // Request QR code from API
        long  timeQr =   System.nanoTime();
        //converting key to String
        byte[] publicKeyBytesNew = publicKey.getEncoded();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKeyBytesNew);

        try{
            //Setup connection
            URL url = new URL("https://api.qrserver.com/v1/create-qr-code/?color=000000&bgcolor=FFFFFF&data="
                    //This is the string that should be translated. It must be escaped
                    + Integer.toString(ID)
                    + "-"
                    + IP
                    + "-"
                    + publicKeyString +
                    "&qzone=1&margin=0&size=400x400&ecc=L");
            URLConnection connection = url.openConnection();
            int responseLength = connection.getContentLength();
            DataInputStream stream =new DataInputStream(url.openStream());
            byte[] buffer = new byte[responseLength];
            stream.readFully(buffer);
            stream.close();
            QR = buffer;
        } catch (MalformedURLException e){
            e.printStackTrace();
            return null;
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }

        //initialize new user with available data
        long  timeUser =   System.nanoTime();
        User newUser = new User(ID,username,password,privateKey,publicKey,QR);
        //TODO: this is only for testing until add contact is made, remove once done
        Contact contact1 =new Contact("My Phone", 0,"192.168.1.13", true, null); //static ip of my phone
        Contact contact2 =new Contact("My Tablet", 1,"192.168.1.196", true, null); //static ip of my tablet
        Contact contact3 =new Contact("My PC", 2,"192.168.1.18", true, null); //static ip of my tablet
        newUser.getContacts().put("My Phone",contact1);
        newUser.getContacts().put("My Tablet",contact2);
        newUser.getContacts().put("My PC",contact3);


        //encrypting storage and saving file
        long  timeEncryptingStorage =   System.nanoTime();
        File encryptedFile = new File(this.getFilesDir(), username);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(encryptedFile));
            byte[] yourKey = generateKey(password);
            byte[] filesBytes = encodeFile(yourKey, convertToBytes(newUser));
            bos.write(filesBytes);
            bos.flush();
            bos.close();
        }catch (Exception e1){
            Toast.makeText(this,"Could not encrypt and save file to storage",Toast.LENGTH_LONG).show();
            Log.e("Encrypt error", e1.getMessage());
            return null;
        }

        return newUser;
    }

    /**
     * Generate key for internal storage
     *
     * @param password  Password chosen by the user at sign up
     * @return  the key generated
     * @throws Exception    from called methods
     */
    public static byte[] generateKey(String password) throws Exception {
        byte [] salt = "salt1234".getBytes();
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);

        byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        byte[] ciphertext = cipher.doFinal("Hello, World!".getBytes("UTF-8"));

        return tmp.getEncoded();
    }

    /* -------------- Functions related to the encoding and decoding files  -------------- */

    /**
     * Encode file as a sequence of bytes.
     * It uses symmetric encryption
     *
     * @param key   key used for symmetric encryption/decryption
     * @param fileData  data to be encrypted
     * @return  encrypted data as {@code byte[]}
     * @throws Exception    from called methods
     */
    public static byte[] encodeFile(byte[] key, byte[] fileData) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(fileData);

        return encrypted;
    }

    /**
     * Decode file as a sequence of bytes.
     * It uses symmetric encryption
     *
     * @param key   key used for symmetric encryption/decryption
     * @param fileData  data to be decrypted
     * @return  decrypted data as {@code byte[]}
     * @throws Exception    from called methods
     */
    public static byte[] decodeFile(byte[] key, byte[] fileData) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);

        byte[] decrypted = cipher.doFinal(fileData);

        return decrypted;
    }

    /**
     * Convert a generic {@code Object}to a sequence of bytes
     *
     * @param object    The {@code Object} to be converted
     * @return  The {@code byte[]} containing the encoded object
     * @throws IOException  if an error occur in handling the OutputStreams
     */
    public static byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    /**
     * Convert a sequence of bytes into a generic {@code Object}
     *
     * @param bytes bytes to be converted
     * @return  the object extracted
     * @throws IOException  TODO
     * @throws ClassNotFoundException   TODO
     */
    public static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }
}


/*
//decrypting storage and extracting user class
try {
    String filePath = this.getFilesDir() + "/" + username+"Encrypted";
    File file = new File( filePath );
    byte[] fileBytes = new byte[(int) file.length()];
    BufferedInputStream bis = new BufferedInputStream(this.openFileInput(username+"Encrypted"));
    bis.read(fileBytes);
    byte[] yourKey = generateKey(password);
    byte[] decodedData = decodeFile(yourKey, fileBytes);
    User decodedUser = (User) convertFromBytes(decodedData);
} catch (FileNotFoundException e) {
    Log.e("File not found", e.toString());
} catch (Exception e) {
    Log.e("Error", e.toString());
}

// Use this for encrypting the message (just put message into stringForEncryption)
String stringForEncryption = "Borko";
byte[] encodedBytes = null;
try {
Cipher c = Cipher.getInstance("RSA");
c.init(Cipher.ENCRYPT_MODE, publicKey);
encodedBytes = c.doFinal(stringForEncryption.getBytes());
} catch (Exception e) {
Log.e("EncryptionError", "Error while doing encryption");
}

//translate encryption to string
String encryptedMessage = new String(Base64.encodeToString(encodedBytes, Base64.DEFAULT));

// Use this for decrypting the message
byte[] decodedBytes = null;
try {
Cipher c = Cipher.getInstance("RSA");
c.init(Cipher.DECRYPT_MODE, privateKey);
decodedBytes = c.doFinal(encodedBytes);
} catch (Exception e) {
Log.e("DecryptionError", "Error while doing decryption");
}

//translate decrypted message to string
String decryptedMessage = new String(decodedBytes);
*/

/*
if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
    //converting key to String
    Key exampleKey = null;
    byte[] publicKeyBytesNew = exampleKey.getEncoded();
    String publicKeyStringNew = Base64.getEncoder().encodeToString(publicKeyBytesNew);

    //converting from string to Key
    try {
        String exampleKeyString = "";
        // Base64 decode the data
        byte [] encoded = Base64.getDecoder().decode(exampleKeyString);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        Key publicKeyNew = kf.generatePublic(publicKeySpec);
    } catch (Exception e2) {
        Log.e("error", e2.toString());
    }
}
*/

