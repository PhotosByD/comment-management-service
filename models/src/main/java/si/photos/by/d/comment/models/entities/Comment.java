package si.photos.by.d.comment.models.entities;

import javax.persistence.*;
import java.util.List;

@Entity(name="comment")
@NamedQueries(value =
        {
                @NamedQuery(name = "Comment.getAll", query = "SELECT a FROM comment a")
        })
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "photo_id")
    private Integer photoId;

    @Column(name = "comment_body")
    private String body;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Integer photoId) {
        this.photoId = photoId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}