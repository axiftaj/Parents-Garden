package com.example.pg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;



import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pg.Adapter.AdapterComments;
import com.example.pg.Model.ModelComments;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity {

    //get details of user and post
    String hisUid , pImage ,myUid ,myEmail , myName , myDp,
            postId , pLikes , hisDp , hisName ;
    ImageView pImageIv;
    TextView uNameTv ,pTimeTv ,pTitleTv ,pDescriptionTv ,pLikeTv, pCommentsTv ;
    ImageButton moreBtn;
    Button likeBtn , shareBtn ;
    LinearLayout profileLayout;
    CircleImageView uPictureIv ;

    ProgressDialog pd ;
    //add comment view
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv ;

    boolean mProcessComment = false;
    boolean mProcessLikes = false;

    RecyclerView recyclerView ;
    List<ModelComments> commentsList ;
    AdapterComments adapterComments ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);




        //get id of the post of using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        pTimeTv = findViewById(R.id.pTimeTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikeTv = findViewById(R.id.pLikeTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBTN);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profileLayout);

        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);
        recyclerView = findViewById(R.id.recyclerView);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        loadPostInfo();
        checkUserStatus();
        loadUserInfo();

        setLikes();
        //set subtitle
        actionBar.setSubtitle("SignedIn as:"+myEmail);

        loadComments();
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareMoreOption();

            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle = pTitleTv.getText().toString().trim();
                String pDecsiption = pDescriptionTv.getText().toString().trim();

                BitmapDrawable bitmapDrawable = (BitmapDrawable)pImageIv.getDrawable();
                if (bitmapDrawable == null){
                    //share text only
                    shareTextOnly(pTitle , pDecsiption);
                }else {
                    //post with image

                    //convert image to bit map
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle , pDecsiption , bitmap);

                }

            }
        });
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {

        String shareBody = pTitle +"\n" + pDescription ;

        Uri uri = saveImageToShare(bitmap);

        //share inteant
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM , uri);
        sIntent.putExtra(Intent.EXTRA_TEXT , shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT , "Subject Here");
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent , "Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {

        File imageFolder = new File(getCacheDir() , "images");
        Uri uri = null ;
        try {

            imageFolder.mkdirs(); // create if not exists
            File file = new File(imageFolder , "shared_images.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG , 90 , stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this , "com.example.firebaseapp.fileprovider" , file);

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return uri ;
    }

    private void shareTextOnly(String pTitle, String pDescription) {

        String shareBody = pTitle +"\n" + pDescription ;

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT , "subject here"); //in case if you want to share throug email
        sIntent.putExtra(Intent.EXTRA_TEXT , shareBody);
        startActivity(Intent.createChooser(sIntent , "Share Via"));
    }


    private void loadComments() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        commentsList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentsList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelComments modelComments = ds.getValue(ModelComments.class);
                    commentsList.add(modelComments);

                    //pass myUid and postId as parameter of constructor of comment adapter
                    adapterComments = new AdapterComments(PostDetailActivity.this , commentsList , myUid , postId);
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void shareMoreOption() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            PopupMenu popupMenu = new PopupMenu(this , moreBtn, Gravity.END);
            //add menu itme in menu

            //show delete option in only posts of currently sing in user
            if (hisUid.equals(myUid)){
                popupMenu.getMenu().add(Menu.NONE , 0 , 0 , "Delete");
                popupMenu.getMenu().add(Menu.NONE , 1 , 0 , "Edit");


            }
            //menu item clicklistner
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == 0 ){
                        //delete is clciked
                        beginDelete();
                    }
                    else if(id == 1 ){
                        //edit is clciked
                        Intent intent = new Intent(PostDetailActivity.this , AddPostActivity.class);
                        intent.putExtra("key" , "editPost");
                        intent.putExtra("editPostId" , postId);
                        startActivity(intent);

                    }
                    return false;
                }
            });
            popupMenu.show();
            //show menu
        }
    }

    private void beginDelete() {
        //POST CAN be with image or without image
        if (pImage.equals("noImage")){
            deleteWithoutImage();
        }else {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");
        pd.show();
        //delete image using uri
        //delete from database using post id
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image delete on delete the database
                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();// remove value from forebase wher pId matache
                                }
                                Toast.makeText(PostDetailActivity.this, "Delete successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dailed cannpt delte the post
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");
        pd.show();
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();// remove value from forebase wher pId matache
                }
                Toast.makeText(PostDetailActivity.this, "Delete successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikes(){
        //when the detail of the user is loading, also check when the current sign in user like the post or not
        final DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("Likes");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postId).hasChild(myUid)){
                    //user has liked the post
                    //to indicate that post is liked by signin user
                    //change drwable left icon of like button
                    //change the text from like to liked
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like , 0 , 0 , 0 );
                    likeBtn.setText("Liked");
                }else {
                    //user has liked the post
                    //to indicate that post is not liked by signin user
                    //change drwable left icon of like button
                    //change the text from liked to like
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black , 0 , 0 , 0 );
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {

        mProcessLikes = true ;
        final DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("Likes");
        final DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts");

        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessLikes){
                    if (dataSnapshot.child(postId).hasChild(myUid)){
                        //alread like
                        postRef.child(postId).child("pLike").setValue(""+(Integer.parseInt(pLikes)-1));
                        likeRef.child(postId).child(myUid).removeValue();
                        mProcessLikes = false ;


                    }else {
                        //not like, liked it
                        postRef.child(postId).child("pLike").setValue(""+(Integer.parseInt(pLikes)+1));
                        likeRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLikes = false ;


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding comment..");

        String comment = commentEt.getText().toString().trim();
        if (TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Please enter comment..", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());
        //each post will have a child "Comments" that contain comments of the post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("cId" , timeStamp);
        hashMap.put("comment" , comment);
        hashMap.put("timeStamp" , timeStamp);
        hashMap.put("uid" , myUid);
        hashMap.put("uEmail" , myEmail);
        hashMap.put("uDp" , myDp);
        hashMap.put("uName" , myName);

        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, "comment added", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateCommentCount() {
        //whenever user adds comment increase the comment count;
        mProcessComment = true;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessComment){
                    String comments = ""+dataSnapshot.child("pComments").getValue();
                    int newCommentValue = Integer.parseInt(comments) + 1 ;
                    ref.child("pComments").setValue(""+newCommentValue);
                    mProcessComment = false ;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    myName = ""+ds.child("name").getValue();
                    myDp = ""+ds.child("image").getValue();

                    //set user Image in comment
                    try {
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_default_image).into(cAvatarIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_image).into(cAvatarIv);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDrscr = ""+ds.child("pDrscr").getValue();
                    pLikes = ""+ds.child("pLike").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String commentCount = ""+ds.child("pComments").getValue();
                    //convert time to proper format
                    //convert date into local
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa" , calendar).toString();

                    //set data
                    pTitleTv.setText(pTitle);
                    uNameTv.setText(hisName);
                    pTimeTv.setText(pTime);
                    pDescriptionTv.setText(pDrscr);
                    pLikeTv.setText(pLikes +" Likes");
                    pCommentsTv.setText(commentCount+" Comments");

                    //set post image
                    if (pImage.equals("noImage")){
                        pImageIv.setVisibility(View.GONE);
                    }else {
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).placeholder(R.drawable.ic_default_image).into(pImageIv);
                        }catch (Exception e){
                            Picasso.get().load(R.drawable.ic_default_image).into(pImageIv);

                        }
                    }



                    //set user Image in comment
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_image).into(uPictureIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_image).into(uPictureIv);

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus(){

        //getCurrentUSER
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if ( user != null){
            //user is signin stay here
            myUid = user.getUid();
            myEmail = user.getEmail();
        }else {
            //user not sign in go to main activity
            startActivity(new Intent(this , MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main , menu);

        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if ( id == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
