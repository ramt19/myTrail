package com.wip.major.mytrail;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

    private static final String TAG = MyAdapter.class.getSimpleName();
    private ArrayList<Contact> members = new ArrayList<>();
    private final LayoutInflater inflater;
    View view;
    MyViewHolder holder;
    private Context context;

    public MyAdapter(Context context){
        this.context = context;
        inflater=LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        view = inflater.inflate(R.layout.my_text_view, viewGroup, false);
        holder = new MyViewHolder(view, context);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Contact list_items = members.get(i);
        myViewHolder.name.setText(list_items.getName());
        myViewHolder.number.setText(list_items.getNumber());

    }

    public void setListContent(ArrayList<Contact> members){
        this.members = members;
        notifyItemRangeChanged(0, members.size());
    }
    @Override
    public int getItemCount() {
        return members.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView name,number;
        Context context;
        public MyViewHolder(View itemView, final Context context) {
            super(itemView);
            itemView.setOnClickListener(this);
            name=(TextView)itemView.findViewById(R.id.name);
            number=(TextView)itemView.findViewById(R.id.number);
            this.context = context;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Confirmation");
                    builder.setMessage("Do you want to Delete this Contact?");
                    // add the buttons
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final int pos = getAdapterPosition();
                            Contact clickeditem = members.get(pos);
                            final String name = clickeditem.getName();
                            Log.i(TAG, clickeditem.getName());
                            SharedPreferences pref = context.getSharedPreferences("session", Context.MODE_PRIVATE);
                            final String uid = pref.getString("uid","null");
                            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
                            DatabaseReference users = firebaseDatabase.child("Users");
                            ValueEventListener valueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    dataSnapshot.child(uid).child("Contacts").child(name).getRef().removeValue();
                                    removeAt(pos);
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            };
                            users.addListenerForSingleValueEvent(valueEventListener);
                            Log.i(TAG, "populatecontactvalues done");
                        }
                    });
                    builder.setNegativeButton("Cancel", null);

                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            });

        }
        @Override
        public void onClick(View v) {

        }
    }
    public void removeAt(int position) {
        members.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, members.size());
    }
}
