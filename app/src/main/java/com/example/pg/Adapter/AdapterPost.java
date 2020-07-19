package com.example.pg.Adapter;



import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import com.example.pg.AddPostActivity;
import com.example.pg.ImageDetailActivity;
import com.example.pg.Model.ModelPost;
import com.example.pg.PostDetailActivity;
import com.example.pg.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder>{

    Context context;
    List<ModelPost> postList;
    String myUid;


    private DatabaseReference likeRef ;
    private DatabaseReference postRef ;
    boolean mProcessLikes = false ;


    public AdapterPost(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeRef = FirebaseDatabase.getInstance().getReference("Likes");
        postRef = FirebaseDatabase.getInstance().getReference("Posts");

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.new_posts , parent , false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        final String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        final String pId = postList.get(position).getpId();
        final String pTitle = postList.get(position).getpTitle();
        final String pDescription = postList.get(position).getpDrscr();
        final String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLike(); //conatin total number of like for a post
        String pComments = postList.get(position).getpComments(); //conatin total number of comments for a post



        //convert date into local
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa" , calendar).toString();

        //set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.plikeTv.setText(pLikes +" Likes"); //e.g 100 likes
        holder.pCommetnsTv.setText(pComments+" Comments");

        setLikes(holder , pId);
        //setUserDp
        try {

            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_image).into(holder.uPictureIv);
        }catch (Exception e){

        }

        holder.progressbar.setVisibility(View.VISIBLE);
        //set post image
        if (pImage.equals("noImage")){
            holder.pImageIv.setVisibility(View.GONE);
        }else {

            holder.pImageIv.setVisibility(View.VISIBLE);


            try {
                Glide.with(context)
                        .load(pImage)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.progressbar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.progressbar.setVisibility(View.GONE);
                                return false;
                            }

                        })
                        .into(holder.pImageIv);
                //Picasso.get().load(pImage).placeholder(R.drawable.ic_default_image).into(holder.pImageIv);
            }catch (Exception e){

            }
        }

        holder.pImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context , ImageDetailActivity.class);
                intent.putExtra("image_url" , postList.get(position).getpImage());
                context.startActivity(intent);
            }
        });

        //handle button click
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareMoreOption(holder.moreBtn , uid , myUid , pId , pImage);
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProcessLikes = true ;
                final String postId = postList.get(position).getpId();
                final int pLikes = Integer.parseInt(postList.get(position).getpLike());

                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mProcessLikes){
                            if (dataSnapshot.child(postId).hasChild(myUid)){
                                //alread like
                                postRef.child(postId).child("pLike").setValue(""+(pLikes-1));
                                likeRef.child(postId).child(myUid).removeValue();
                                mProcessLikes = false ;
                            }else {
                                //not like, liked it
                                postRef.child(postId).child("pLike").setValue(""+(pLikes+1));
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
        });
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context , PostDetailActivity.class);
                intent.putExtra("postId" , pId); //we will get the detail of post from this post id
                context.startActivity(intent);
            }
        });
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BitmapDrawable bitmapDrawable = (BitmapDrawable)holder.pImageIv.getDrawable();
                if (bitmapDrawable == null){
                    //share text only
                    shareTextOnly(pTitle , pDescription);
                }else {
                    //post with image

                    //convert image to bit map
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle , pDescription , bitmap);

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
        context.startActivity(Intent.createChooser(sIntent , "Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {

        File imageFolder = new File(context.getCacheDir() , "images");
        Uri uri = null ;
        try {

            imageFolder.mkdirs(); // create if not exists
            File file = new File(imageFolder , "shared_images.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG , 90 , stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context , "com.example.pg" , file);

        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return uri ;
    }

    private void shareTextOnly(String pTitle, String pDescription) {

        String shareBody = pTitle +"\n" + pDescription ;

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT , "subject here"); //in case if you want to share throug email
        sIntent.putExtra(Intent.EXTRA_TEXT , shareBody);
        context.startActivity(Intent.createChooser(sIntent , "Share Via"));
    }

    private void setLikes(final MyHolder holder, final String postKey) {
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postKey).hasChild(myUid)){
                    //user has liked the post
                    //to indicate that post is liked by signin user
                    //change drwable left icon of like button
                    //change the text from like to liked
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like , 0 , 0 , 0 );
                    holder.likeBtn.setText("Liked");
                }else {
                    //user has liked the post
                    //to indicate that post is not liked by signin user
                    //change drwable left icon of like button
                    //change the text from liked to like
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black , 0 , 0 , 0 );
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void shareMoreOption(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            PopupMenu popupMenu = new PopupMenu(context , moreBtn, Gravity.END);
            //add menu itme in menu

            //show delete option in only posts of currently sing in user
            if (uid.equals(myUid)){
                popupMenu.getMenu().add(Menu.NONE , 0 , 0 , "Delete");
                popupMenu.getMenu().add(Menu.NONE , 1 , 0 , "Edit");


            }
            popupMenu.getMenu().add(Menu.NONE , 2 , 0 , "View Detail");
            //menu item clicklistner
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == 0 ){
                        //delete is clciked
                        beginDelete(pId , pImage);
                    }
                    else if(id == 1 ){
                        //edit is clciked
                        Intent intent = new Intent(context , AddPostActivity.class);
                        intent.putExtra("key" , "editPost");
                        intent.putExtra("editPostId" , pId);
                        context.startActivity(intent);

                    }else if (id==2){
                        Intent intent = new Intent(context , PostDetailActivity.class);
                        intent.putExtra("postId" , pId); //we will get the detail of post from this post id
                        context.startActivity(intent);
                    }
                    return false;
                }
            });
            popupMenu.show();
            //show menu
        }

    }

    private void beginDelete(String pId, String pImage) {
        //POST CAN be with image or without image
        if (pImage.equals("noImage")){
            deleteWithoutImage(pId);
        }else {
            deleteWithImage(pId , pImage);
        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        final ProgressDialog pd = new ProgressDialog(context);
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
                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();// remove value from forebase wher pId matache
                                }
                                Toast.makeText(context, "Delete successfully", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");
        pd.show();
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();// remove value from forebase wher pId matache
                }
                Toast.makeText(context, "Delete successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView uPictureIv, pImageIv ;
        TextView uNameTv ,pTimeTv , pTitleTv, pDescriptionTv , plikeTv, pCommetnsTv;
        ImageButton moreBtn;
        Button likeBtn , commentBtn ,shareBtn ;
        LinearLayout profileLayout ;
        ProgressBar progressbar;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            uPictureIv =itemView.findViewById(R.id.uPictureIv);
            pImageIv =itemView.findViewById(R.id.pImageIv);
            uNameTv =itemView.findViewById(R.id.uNameTv);
            pTimeTv =itemView.findViewById(R.id.pTimeTv);
            pTitleTv =itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv =itemView.findViewById(R.id.pDescriptionTv);
            plikeTv =itemView.findViewById(R.id.pLikeTv);
            moreBtn =itemView.findViewById(R.id.moreBtn);
            likeBtn =itemView.findViewById(R.id.likeBTN);
            commentBtn =itemView.findViewById(R.id.commentBtn);
            shareBtn =itemView.findViewById(R.id.shareBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);
            pCommetnsTv = itemView.findViewById(R.id.pCommentsTv);
            progressbar = itemView.findViewById(R.id.progressbar);

        }
    }
}
