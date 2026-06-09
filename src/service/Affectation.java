package service;
import model.Encadrant;
import model.*;
import java.util.Map;
import java.util.*;


public class Affectation {
    public Map<Encadrant, List<Etudiant>> affectation(
            List<Etudiant> etudiants,
            List<Professeur> professeurs) {

        if (professeurs == null) {
            throw new IllegalArgumentException("La liste des professeurs est vide");
        }
        if(etudiants==null){
            throw new IllegalArgumentException("La liste des étudiants est null");
        }
        Map<Encadrant, List<Etudiant>> affectation = new LinkedHashMap<>();
        List<Encadrant> profsInfo = new ArrayList<>();

        for (Professeur p : professeurs) {
            if (p == null) continue;
            if ("informatique".equalsIgnoreCase(p.getDepartement())) {

                Encadrant enc=new Encadrant (p.getId(),p.getNom(),p.getPrenom(),
                        p.getDepartement(),p.getSpecialite()

                );
                profsInfo.add(enc);
                affectation.put(enc,new ArrayList<>());
            }
        }
        if (profsInfo.isEmpty()) {
            throw new IllegalStateException("Aucun professeur disponible" +
                    " pour l'affectation");
        }
        if (etudiants == null || etudiants.isEmpty()) {
            return affectation;
        }
        int i = 0;

        for (Etudiant e : etudiants) {
            Encadrant enc = profsInfo.get(i);
            affectation.get(enc).add(e);
            i = (i + 1) % profsInfo.size();
        }

        return affectation;
    }

    public int maxAffectation(Map<Encadrant, List<Etudiant>> affectation) {
        int max = 0;

        for (List<Etudiant> liste : affectation.values()) {
            if (liste != null && liste.size() > max) {
                max = liste.size();
            }
        }

        return max;
    }


}
