package webservices;

import entities.UniteEnseignement;
import metiers.UniteEnseignementBusiness;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ue")
public class UniteEnsRestApi {

    private static final UniteEnseignementBusiness helper = new UniteEnseignementBusiness();

    @Path("/getListe")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListe() {
        return Response.ok(helper.getListeUE()).build();
    }

    @Path("/getById/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") int id) {
        UniteEnseignement ue = helper.getUEByCode(id);
        if (ue == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucune UE avec le code " + id).build();
        }
        return Response.ok(ue).build();
    }

    @Path("/getByDomaine/{domaine}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByDomaine(@PathParam("domaine") String domaine) {
        return Response.ok(helper.getUEByDomaine(domaine)).build();
    }

    @Path("/getBySemestre/{semestre}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBySemestre(@PathParam("semestre") int semestre) {
        return Response.ok(helper.getUEBySemestre(semestre)).build();
    }

    @Path("/addEns")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUE(UniteEnseignement ue) {
        if (ue == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Corps JSON manquant ou invalide").build();
        }
        helper.addUniteEnseignement(ue);
        return Response.status(Response.Status.CREATED).entity(ue).build();
    }

    @Path("/updateEns/{code}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUE(@PathParam("code") int code, UniteEnseignement ue) {
        ue.setCode(code);
        boolean updated = helper.updateUniteEnseignement(code, ue);
        if (!updated) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucune UE avec le code " + code).build();
        }
        return Response.ok(ue).build();
    }

    @Path("/deleteEns/{id}")
    @DELETE
    public Response deleteUE(@PathParam("id") int id) {
        boolean deleted = helper.deleteUniteEnseignement(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucune UE avec le code " + id).build();
        }
        return Response.noContent().build();
    }

}
