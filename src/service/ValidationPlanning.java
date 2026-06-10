package service;

import model.*;
import java.time.LocalDateTime;
import java.util.*;

public class ValidationPlanning {

    public static boolean verifierSalle(List<Soutenance> soutenances){
        for(int i=0;i<soutenances.size();i++){
            for(int j=i+1;j<soutenances.size();j++){

                Soutenance a=soutenances.get(i);
                Soutenance b=soutenances.get(j);

                if(a.getSalle().equals(b.getSalle()) && overlap(a,b)){
                    System.out.println("Conflit Salle : "+a.getSalle());
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean verifierProf(List<Soutenance> soutenances){
        for(int i=0;i<soutenances.size();i++){
            for(int j=i+1;j<soutenances.size();j++){

                Soutenance a=soutenances.get(i);
                Soutenance b=soutenances.get(j);

                if(overlap(a,b)){
                    if(memeProf(a,b)){
                        System.out.println("Conflit Prof");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean verifierJury(List<Soutenance> soutenances){
        for(Soutenance s : soutenances){
            if (s.getEncadrant().equals(s.getJury1())
                    || s.getEncadrant().equals(s.getJury2())
                    || s.getJury1().equals(s.getJury2())) {

                System.out.println("ERROR jury pour : " + s.getEtudiant());
                return false;
            }
        }
        return true;
    }

    public static boolean verifierSuccession(List<Soutenance> soutenances) {

        // Map : id du prof → liste de ses créneaux
        Map<String, List<Creneau>> profCreneaux = new HashMap<>();

        for (Soutenance s : soutenances) {
            ajouterCreneau(profCreneaux, s.getEncadrant().getId(), s.getCreneau());
            ajouterCreneau(profCreneaux, s.getJury1().getId(), s.getCreneau());
            ajouterCreneau(profCreneaux, s.getJury2().getId(), s.getCreneau());
        }

        boolean valide = true;

        for (String profId : profCreneaux.keySet()) {
            List<Creneau> creneaux = profCreneaux.get(profId);

            creneaux.sort(Comparator
                    .comparing(Creneau::getDate)
                    .thenComparing(Creneau::getHeureDebut)
            );

            int successionsConsecutives = 0;

            for (int i = 0; i < creneaux.size() - 1; i++) {
                Creneau actuel = creneaux.get(i);
                Creneau suivant = creneaux.get(i + 1);

                boolean memeJour = actuel.getDate().equals(suivant.getDate());
                boolean enchaine = actuel.getHeureFin().equals(suivant.getHeureDebut());

                if (memeJour && enchaine) {
                    successionsConsecutives++;
                    System.out.println("Succession détectée pour prof " + profId +
                            " : " + actuel + " → " + suivant);

                    // Par exemple : interdire plus de 2 soutenances d'affilée
                    if (successionsConsecutives >= 2) {
                        System.out.println("Trop de soutenances successives pour : " + profId);
                        valide = false;
                    }
                } else {
                    successionsConsecutives = 0; // reset si pas successif
                }
            }
        }

        return valide;
    }

    // Méthode helper
    private static void ajouterCreneau(Map<String, List<Creneau>> map, String id, Creneau c) {
        map.computeIfAbsent(id, k -> new ArrayList<>()).add(c);
    }

    private static boolean overlap(Soutenance a, Soutenance b) {
        Creneau c1 =a.getCreneau();
        Creneau c2 = b.getCreneau();

        if (!c1.getDate().equals(c2.getDate())) {
            return false;
        }
        boolean pasDeChevau = c1.getHeureFin().compareTo(c2.getHeureDebut()) <= 0
                || c2.getHeureFin().compareTo(c1.getHeureDebut()) <= 0;

        return !pasDeChevau;
    }

    private static boolean memeProf(Soutenance a, Soutenance b) {
        return a.getEncadrant().equals(b.getEncadrant()) ||
                a.getEncadrant().equals(b.getJury1()) ||
                a.getEncadrant().equals(b.getJury2()) ||
                a.getJury1().equals(b.getEncadrant()) ||
                a.getJury1().equals(b.getJury1()) ||
                a.getJury1().equals(b.getJury2()) ||
                a.getJury2().equals(b.getEncadrant()) ||
                a.getJury2().equals(b.getJury1()) ||
                a.getJury2().equals(b.getJury2());
    }



    public static boolean verifierTout(List<Soutenance> soutenances) {

        return verifierSalle(soutenances)
                && verifierProf(soutenances)
                && verifierJury(soutenances)
                && verifierSuccession(soutenances);
    }
}