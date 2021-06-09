package com.stephenfong.pepperchat.view.activities.contact;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.adapter.ContactsAdapter;
import com.stephenfong.pepperchat.databinding.ActivityContactsBinding;
import com.stephenfong.pepperchat.model.user.User;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private static final String TAG = "ContactsActivityLOG";
    private ActivityContactsBinding binding;
    private List<User> userList;
    private ContactsAdapter adapter;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;

    public static final int REQUEST_READ_CONTACTS = 6000;
    private ListView contactList;
    private ArrayList mobileArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contacts);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (firebaseUser != null) {
            getContactFromPhone(); // If they are using this app
            refreshContactList();
        }

        if (mobileArray != null) {
            for (int i = 0; i < mobileArray.size(); i++) {
                Log.d("WTF", mobileArray.get(i).toString());
            }
        }

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getContactFromPhone() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            mobileArray = getAllPhoneContacts();
        } else {
            requestPermission();
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
            // show UI part if you want here to show some rationale !!!
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mobileArray = getAllPhoneContacts();
                } else {
                    // permission denied,Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
    private ArrayList getAllPhoneContacts() {
        ArrayList<String> phoneList = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
//                String name = cur.getString(cur.getColumnIndex(
//                        ContactsContract.Contacts.DISPLAY_NAME));
//                nameList.add(name);

                if (cur.getInt(cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNo = phoneNo.replace("-", "").replace(" ", "");
                        phoneList.add(phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
        return phoneList;
    }

    private void refreshContactList() {
        userList = new ArrayList<>();
        firebaseFirestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                    String userID = snapshot.getString("userID");
                    String userName = snapshot.getString("userName");
                    String imageUrl = snapshot.getString("profileImage");
                    String desc = snapshot.getString("bio");
                    String phone = snapshot.getString("userPhone");

                    User user = new User();
                    user.setUserID(userID);
                    user.setBio(desc);
                    user.setUserName(userName);
                    user.setProfileImage(imageUrl);
                    user.setUserPhone(phone);

                    if (userID != null && ! userID.equals(firebaseUser.getUid())) {
                        Log.d("ContactsActivity", mobileArray + "");
                        if (mobileArray.contains(user.getUserPhone())) {
                            Log.d("ContactsActivity", "Phone on contact list: " + user.getUserPhone());
                            userList.add(user);
                        }
                    }
                }
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                adapter = new ContactsAdapter(userList, ContactsActivity.this);
                binding.recyclerView.setAdapter(adapter);
            }
        });
    }
}