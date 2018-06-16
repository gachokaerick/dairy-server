/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest.contract;

import domain.Contract;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 * REST Web Service
 *
 * @author enrico
 */
@Path("rest/contract")
public class ContractResource {

    @Context
    private UriInfo context;

    public static SessionFactory factory;
    private String msg = "";

    /**
     * Creates a new instance of ContractResource
     */
    public ContractResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pending")
    public Response getPendingContracts() {
        Session session = factory.openSession();
        List<Contract> list;
        try {
            Criteria criteria = session.createCriteria(Contract.class);
            criteria.add(Restrictions.eq("status", "pending"));
            criteria.addOrder(Order.asc("startDate"));
            list = criteria.list();
        } catch (HibernateException e) {
            msg = e.getLocalizedMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("X-Hibernate-Error", msg).build();
        } finally {
            session.close();
        }
        GenericEntity<List<Contract>> listWrapper = new GenericEntity<List<Contract>>(list) {
        };
        return Response.ok(listWrapper).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/denied")
    public Response getDeniedContracts() {
        Session session = factory.openSession();
        List<Contract> list;
        try {
            Criteria criteria = session.createCriteria(Contract.class);
            criteria.add(Restrictions.eq("status", "denied"));
            criteria.addOrder(Order.asc("startDate"));
            list = criteria.list();
        } catch (HibernateException e) {
            msg = e.getLocalizedMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("X-Hibernate-Error", msg).build();
        } finally {
            session.close();
        }
        GenericEntity<List<Contract>> listWrapper = new GenericEntity<List<Contract>>(list) {
        };
        return Response.ok(listWrapper).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/approved")
    public Response getApprovedContracts() {
        Session session = factory.openSession();
        List<Contract> list;
        try {
            Criteria criteria = session.createCriteria(Contract.class);
            criteria.add(Restrictions.eq("status", "approved"));
            criteria.addOrder(Order.asc("endDate"));
            list = criteria.list();
        } catch (HibernateException e) {
            msg = e.getLocalizedMessage();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("X-Hibernate-Error", msg).build();
        } finally {
            session.close();
        }
        GenericEntity<List<Contract>> listWrapper = new GenericEntity<List<Contract>>(list) {
        };
        return Response.ok(listWrapper).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response changeStatus(Contract contract) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            int id = contract.getContractId();
            String status = contract.getStatus();  //NOTE: desired status has already been set in the contract class
            String hql = "UPDATE Contract as c set c.status=:status where c.contractId=:id";
            Query query = session.createQuery(hql);
            query.setString("status", status);
            query.setInteger("id", id);
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            msg = e.getLocalizedMessage();
            System.err.println("ERROR: " + msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("X-Hibernate-Error", msg).build();
        } finally {
            session.close();
        }
        msg = "contract modified successfully";
        return Response.ok(msg).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveContract(Contract contract) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(contract);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            System.err.println("Hibernate error occurred: " + e);
            msg = e.getMessage();
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("X-Validation-Failure", msg).build();
        } finally {
            session.close();
        }
        msg = "contract sent successfully";
        return Response.ok(msg).build();
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{contractId: \\d+}")
    public Response deleteContract(final @PathParam("contractId") String contractId) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            int id = Integer.parseInt(contractId);
            tx = session.beginTransaction();
            String hql = "delete from Contract as c where c.contractId=:contractId";
            Query query = session.createQuery(hql);
            query.setInteger("contractId", id);
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            System.err.println("Hibernate error occurred: " + e);
            msg = e.getMessage();
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("X-Validation-Failure", msg).build();
        } finally {
            session.close();
        }
        msg = "contract deleted successfully";
        return Response.ok(msg).build();
    }
}
