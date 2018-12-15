package si.photos.by.d.comment.services.beans;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import si.photos.by.d.comment.services.configuration.AppProperties;
import si.photos.by.d.comment.models.entities.Comment;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;




@ApplicationScoped
public class CommentBean {
    private Logger log = Logger.getLogger(CommentBean.class.getName());

    @Inject
    private EntityManager em;

    @Inject
    private AppProperties appProperties;

    private Client httpClient;

    @PostConstruct
    private void init() {
        // This here will connect to photo service and get me photos for comment
        httpClient = ClientBuilder.newClient();
        //photoUrl = "http://localhost:8081"; // only for demonstration
    }

    public List<Comment> getComments() {
        TypedQuery<Comment> query = em.createNamedQuery("Comment.getAll", Comment.class);
        return query.getResultList();
    }
    public List<Comment> getCommentsFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, Comment.class, queryParameters);
    }

    public Comment getComment(Integer commentId) {
        Comment comment = em.find(Comment.class, commentId);

        if (comment == null) {
            throw new NotFoundException();
        }

        return comment;
    }

    public Comment createComment(Comment comment) {
        try {
            beginTx();
            em.persist(comment);
            commitTx();
        } catch (Exception e) {
            log.warning("There was a problem with saving new comment for photo " + comment.getPhotoId());
            rollbackTx();
        }
        log.info("Successfully saved new comment for photo" + comment.getPhotoId());
        return comment;
    }

    public Comment updateComment(Integer commentId, Comment comment) {
        Comment u = em.find(Comment.class, commentId);

        if (u == null) return null;

        try {
            beginTx();
            comment.setId(commentId);
            em.merge(comment);
            commitTx();
        } catch (Exception e) {
            log.warning("There was a problem with updating comment for photo " + comment.getPhotoId());
            rollbackTx();
        }
        log.info("Successfully updated comment for photo " + comment.getPhotoId());
        return comment;
    }

    public boolean deleteComment(Integer commentId) {
        Comment comment = em.find(Comment.class, commentId);

        if (comment != null) {
            try {
                beginTx();
                em.remove(comment);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else {
            return false;
        }

        return true;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    private void commitTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().commit();
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();
    }
}