package si.photos.by.d.comment.api.v1.resources;

import com.kumuluz.ee.logs.cdi.Log;
import si.photos.by.d.comment.models.entities.Comment;
import si.photos.by.d.comment.services.beans.CommentBean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Log
@ApplicationScoped
@Path("/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommentResource {
    @Inject
    private CommentBean commentBean;

    @Context
    UriInfo uriInfo;

    @GET
    public Response getComments() {
        List<Comment> comments = commentBean.getComments();

        return Response.ok(comments).build();
    }

    @GET
    @Path("/filtered")
    public Response getCommentsFiltered() {
        List<Comment> comments;

        comments = commentBean.getCommentsFilter(uriInfo);

        return Response.status(Response.Status.OK).entity(comments).build();
    }

    @GET
    @Path("/{commentId}")
    public Response getComment(@PathParam("commentId") Integer commentId) {
        Comment comment = commentBean.getComment(commentId);

        if(comment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(comment).build();
    }

    @POST
    public Response createCustomer(Comment comment) {

        if ( comment.getBody().isEmpty() || (comment.getPhotoId() == null) || (comment.getUserId() == null)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            comment = commentBean.createComment(comment);
        }
        if (comment.getId() != null) {
            return Response.status(Response.Status.CREATED).entity(comment).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(comment).build();
        }
    }

    @PUT
    @Path("{commentId}")
    public Response updateComment(@PathParam("commentId") Integer commentId, Comment comment) {

        comment = commentBean.updateComment(commentId, comment);

        if (comment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (comment.getId() != null)
                return Response.status(Response.Status.OK).entity(comment).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("{commentId}")
    public Response deleteCustomer(@PathParam("commentId") Integer commentId) {

        boolean deleted = commentBean.deleteComment(commentId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}