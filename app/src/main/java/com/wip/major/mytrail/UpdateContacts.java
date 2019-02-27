package com.wip.major.mytrail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UpdateContacts extends AppCompatActivity {

    private static final String TAG = UpdateContacts.class.getSimpleName() ;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button selectContacts;
    private MyAdapter adapter;
    private ArrayList<Contact> myDataset = new ArrayList<>();
    private boolean flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contacts);
        recyclerView = (RecyclerView) findViewById(R.id.contacts);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MyAdapter(this);
        populateContactValues();
        selectContacts = (Button) findViewById(R.id.addContacts);
        selectContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, 1);
            }
        });
    }

    private void populateContactValues() {
        Log.i(TAG, "populatecontactvalues start");
        SharedPreferences pref = getApplication().getSharedPreferences("session", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        final String uid = pref.getString("uid","null");
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference users = firebaseDatabase.child("Users");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(uid).child("Contacts").exists()){
                    editor.putString("contacts","true");
                }
                else
                {
                    editor.putString("contacts","false");
                }
                editor.commit();
                for(DataSnapshot dataSnapshot1: dataSnapshot.child(uid).child("Contacts").getChildren()){
                    String name = dataSnapshot1.getKey();
                    String phone = (String) dataSnapshot1.getValue();
                    Contact contact = new Contact();
                    contact.setName(name);
                    contact.setNumber(phone);
                    myDataset.add(contact);
                    Log.i(TAG, name);

                }
                adapter.setListContent(myDataset);
                recyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        users.addListenerForSingleValueEvent(valueEventListener);
        Log.i(TAG, "populatecontactvalues done");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri;
                Cursor cursor1;
                uri = data.getData();
                Cursor cursor =  managedQuery(uri, null, null, null, null);
                cursor.moveToFirst();

                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Users");
                //start next activity
                SharedPreferences pref = getApplicationContext().getSharedPreferences("session", MODE_PRIVATE);
                String uid = pref.getString("uid","null");
                mDatabase.child(uid).child("Contacts").child(name).setValue(number);
                Log.i(TAG, number);
                Log.i(TAG, name);
            }
            finish();
            startActivity(getIntent());
        }
    }

    public static interface ClickListener{
        public void onClick(View view, int position);
        public void onLongClick(View view, int position);
    }

    class RecyclerTouchListener implements  RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
            View child=recyclerView.findChildViewUnder(motionEvent.getX(),motionEvent.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(motionEvent)){
                clicklistener.onClick(child,recyclerView.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean b) {

        }
    }

}
