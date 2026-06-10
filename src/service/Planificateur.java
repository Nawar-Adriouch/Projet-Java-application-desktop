package service;
import java.util.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import model.*;

public class Planificateur {

    private List<Creneau>    creneaux;
    private List<Salle>      salles;
    private List<Professeur> professeurs;
    private Planning         planning;
    private Map<Professeur, Integer> charge      = new HashMap<>();
    private Map<Professeur, Integer> poids       = new HashMap<>();
    private Map<Professeur, Integer> chargeCible = new HashMap<>();

    public Planificateur(List<Creneau> creneaux, List<Salle> salles, List<Professeur> professeurs) {
        this.creneaux    = creneaux;
        this.salles      = salles;
        this.professeurs = professeurs;
        this.planning    = new Planning();
        for (Professeur p : professeurs) {
            charge.put(p, 0);
            poids.put(p, 1);
        }
    }

    public List<Creneau>    getCreneaux()     { return creneaux; }
    public List<Salle>      getSalles()       { return salles; }
    public List<Professeur> getProfesseurs()  { return professeurs; }
    public Planning         getPlanning()     { return planning; }
    public Map<Professeur, Integer> getCharge()      { return charge; }
    public Map<Professeur, Integer> getPoids()       { return poids; }
    public Map<Professeur, Integer> getChargeCible() { return chargeCible; }

    public void initialiserModele(Map<Encadrant, List<Etudiant>> affectation) {
        for (Map.Entry<Encadrant, List<Etudiant>> entry : affectation.entrySet()) {
            poids.put(entry.getKey(), 2 + entry.getValue().size());
        }
        int total = 0;
        for (List<Etudiant> list : affectation.values()) total += list.size();
        int sommePoids = 0;
        for (Professeur p : professeurs) sommePoids += poids.getOrDefault(p, 1);
        for (Professeur p : professeurs) {
            int cible = (int) Math.ceil(
                    (double) total * poids.getOrDefault(p, 1) / sommePoids);
            chargeCible.put(p, cible);
        }
    }

    public void updateCharge(Professeur p) {
        charge.put(p, charge.getOrDefault(p, 0) + 1);
    }


    public int scoreProfesseur(Professeur p, Etudiant e, Creneau c, Encadrant encadrant) {

        if (p.getId().equals(encadrant.getId())) return Integer.MAX_VALUE;
        if (!estDisponible(p, c))                return Integer.MAX_VALUE;

        int score = 0;

        score += charge.getOrDefault(p, 0) * 10;


        if (e.isAnglophone() && p.estAnglais())   score -= 50;
        if (!e.isAnglophone() && p.estAnglais())  score += 30;

        for (Soutenance s : planning.getSoutenances()) {
            if (s.getCreneau().estSuccessif(c) &&
                    (s.getJury1().getId().equals(p.getId())    ||
                            s.getJury2().getId().equals(p.getId())    ||
                            s.getEncadrant().getId().equals(p.getId()))) {
                score += 20;
            }
        }

        return score;
    }

    public List<Professeur> chercherJuryEquitable(Etudiant e, Creneau c, Encadrant encadrant) {

        List<Professeur> candidats = new ArrayList<>();

        for (Professeur p : professeurs) {
            if (p.getId().equals(encadrant.getId())) continue;
            if (!estDisponible(p, c))                continue;
            candidats.add(p);
        }

        candidats.sort(Comparator.comparingInt(p -> scoreProfesseur(p, e, c, encadrant)));

        List<Professeur> jurys = new ArrayList<>();
        for (Professeur p : candidats) {
            jurys.add(p);
            if (jurys.size() == 2) break;
        }

        return jurys;
    }


    public Planning plannifier(Map<Encadrant, List<Etudiant>> affectation) {
        initialiserModele(affectation);
        Planning meilleurPlanning = null;
        int maxSoutenances = 0;

        for (int attempt = 0; attempt < 50; attempt++) {
            this.planning = new Planning();
            charge.clear();
            for (Professeur p : professeurs) charge.put(p, 0);

            Collections.shuffle(creneaux);
            Collections.shuffle(professeurs);

            Planning resultat = executerPlanification(affectation);
            int nb = resultat.getSoutenances().size();
            System.out.println("Tentative " + attempt + " : " + nb);

            if (nb > maxSoutenances) {
                maxSoutenances = nb;
                meilleurPlanning = new Planning();
                for (Soutenance s : resultat.getSoutenances())
                    meilleurPlanning.ajouterSoutenance(s);
            }
        }

        System.out.println("Meilleur planning : " + maxSoutenances);
        return meilleurPlanning;
    }

    public Planning executerPlanification(Map<Encadrant, List<Etudiant>> affectation) {
        for (Map.Entry<Encadrant, List<Etudiant>> entry : affectation.entrySet()) {
            Professeur prof = entry.getKey();
            Encadrant encadrant = new Encadrant(
                    prof.getId(), prof.getNom(), prof.getPrenom(),
                    prof.getDepartement(), prof.getSpecialite());

            for (Etudiant e : entry.getValue()) {
                boolean planifie = false;

                for (Creneau c : creneaux) {
                    if (!estDisponible(encadrant, c)) continue;

                    Salle salle = salleDisponible(c);
                    if (salle == null) continue;

                    List<Professeur> jurys = chercherJuryEquitable(e, c, encadrant);

                    if (jurys.size() == 2) {
                        planning.ajouterSoutenance(new Soutenance(
                                e, encadrant, jurys.get(0), jurys.get(1), salle, c));
                        updateCharge(encadrant);
                        updateCharge(jurys.get(0));
                        updateCharge(jurys.get(1));
                        planifie = true;
                        break;
                    }
                }

                if (!planifie)
                    System.out.println("Non planifié : " + e.getNom());
            }
        }
        return planning;
    }


    public boolean estDisponible(Professeur p, Creneau c) {
        for (Soutenance s : planning.getSoutenances()) {
            boolean estDedans =
                    p.getId().equals(s.getEncadrant().getId()) ||
                            p.getId().equals(s.getJury1().getId())     ||
                            p.getId().equals(s.getJury2().getId());

            if (estDedans) {
                boolean memeCreneau =
                        s.getCreneau().getDate().equals(c.getDate()) &&
                                s.getCreneau().getHeureDebut().equals(c.getHeureDebut());

                if (memeCreneau || s.getCreneau().estSuccessif(c)) return false;
            }
        }
        return true;
    }

    public Salle salleDisponible(Creneau c) {
        for (Salle S : salles) {
            boolean occupee = false;
            for (Soutenance s : planning.getSoutenances()) {
                if (s.getCreneau().getDate().equals(c.getDate()) &&
                        s.getCreneau().getHeureDebut().equals(c.getHeureDebut()) &&
                        s.getSalle().getId().equals(S.getId())) {
                    occupee = true;
                    break;
                }
            }
            if (!occupee) return S;
        }
        return null;
    }

    public List<Professeur> chercherJury(Etudiant e, Creneau c, Encadrant encadrant) {
        return chercherJuryEquitable(e, c, encadrant);
    }
}