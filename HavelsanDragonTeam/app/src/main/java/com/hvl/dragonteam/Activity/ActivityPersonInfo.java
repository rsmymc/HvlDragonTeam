package com.hvl.dragonteam.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
public class ActivityPersonInfo extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextWeight;
    private EditText editTextHeight;
    private Spinner spinnerSide;
    private Button btnSave;
    private Bundle bundle;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);
        bundle = getIntent().getExtras();
        editTextName = (EditText) findViewById(R.id.txt_name);
        editTextWeight = (EditText) findViewById(R.id.txt_weight);
        editTextHeight = (EditText) findViewById(R.id.txt_height);
        spinnerSide = (Spinner) findViewById(R.id.spinner_side);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ActivityPersonInfo.this, R.array.side_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSide.setAdapter(adapter);
        btnSave = (Button) findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(editTextName.getText().toString().trim().equals("")){
                    Util.toastWarning(ActivityPersonInfo.this, getString(R.string.name) + " "  + getString(R.string.warning_cant_empty));
                } else if(editTextHeight.getText().toString().trim().equals("")){
                    Util.toastWarning(ActivityPersonInfo.this, getString(R.string.height) + " "  + getString(R.string.warning_cant_empty));
                } else if(editTextWeight.getText().toString().trim().equals("")){
                    Util.toastWarning(ActivityPersonInfo.this, getString(R.string.weight) + " "  + getString(R.string.warning_cant_empty));
                } else {
                    Person person = new Person(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            editTextName.getText().toString(),
                            FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),
                            Integer.parseInt(editTextHeight.getText().toString()),
                            Integer.parseInt(editTextWeight.getText().toString()),
                            SideEnum.toSideEnum(spinnerSide.getSelectedItemPosition()).getValue(),
                            null);

                    createUser(person);
                }
            }
        });

    }
    private void createUser(Person person) {
        PersonService personService = new PersonService();
        progressDialog = ProgressDialog.show(ActivityPersonInfo.this, getString(R.string.processing), getString(R.string.wait), false, false);
        try {
            personService.savePerson(this,
                    person,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Constants.person = person;
                            PersonNotification personNotification = new PersonNotification(FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseInstanceId.getInstance().getToken(), true, LanguageEnum.getLocaleEnumValue());
                            saveNotification(personNotification);
                            progressDialog.dismiss();
                            goToIntent();
                        }

                        @Override
                        public void onError(String result) {
                            FirebaseAuth.getInstance().getCurrentUser().delete();
                            FirebaseAuth.getInstance().signOut();
                            progressDialog.dismiss();
                            Util.toastError(ActivityPersonInfo.this);
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Util.toastError(ActivityPersonInfo.this);
        }
    }

    private void saveNotification(PersonNotification personNotification) {

        NotificationService notificationService = new NotificationService();
        try {
            notificationService.saveNotification(ActivityPersonInfo.this, personNotification, null);
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(ActivityPersonInfo.this);
        }
    }

    private void goToIntent() {

        Intent intent = new Intent(ActivityPersonInfo.this, ActivityTeam.class);
        if (bundle != null/* && bundle.getString("DIRECT", "").equals("JOIN")*/) {
            intent.putExtras(bundle);
        }
        startActivity(intent);

        finish();
    }

}