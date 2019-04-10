package com.example.soura.metatasker;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskActivity extends AppCompatActivity {

    String  taskid;
    FirebaseAuth mAuth;
    String mCurrent_User_ID;
    DatabaseReference mFriendsDatabase;
    private Context context;
    DownloadManager downloadManager;
    private RecyclerView mMessagesList;
    private ImageView mChatSendButton;
    TextView usermanager;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private String mLastKey = "";
    private String mPrevKey = "";

    private int itemPos = 0;

    EditText mChatMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        taskid = getIntent().getStringExtra("taskid");

        Toast.makeText(TaskActivity.this, taskid, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        mCurrent_User_ID = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskid);
        mFriendsDatabase.keepSynced(true);

        final TextView nameText = (TextView)findViewById(R.id.taskname);
        final TextView detailsText = (TextView)findViewById(R.id.detailstext);
        final TextView attachmenttext = (TextView)findViewById(R.id.attachments);

        usermanager = (TextView)findViewById(R.id.manageusers);

        mChatSendButton = (ImageView)findViewById(R.id.sendbutton);
        mChatMessageView =(EditText)findViewById(R.id.commenttext);

        mAdapter = new MessageAdapter(messagesList);
        mMessagesList=(RecyclerView)findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);

        loadMessages();


        mFriendsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot)
            {
                final String name = dataSnapshot.child("name").getValue().toString();
                String details = dataSnapshot.child("details").getValue().toString();
                final String attachment = dataSnapshot.child("file").getValue().toString();

                usermanager.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent search = new Intent(TaskActivity.this,SearchActivity.class);
                        search.putExtra("name",name);
                        search.putExtra("user",mCurrent_User_ID);
                        search.putExtra("taskid",taskid);
                        startActivity(search);

                    }
                });

                if(attachment.equalsIgnoreCase("default"))
                {
                    attachmenttext.setText("No Attachment");
                }
                else
                {
                    attachmenttext.setText("1 Attachment");
                    attachmenttext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String filename = dataSnapshot.child("file_name").getValue().toString();
                            downloadManager= (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                            Uri uri = Uri.parse(attachment);
                            DownloadManager.Request request = new DownloadManager.Request(uri);
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setTitle(filename);
                            Long reference = downloadManager.enqueue(request);
                        }
                    });

                }

                nameText.setText(name);
                detailsText.setText(details);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendMessages();
            }
        });

    }


    private void loadMessages()
    {
        DatabaseReference messageRef = mFriendsDatabase.child("Conversations");

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if(itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void sendMessages()
    {
        String message= mChatMessageView.getText().toString();

        if(!TextUtils.isEmpty(message))
        {
            Map messageMap = new HashMap();
            messageMap.put("comments", message);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from",mAuth.getCurrentUser().getUid());

            mChatMessageView.setText("");

            mFriendsDatabase.child("Conversations").push().updateChildren(messageMap, new DatabaseReference.CompletionListener()
            {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                {
                    if(databaseError != null)
                    {
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });

        }
    }
}


