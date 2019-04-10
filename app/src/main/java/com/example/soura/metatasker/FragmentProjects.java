package com.example.soura.metatasker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class FragmentProjects extends Fragment {

    private RecyclerView mFriendsList;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mTaskDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_User_ID;
    View view;
    private Button OkButton,Cancelbutton;

    public FragmentProjects() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_fragment_projects, container, false);

        mFriendsList = (RecyclerView)view.findViewById(R.id.rv_projects);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_User_ID = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Projects");
        mFriendsDatabase.keepSynced(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        mTaskDatabase = FirebaseDatabase.getInstance().getReference().child("Projects").child(mCurrent_User_ID);
        mTaskDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton floatingActionButton = (FloatingActionButton)view.findViewById(R.id.fab);

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                projectalert();
            }
        });

        return view;
    }

    private void projectalert()
    {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog, null);

        final EditText nametext = (EditText) alertLayout.findViewById(R.id.projectname);

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Create Project:");
        alert.setView(alertLayout);
        alert.setCancelable(false);

        final AlertDialog dialog = alert.create();

        OkButton = (Button) alertLayout.findViewById(R.id.okbutton);

        Cancelbutton = (Button) alertLayout.findViewById(R.id.cancelbutton);

        OkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String name = nametext.getText().toString().trim();
                if(name.isEmpty())
                {
                    nametext.setError("Required");
                }
                else
                {
                    mTaskDatabase.push().child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        Cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = mFriendsDatabase.child(mCurrent_User_ID);

        FirebaseRecyclerOptions<ProjectData> options = new FirebaseRecyclerOptions.Builder<ProjectData>()
                .setQuery(query, ProjectData.class)
                .setLifecycleOwner(this)
                .build();


        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<ProjectData, TaskViewHolder>(options)
        {
            @Override
            public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_list_view, parent, false);

                return new TaskViewHolder(view);
            }

            protected void onBindViewHolder(final TaskViewHolder taskViewHolder, int position, ProjectData model)
            {
                taskViewHolder.setName(model.getName());

                final String taskid = getRef(position).getKey();

                taskViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFriendsDatabase.child(mCurrent_User_ID).child(taskid).setValue(null);
                    }
                });

                taskViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent profileIntent = new Intent(getContext(), ProjectActivity.class);
                        profileIntent.putExtra("taskid", taskid);
                        startActivity(profileIntent);
                    }
                });

            }
        };
        mFriendsList.setAdapter(adapter);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        View mView;
        public TaskViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            this.imageView = (ImageView)itemView.findViewById(R.id.delete);
        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.single_user_name);
            userNameView.setText(name);
        }
    }

}
