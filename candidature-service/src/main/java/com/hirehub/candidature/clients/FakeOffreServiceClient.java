package com.hirehub.candidature.clients;



import org.springframework.context.annotation.Profile;

import org.springframework.stereotype.Component;



import java.util.HashMap;

import java.util.Map;



/**

 * Faux client local pour simuler offre-service pendant le développement.

 * Actif uniquement avec le profil `sandbox`.

 */

@Component

@Profile("sandbox")

public class FakeOffreServiceClient implements IOffreServiceClient {



    private final Map<String, OffreDTO> offers = new HashMap<>();



    public FakeOffreServiceClient() {

        offers.put("1001", OffreDTO.sandbox(

                "1001",

                "Développeur Java Spring Boot",

                "Créer et maintenir des microservices HireHub",

                "recruteur-1",

                true));



        offers.put("1002", OffreDTO.sandbox(

                "1002",

                "Développeur Frontend Angular",

                "Intégration frontend avec les API microservices",

                "recruteur-2",

                true));



        offers.put("1003", OffreDTO.sandbox(

                "1003",

                "Offre brouillon non publiée",

                "Cette offre sert à tester le cas non publié",

                "recruteur-1",

                false));



        offers.put("4", OffreDTO.sandbox(

                "4",

                "Offre de test #4",

                "Offre publiee pour tests locaux",

                "recruteur-1",

                true));

    }



    @Override

    public OffreDTO getOffre(String id) {

        return offers.get(id);

    }



    @Override

    public boolean offreExists(String id) {

        OffreDTO offre = offers.get(id);

        return offre != null && offre.isPublished();

    }



    @Override

    public boolean isRecruteurOwner(String offreId, String recruteurId) {

        OffreDTO offre = offers.get(offreId);

        return offre != null && offre.isPublished() && recruteurId != null && recruteurId.equals(offre.getRecruteurId());

    }

}

