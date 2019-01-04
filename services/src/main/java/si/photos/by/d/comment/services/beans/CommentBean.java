package si.photos.by.d.comment.services.beans;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import si.photos.by.d.comment.models.dtos.User;
import si.photos.by.d.comment.models.entities.Comment;
import si.photos.by.d.comment.services.configuration.AppProperties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;

@RequestScoped
public class CommentBean {
    private Logger log = Logger.getLogger(CommentBean.class.getName());

    @Inject
    private EntityManager em;

    @Inject
    @DiscoverService("user-management-service")
    private Optional<String> userUrl;

    private Client httpClient;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
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

        User user = getUser(comment.getUserId());
        if (user != null) {
            comment.setUser(user.getUsername());
        }

        return comment;
    }

    public List<Comment> getCommentsForUser(Integer id) {
        TypedQuery<Comment> query = em.createQuery("SELECT c FROM comment c WHERE c.userId = :id", Comment.class);
        query.setParameter("id", id);

        return query.getResultList();
    }

    public List<Comment> getCommentsForPhoto(Integer id) {
        TypedQuery<Comment> query = em.createQuery("SELECT c FROM comment c WHERE c.photoId = :id", Comment.class);
        query.setParameter("id", id);
        List<Comment> comments = query.getResultList();
        IntStream.range(0, comments.size()).forEach(i -> {
            User u = getUser(comments.get(i).getUserId());
            if (u != null) {
                Comment c = comments.get(i);
                c.setUser(u.getUsername());
                comments.set(i, c);
            }
        });
        return comments;
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

    private User getUser(Integer userId) {
        if (userUrl.isPresent()) {
            try {
                return httpClient
                        .target(userUrl.get() + "/v1/users/" + userId)
                        .request().get(new GenericType<User>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.severe(e.getMessage());
                throw new InternalServerErrorException(e);
            }
        }
        return null;
    }
}