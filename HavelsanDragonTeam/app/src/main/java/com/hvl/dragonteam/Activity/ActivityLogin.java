package com.hvl.dragonteam.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.hvl.dragonteam.DataService.NotificationService;
import com.hvl.dragonteam.DataService.PersonService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.LanguageEnum;
import com.hvl.dragonteam.Model.Enum.SideEnum;
import com.hvl.dragonteam.Model.Person;
import com.hvl.dragonteam.Model.PersonNotification;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
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
    private TextView txtEnterCode;
    private TextView txtResend;
    private ImageView imgLogo;
    private LinearLayout layoutPhone;
    private LinearLayout layoutCode;
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
        txtEnterCode = (TextView) findViewById(R.id.txt_enter_code);
        txtResend = (TextView) findViewById(R.id.txt_resend);
        imgLogo = (ImageView) findViewById(R.id.img_logo);
        layoutPhone = (LinearLayout) findViewById(R.id.layout_phone);
        layoutCode = (LinearLayout) findViewById(R.id.layout_code);
        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //testPhoneAutoRetrieve(editTextPhone.getText().toString(), editTextCode.getText().toString());
                if (!editTextPhone.getText().toString().trim().equals("")) {
                    progressDialog = ProgressDialog.show(ActivityLogin.this, getString(R.string.processing), getString(R.string.verifying), false, false);
                    startPhoneNumberVerification(editTextPhone.getText().toString().trim());
                } else {
                    Util.toastWarning(ActivityLogin.this,getString(R.string.enter_phone_number));
                }
            }
        });
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextCode.getText().toString().trim().equals("")) {
                    verifyPhoneNumberWithCode(mVerificationId, editTextCode.getText().toString().trim());
                } else {
                    Util.toastWarning(ActivityLogin.this,getString(R.string.enter_verification_code).replace("XXX", editTextPhone.getText().toString().trim()));
                }
            }
        });

        txtResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = ProgressDialog.show(ActivityLogin.this, getString(R.string.processing), getString(R.string.resending), false, false);
                resendVerificationCode(editTextPhone.getText().toString());
            }
        });

      /*  Glide.with(ActivityLogin.this)
                .load(Uri.parse(Util.getTeamLogoURL()))
                .apply(new RequestOptions()
                        .centerInside()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.icon)
                        .error(R.drawable.icon))
                .into(imgLogo);*/

        layoutPhone.setVisibility(View.VISIBLE);
        layoutCode.setVisibility(View.GONE);
    }

    private void startPhoneNumberVerification(String phoneNumber) {

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                progressDialog.dismiss();
                Util.toastError(ActivityLogin.this);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                Util.toastInfo(ActivityLogin.this, getString(R.string.code_sent));
                progressDialog.dismiss();
                mVerificationId = verificationId;
                mResendToken = token;

                txtEnterCode.setText(getString(R.string.enter_verification_code).replace("XXX", phoneNumber));
                layoutPhone.setVisibility(View.GONE);
                layoutCode.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                Log.i(TAG, "onCodeAutoRetrievalTimeOut: " + s);
                //Util.toastWarning(ActivityLogin.this, "Timeout");
            }
        };

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

        signInWithPhoneAuthCredential(credential);
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

    private void resendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(mResendToken)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void testPhoneAutoRetrieve(String phoneNumber, String smsCode) {
        // [START auth_test_phone_auto]
        // The test phone number and code should be whitelisted in the console.
        phoneNumber = "+16505554567";
        smsCode = "123456";

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
               "",
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

        Intent intent = new Intent(ActivityLogin.this, ActivityTeam.class);//TODO team mi home mu
        startActivity(intent);

        finish();
    }

}