package com.hvl.dragonteam.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.hvl.dragonteam.DataService.NotificationService;
import com.hvl.dragonteam.DataService.PersonService;
import com.hvl.dragonteam.DataService.PersonTeamService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.LanguageEnum;
import com.hvl.dragonteam.Model.Enum.RoleEnum;
import com.hvl.dragonteam.Model.Enum.SideEnum;
import com.hvl.dragonteam.Model.Person;
import com.hvl.dragonteam.Model.PersonNotification;
import com.hvl.dragonteam.Model.PersonTeam;
import com.hvl.dragonteam.Model.PersonTeamView;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * Created by rasim-pc on 27.5.2018.
 */
public class ActivityLogin extends AppCompatActivity {

    private Button btnLogin;
    private EditText editTextPhone;
    private Button btnVerify;
    private EditText editTextCode;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private static final String TAG = "PhoneAuthActivity";
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseAuth.getInstance().getFirebaseAuthSettings().forceRecaptchaFlowForTesting(true);

        btnLogin = (Button) findViewById(R.id.btn_send_sms);
        editTextPhone = (EditText) findViewById(R.id.txt_phone);
        btnVerify = (Button) findViewById(R.id.btn_verify);
        editTextCode = (EditText) findViewById(R.id.txt_code);
        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //testPhoneVerify();
                testPhoneAutoRetrieve(editTextPhone.getText().toString(), editTextCode.getText().toString());
                //startPhoneNumberVerification(editTextPhone.getText().toString());
            }
        });
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPhoneNumberWithCode(mVerificationId, editTextCode.getText().toString());
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }
                Util.toastError(ActivityLogin.this);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
                //TODO hide phone show code UI
            }
            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                Log.i(TAG, "onCodeAutoRetrievalTimeOut: " + s);
            }
        };
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(10L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressDialog = ProgressDialog.show(ActivityLogin.this, getString(R.string.processing), getString(R.string.wait), false, false);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getPerson();
                        } else {
                            progressDialog.dismiss();
                            Util.toastError(ActivityLogin.this);
                        }
                    }
                });
    }
    public void testPhoneVerify() {
        // [START auth_test_phone_verify]
        String phoneNum = "+16505554567";
        String testVerificationCode = "123456";

        // Whenever verification is triggered with the whitelisted number,
        // provided it is not set for auto-retrieval, onCodeSent will be triggered.
        FirebaseAuth auth = FirebaseAuth.getInstance();
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNum)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        // Save the verification id somewhere
                        // ...

                        // The corresponding whitelisted code above should be used to complete sign-in.
                        ActivityLogin.this.enableUserManuallyInputCode();
                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        // Sign in with the credential
                        // ...
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        // ...
                        Log.e(TAG,e.getLocalizedMessage());
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        // [END auth_test_phone_verify]
    }

    private void enableUserManuallyInputCode() {
        // No-op
    }

    public void testPhoneAutoRetrieve(String phoneNumber, String smsCode) {
        // [START auth_test_phone_auto]
        // The test phone number and code should be whitelisted in the console.
       // String phoneNumber = "+16505556878";
        //String smsCode = "687868";

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseAuthSettings firebaseAuthSettings = firebaseAuth.getFirebaseAuthSettings();

        // Configure faking the auto-retrieval with the whitelisted numbers.
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, smsCode);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // Instant verification is applied and a credential is directly returned.
                        // ...
                        signInWithPhoneAuthCredential(credential);
                    }

                    // [START_EXCLUDE]
                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                    }
                    // [END_EXCLUDE]
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        // [END auth_test_phone_auto]
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void getPerson() {

        PersonService personService = new PersonService();
        Person _person = new Person();
        _person.setId(mAuth.getCurrentUser().getUid());

        try {
            personService.getPerson(ActivityLogin.this,
                    _person,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Person person = new Gson().fromJson(result.toString(), Person.class);
                            Constants.person = person;
                            getNotification();
                            progressDialog.dismiss();
                            goToIntent();
                        }

                        @Override
                        public void onError(String result) {
                            createUser();
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Util.toastError(ActivityLogin.this);
        }
    }

    private void createUser() {
        PersonService personService = new PersonService();
        Person person = new Person(mAuth.getCurrentUser().getUid(),
                mAuth.getCurrentUser().getDisplayName(),
                mAuth.getCurrentUser().getPhoneNumber(),
                0,
               0,
                SideEnum.RIGHT.getValue(),
                null);

        try {
            personService.savePerson(this,
                    person,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Constants.person = person;
                            PersonNotification personNotification = new PersonNotification(mAuth.getCurrentUser().getUid(), FirebaseInstanceId.getInstance().getToken(), true, LanguageEnum.getLocaleEnumValue());
                            saveNotification(personNotification);
                            progressDialog.dismiss();
                            goToIntent();
                        }

                        @Override
                        public void onError(String result) {
                            mAuth.getCurrentUser().delete();
                            FirebaseAuth.getInstance().signOut();
                            progressDialog.dismiss();
                            Util.toastError(ActivityLogin.this);
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Util.toastError(ActivityLogin.this);
        }
    }

    private void getNotification() {

        NotificationService notificationService = new NotificationService();
        PersonNotification _personNotification = new PersonNotification(mAuth.getCurrentUser().getUid(), FirebaseInstanceId.getInstance().getToken());

        try {
            notificationService.getNotification(ActivityLogin.this,
                    _personNotification,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            PersonNotification personNotification = new Gson().fromJson(result.toString(), PersonNotification.class);
                            personNotification.setLoggedIn(true);
                            personNotification.setLanguageType(LanguageEnum.getLocaleEnumValue());
                            saveNotification(personNotification);
                        }

                        @Override
                        public void onError(String result) {
                            PersonNotification personNotification = new PersonNotification(mAuth.getCurrentUser().getUid(), FirebaseInstanceId.getInstance().getToken(), true, LanguageEnum.getLocaleEnumValue());
                            saveNotification(personNotification);
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(ActivityLogin.this);
        }
    }

    private void saveNotification(PersonNotification personNotification) {

        NotificationService notificationService = new NotificationService();
        try {
            notificationService.saveNotification(ActivityLogin.this, personNotification, null);
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(ActivityLogin.this);
        }
    }

    private void goToIntent() {

       Intent intent = new Intent(ActivityLogin.this, ActivityHome.class);//TODO team mi home mu
       startActivity(intent);

       finish();
    }

}