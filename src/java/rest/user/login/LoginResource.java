/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest.user.login;

import domain.LoginUser;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * REST Web Service
 *
 * @author enrico
 */
@Path("rest/user/login")
public class LoginResource {

    @Context
    private UriInfo context;

    public static SessionFactory factory;
    private String msg = "";

    /**
     * Creates a new instance of LoginResource
     */
    public LoginResource() {
    }

    /**
     * Retrieves representation of an instance of rest.user.login.LoginResource
     *
     * @return an instance of domain.LoginUser
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public LoginUser getJson() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of LoginResource
     *
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void putJson(LoginUser content) {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response loginUser(LoginUser loginUser) {
        Session session = factory.openSession();
        try {
            String id = loginUser.getId() + "";
            String password = loginUser.getPassword();

            String hql = "select count(*) from User as user where user.id=:id and user.password=:password";
            Query query = session.createQuery(hql);
            query.setString("id", id);
            query.setString("password", password);
            Long count = (Long) query.uniqueResult();

            if (count == 0) {
                msg = "User not found";
                return Response.status(Response.Status.NO_CONTENT).header("X-User-Not-Found", msg).build();
            }
            return Response.ok(count).build();
        } catch (HibernateException e) {
            msg = e.getLocalizedMessage();
            System.err.println("ERROR: " + msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("X-Hibernate-Error", msg).build();
        } catch (Exception e) {
            msg = e.getLocalizedMessage();
            System.err.println("ERROR: " + msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("X-Unkown-Error", msg).build();
        } finally {
            session.close();
        }
    }
}
