package com.example.soura.metatasker;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ProjectActivity extends AppCompatActivity {
    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mTaskDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_User_ID;
    String projectid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        projectid = getIntent().getStringExtra("taskid");

        mFriendsList = (RecyclerView)findViewById(R.id.rv_tasks);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_User_ID = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Projects").child(mCurrent_User_ID).child(projectid);
        mFriendsDatabase.keepSynced(true);

        mTaskDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks");
        mTaskDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton floatingActionButton = (FloatingActionButton)findViewById(R.id.fab);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectActivity.this, AddTaskActivity.class);
                intent.putExtra("projectid", projectid);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = mFriendsDatabase.child("Tasks");

        FirebaseRecyclerOptions<TaskData> options = new FirebaseRecyclerOptions.Builder<TaskData>()
                .setQuery(query, TaskData.class)
                .setLifecycleOwner(this)
                .build();


        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<TaskData, TaskViewHolder>(options) {
            @Override
            public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_list_view, parent, false);

                return new TaskViewHolder(view);
            }

            protected void onBindViewHolder(final TaskViewHolder taskViewHolder, int position, TaskData model) {
                taskViewHolder.setName(model.getName());

                final String taskid = getRef(position).getKey();

                taskViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFriendsDatabase.child("Tasks").child(taskid).setValue(null);
                        mTaskDatabase.child(taskid).setValue(null);
                    }
                });

                taskViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(ProjectActivity.this, TaskActivity.class);
                        profileIntent.putExtra("taskid", taskid);
                        profileIntent.putExtra("ptojectid", projectid);
                        startActivity(profileIntent);
                    }
                });

            }
        };
        mFriendsList.setAdapter(adapter);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        View mView;

        public TaskViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            this.imageView = (ImageView) itemView.findViewById(R.id.delete);
        }

        public void setName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.single_user_name);
            userNameView.setText(name);
        }
    }
}