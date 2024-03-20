    package com.example.insta;

    import android.view.View;
    import android.widget.ImageView;

    import androidx.annotation.NonNull;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    public class Post {
        public String uid;
        public String author;
        public String authorPhotoUrl;
        public String content;
        public String mediaUrl;
        public String mediaType;
        public Map<String, Boolean> likes = new HashMap<>();
        public long timeStamp;
        public List<Comment> comments = new ArrayList<>();
        // Constructor vacio requerido por Firestore
        public Post() {}

        public Post(String uid, String author, String authorPhotoUrl, String
                content, long timeStamp,String mediaUrl, String mediaType) {
            this.uid = uid;
            this.author = author;
            this.authorPhotoUrl = authorPhotoUrl;
            this.content = content;
            this.mediaUrl = mediaUrl;
            this.mediaType = mediaType;
            this.timeStamp= timeStamp;
        }

    }
