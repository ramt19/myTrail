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
//        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
//                recyclerView, new ClickListener() {
//
//            @Override
//            public void onClick(View view, final int position) {
//                //Values are passing to activity & to fragment as well
//
//            }
//
//            @Override
//            public void onLongClick(View view, int position) {
//                Toast.makeText(UpdateContacts.this, "Long press on position :"+position,
//                        Toast.LENGTH_LONG).show();
//            }
//
//        }));
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
        final String uid = pref.getString("uid","null");
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference users = firebaseDatabase.child("Users");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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


//    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
//        private String[] mDataset;
//
//        public static class MyViewHolder extends RecyclerView.ViewHolder {
//            // each data item is just a string in this case
//            public TextView textView;
//            public MyViewHolder(TextView v) {
//                super(v);
//                textView = v;
//            }
//        }
//
//        // Provide a suitable constructor (depends on the kind of dataset)
//        public MyAdapter(String[] myDataset) {
//            mDataset = myDataset;
//        }
//
//        // Create new views (invoked by the layout manager)
//        @Override
//        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
//                                                         int viewType) {
//             //create a new view
//            TextView v = (TextView) LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.my_text_view, parent, false);
//
//            MyViewHolder vh = new MyViewHolder(v);
//            return vh;
//        }
//
//        // Replace the contents of a view (invoked by the layout manager)
//        @Override
//        public void onBindViewHolder(MyViewHolder holder, int position) {
//            // - get element from your dataset at this position
//            // - replace the contents of the view with that element
//            holder.textView.setText(mDataset[position]);
//
//        }
//
//        // Return the size of your dataset (invoked by the layout manager)
//        @Override
//        public int getItemCount() {
//            return mDataset.length;
//        }
//
//    }
}
