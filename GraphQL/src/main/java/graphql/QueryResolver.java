Query resolver :
package graphql;

import business.UniteEnseignementBusiness;
import com.coxautodev.graphql.tools.GraphQLRootResolver;
import entities.UniteEnseignement;

import java.util.List;

public class QueryResolver implements GraphQLRootResolver {

    private final UniteEnseignementBusiness helper;

    public QueryResolver() {
        helper = new UniteEnseignementBusiness();
    }

    public List<UniteEnseignement> allUEs() {
        return helper.getListeUE();
    }

    public UniteEnseignement ueByCode(int code) {
        return helper.getUEByCode(code);
    }

    public List<UniteEnseignement> uesByDomaine(String domaine) {
        return helper.getUEByDomaine(domaine);
    }

    public List<UniteEnseignement> uesBySemestre(int semestre) {
        return helper.getUEBySemestre(semestre);
    }
}
