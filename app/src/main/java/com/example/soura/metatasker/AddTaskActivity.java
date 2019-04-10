package com.example.soura.metatasker;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddTaskActivity extends AppCompatActivity {

    String year,month,date;
    String filename;
    EditText nameText, detailsText;
    String name, detail,name_of_file;
    Button okbutton;
    private DatabaseReference mFriendsDatabase,mTaskDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_User_ID;
    FirebaseStorage storage;
    StorageReference storageReference;

    TextView datetext;

    private ImageView imageView;
    private Uri filePath;
    String projectid;
    private final int PICK_IMAGE_REQUEST = 71;

    private ImageView AddAttachment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        projectid = getIntent().getStringExtra("projectid");

        imageView = (ImageView) findViewById(R.id.taskimage);

        datetext=(TextView)findViewById(R.id.taskdate);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("Tasks");

        nameText = (EditText) findViewById(R.id.task_title);
        detailsText = (EditText) findViewById(R.id.task);
        AddAttachment = (ImageView) findViewById(R.id.attachments);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_User_ID = mAuth.getCurrentUser().getUid();

        mTaskDatabase = FirebaseDatabase.getInstance().getReference().child("Projects").child(mCurrent_User_ID).child(projectid).child("Tasks");
        mTaskDatabase.keepSynced(true);

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Tasks");
        mFriendsDatabase.keepSynced(true);

        okbutton = (Button) findViewById(R.id.okbutton);


        AddAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{"Choose Photo", "Open Camera"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(AddTaskActivity.this);
                builder.setTitle("Add Attachments:");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            chooseImage();
                        }
                        if (i == 1)
                        {
                            //    choosePdf();
                        }
                    }
                });

                builder.show();
            }
        });
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    private void uploadImage() {
        name = nameText.getText().toString();
        detail = detailsText.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        if (filePath != null && name != null) {

            final StorageReference ref = storageReference.child("/attachments/" + UUID.randomUUID().toString());
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(AddTaskActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri)
                        {
                            final Uri fileUrl = uri;

                            final String downloadFileUrl = fileUrl.toString();

                            Map<String, Object> requestMap = new HashMap<>();

                            requestMap.put("name", name);
                            requestMap.put("details", detail);
                            requestMap.put("file", downloadFileUrl);
                            requestMap.put("file_name", name_of_file);
                            requestMap.put("date", "Date");

                            DatabaseReference db_ref = mFriendsDatabase.push();
                            final String keynode = db_ref.getKey();

                            db_ref.updateChildren(requestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    mTaskDatabase.child(keynode).child("name").setValue(name);
                                }
                            });

                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddTaskActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show(); // code is
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });

            progressDialog.dismiss();

            Intent setup = new Intent(AddTaskActivity.this, ProjectActivity.class);
            setup.putExtra("taskid",projectid);
            startActivity(setup);
            finish();

        } else if (!name.isEmpty()) {
            Map<String, Object> requestMap = new HashMap<>();

            requestMap.put("name", name);
            requestMap.put("details", detail);
            requestMap.put("file", "default");
            requestMap.put("date", "Date");

            DatabaseReference db_ref = mFriendsDatabase.push();
            final String keynode = db_ref.getKey();

            db_ref.updateChildren(requestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    mTaskDatabase.child(keynode).child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            Intent setup = new Intent(AddTaskActivity.this, ProjectActivity.class);
                            setup.putExtra("taskid",projectid);
                            startActivity(setup);
                            finish();
                        }
                    });
                }
            });


        } else {
            nameText.setError("Required");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        filePath = data.getData();

        filename = filePath.getLastPathSegment();

        TextView filenametext = (TextView)findViewById(R.id.namefile);
        filenametext.setText(filename);

        String extension;

        if (filePath.getScheme().equals(ContentResolver.SCHEME_CONTENT))
        {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(this.getContentResolver().getType(filePath));
        }
        else
        {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(filePath.getPath())).toString());
        }

        if(extension.equals("jpg")|| extension.equals("jpeg") || extension.equals("png") )
        {
            if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                    && data != null && data.getData() != null ) {

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.pdf));
        }
    }

    private void chooseImage() {
        String[] extraMimeTypes = {"application/*","text/*","image/*"};
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES,extraMimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_IMAGE_REQUEST);
    }

    public void opencalender(View view)
    {
        View view1 = getLayoutInflater().inflate(R.layout.datepicker, null);
        final PopupWindow popupWindow = new PopupWindow(view1, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setFocusable(true);

        final DatePicker datePicker = (DatePicker) view1.findViewById(R.id.datePicker);
        Button button1 = (Button) view1.findViewById(R.id.pick);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year = String.valueOf(datePicker.getYear());
                month = String.valueOf(datePicker.getMonth());
                date = String.valueOf(datePicker.getDayOfMonth());

                String f_date = date + " - " + month + " - " + year;

                datetext.setText(f_date);

                popupWindow.dismiss();

            }
        });
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }
}
