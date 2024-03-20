package com.example.insta;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    NavController navController;
    public AppViewModel appViewModel;
    FirebaseFirestore  mFirestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        mFirestore = FirebaseFirestore.getInstance();

       view.findViewById(R.id.gotoNewPostFragmentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.newPostFragment);
            }
        });

        //gestion recylerview
        RecyclerView postsRecyclerView = view.findViewById(R.id.postsRecyclerView);

        Query query = FirebaseFirestore.getInstance().collection("posts").orderBy("timeStamp",Query.Direction.DESCENDING).limit(50);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .setLifecycleOwner(this)
                .build();

        postsRecyclerView.setAdapter(new PostsAdapter(options));
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView authorPhotoImageView;
        TextView authorTextView, contentTextView;


        PostViewHolder(@NonNull View itemView) {
            super(itemView);

            authorPhotoImageView = itemView.findViewById(R.id.photoImageView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
        }
    }

    class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.PostViewHolder> {
        public PostsAdapter(@NonNull FirestoreRecyclerOptions<Post> options) {
            super(options);
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post, parent, false));
        }

        @Override
        protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull final Post post) {
            if (post.authorPhotoUrl == null) {
                holder.authorPhotoImageView.setImageResource(R.drawable.user);
            } else {
                Glide.with(getContext()).load(post.authorPhotoUrl).circleCrop().into(holder.authorPhotoImageView);
            }
            //Glide.with(getContext()).load(post.authorPhotoUrl).circleCrop().into(holder.authorPhotoImageView);
            holder.authorTextView.setText(post.author);
            holder.contentTextView.setText(post.content);

            // Gestion de likes
            final String postKey = getSnapshots().getSnapshot(position).getId();
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (post.likes.containsKey(uid))
                holder.likeImageView.setImageResource(R.drawable.like_on);
            else
                holder.likeImageView.setImageResource(R.drawable.like_off);
            holder.numLikesTextView.setText(String.valueOf(post.likes.size()));
            holder.likeImageView.setOnClickListener(view -> {
                FirebaseFirestore.getInstance().collection("posts")
                        .document(postKey)
                        .update("likes." + uid, post.likes.containsKey(uid) ?
                                FieldValue.delete() : true);
            });

            //Delete al post
            DocumentSnapshot documentSnapshot= getSnapshots().getSnapshot(holder.getAbsoluteAdapterPosition());
            final String id=documentSnapshot.getId();
            holder.deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the current user's ID
                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    // Get the post's user ID
                    DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(holder.getAbsoluteAdapterPosition());
                    String postUserId = documentSnapshot.getString("uid");

                    // Check if the current user is the same as the post's user
                    if (currentUserId.equals(postUserId)) {
                        // Current user is the post creator, so allow the delete
                        mFirestore.collection("posts").document(id).delete();
                    } else {
                        // Current user is not the post creator, so do not allow the delete
                        Toast.makeText(v.getContext(), "You are not authorized to delete this post", Toast.LENGTH_SHORT).show();
                    }
                }

            });

            //Comentario
            // Método para enviar un comentario
           /* private void sendComment(String , String userId, String commentContent) {
                // Aquí agregarías el comentario a la base de datos, asociado al postId
                // Asegúrate de guardar la información relevante, como el contenido del comentario, el ID del usuario, etc.
            }

// Método para cargar los comentarios de un post desde la base de datos
            private void loadComments(String postId) {
                // Aquí cargarías los comentarios asociados al postId desde tu base de datos
                // Luego, actualiza la interfaz de usuario para mostrar los comentarios
            }*/


            //fecha
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(post.timeStamp);

            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            String fecha= format.format(calendar.getTime());
            holder.dateTextView.setText(fecha);

            // Miniatura de media
            if (post.mediaUrl != null) {
                holder.mediaImageView.setVisibility(View.VISIBLE);
                if ("audio".equals(post.mediaType)) {
                    Glide.with(requireView()).load(R.drawable.audio).centerCrop().into(holder.mediaImageView);
                } else {
                    Glide.with(requireView()).load(post.mediaUrl).centerCrop().into(holder.mediaImageView);
                }
                holder.mediaImageView.setOnClickListener(view -> {
                    appViewModel.postSeleccionado.setValue(post);
                    navController.navigate(R.id.mediaFragment);
                });
            } else {
                holder.mediaImageView.setVisibility(View.GONE);
            }
            
        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            ImageView authorPhotoImageView, likeImageView, mediaImageView, deleteImageView;
            TextView authorTextView, contentTextView, numLikesTextView, dateTextView;
            EditText commentEditText;

            PostViewHolder(@NonNull View itemView) {
                super(itemView);

                authorPhotoImageView = itemView.findViewById(R.id.photoImageView);
                likeImageView = itemView.findViewById(R.id.likeImageView);
                commentEditText = itemView.findViewById(R.id.commentInput);
                authorTextView = itemView.findViewById(R.id.authorTextView);
                mediaImageView = itemView.findViewById(R.id.mediaImage);
                contentTextView = itemView.findViewById(R.id.contentTextView);
                numLikesTextView = itemView.findViewById(R.id.numLikesTextView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                deleteImageView = itemView.findViewById(R.id.deleteImageView);
            }
        }
    }
}