package com.example.soura.metatasker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {

    private EditText mSearchField;
    private ImageButton mSearchBtn;

    String sharedtaskname,shareduser,taskid;
    private RecyclerView mResultList;

    private DatabaseReference mUserDatabase,mTaskDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        sharedtaskname = getIntent().getStringExtra("name");
        shareduser = getIntent().getStringExtra("user");
        taskid = getIntent().getStringExtra("taskid");

        mUserDatabase = FirebaseDatabase.getInstance().getReference("Users");

        mSearchField = (EditText) findViewById(R.id.search_field);
        mSearchBtn = (ImageButton) findViewById(R.id.search_btn);

        mResultList = (RecyclerView) findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String searchText = mSearchField.getText().toString();
                firebaseUserSearch(searchText);
            }
        });

    }

    private void firebaseUserSearch(String searchText) {
        Query firebaseSearchQuery = mUserDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(firebaseSearchQuery, Users.class)
                        .setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new UsersViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(UsersViewHolder holder, int position, Users model) {
                holder.setDetails(getApplicationContext(), model.getName(), model.getImage());

                final String profile_uid = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        if(profile_uid.equals(shareduser))
                        {
                            Toast.makeText(SearchActivity.this,"You Must Select Differen User!",Toast.LENGTH_LONG).show();
                        }
                        else {
                            Map<String, Object> requestMap = new HashMap<>();

                            requestMap.put("name", sharedtaskname + " (shared project)");
                            requestMap.put("Tasks/"+taskid+"/name",sharedtaskname);

                            mTaskDatabase = FirebaseDatabase.getInstance().getReference().child("Projects").child(profile_uid);
                            mTaskDatabase.push().updateChildren(requestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    Intent search = new Intent(SearchActivity.this,TaskActivity.class);
                                    search.putExtra("taskid",taskid);
                                    startActivity(search);
                                    finish();
                                }
                            });
                        }
                    }
                });
            }
        };

        mResultList.setAdapter(adapter);
    }


    // View Holder Class

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDetails(Context ctx, String userName, String userImage) {
            TextView user_name = (TextView) mView.findViewById(R.id.single_user_name);
            CircleImageView user_image = (CircleImageView) mView.findViewById(R.id.single_user_image);

            user_name.setText(userName);

            Picasso.get().load(userImage).placeholder(R.drawable.profile).into(user_image);
        }

    }
}