package service;

import model.*;
import java.time.LocalDate;
import java.util.*;

public class StatsPlanning {


    //charge professeur

    public static Map<String, Integer> chargeParProf(Planning planning) {
        Map<String, Integer> countParId = new LinkedHashMap<>();
        Map<String, String> nomParId = new LinkedHashMap<>();

        for (Soutenance s : planning.getSoutenances()) {
            Professeur[] profs = {
                    s.getEncadrant(),
                    s.getJury1(),
                    s.getJury2()
            };

            for (Professeur p : profs) {
                String id = p.getId();
                String nom = p.getNom() + " " + p.getPrenom();
                countParId.put(id, countParId.getOrDefault(id, 0) + 1);
                nomParId.put(id, nom);
            }
        }
        Map<String, Integer> total = new LinkedHashMap<>();
        for (String id : countParId.keySet())
            total.put(nomParId.get(id), countParId.get(id));
        return total;

    }

    //repartition par filiere

    public static Map<String, Integer> repartitionParFiliere(Planning planning) {

        Map<String, Integer> resultat = new LinkedHashMap<>();
        for (Soutenance s : planning.getSoutenances()) {
            String filiere = s.getEtudiant().getFiliere();

            if (filiere == null || filiere.equals("")) {
                filiere = "Non définie";
            }

            if (resultat.containsKey(filiere)) {
                resultat.put(filiere, resultat.get(filiere) + 1);
            } else {
                resultat.put(filiere, 1);
            }
        }
        return resultat;
    }

    // nb soutenance par jour
    public static Map<LocalDate, Integer> soutenancesParJour(Planning planning) {

        Map<LocalDate, Integer> resultat = new TreeMap<>();

        for (Soutenance s : planning.getSoutenances()) {
            LocalDate date = s.getCreneau().getDate();

            if (resultat.containsKey(date)) {
                resultat.put(date, resultat.get(date) + 1);
            } else {
                resultat.put(date, 1);
            }
        }
        return resultat;
    }


    // charge de jurys
    public static Map<String, Integer> chargeJurys(Planning planning) {
        Map<String, Integer> countParId = new LinkedHashMap<>();
        Map<String, String>  nomParId   = new LinkedHashMap<>();

        for (Soutenance s : planning.getSoutenances()) {
            for (Professeur p : new Professeur[]{s.getJury1(), s.getJury2()}) {
                String id  = p.getId();
                String nom = p.getNom() + " " + p.getPrenom();
                countParId.put(id, countParId.getOrDefault(id, 0) + 1);
                nomParId.put(id, nom);
            }
        }

        Map<String, Integer> resultat = new LinkedHashMap<>();
        for (String id : countParId.keySet())
            resultat.put(nomParId.get(id), countParId.get(id));

        return resultat;
    }


    //  ecart de charge par jury
    public static Map<String, Integer> ecartJurys(Planning planning) {

        Map<String, Integer> charges = chargeJurys(planning);

        int max = 0;
        int min = Integer.MAX_VALUE;
        int somme = 0;

        for (Integer valeur : charges.values()) {
            if (valeur > max) {
                max = valeur;
            }
            if (valeur < min) {
                min = valeur;
            }
            somme += valeur;
        }
        int moyenne = 0;
        if (charges.size() > 0) {
            moyenne = somme / charges.size();
        } else {
            min = 0;
        }

        int ecart = max - min;

        Map<String, Integer> resultat = new LinkedHashMap<>();
        resultat.put("max", max);
        resultat.put("min", min);
        resultat.put("ecart", ecart);
        resultat.put("moyenne", moyenne);

        return resultat;
    }

    // OCCUPATION DES SALLES

    public static Map<String, Integer> occupationSalles(Planning planning) {

        Map<String, Integer> resultat = new LinkedHashMap<>();

        for (Soutenance s : planning.getSoutenances()) {

            String salle = s.getSalle().getNom();

            if (resultat.containsKey(salle)) {
                resultat.put(salle, resultat.get(salle) + 1);
            } else {
                resultat.put(salle, 1);
            }
        }

        return resultat;
    }


    public static void analyser(Planning planning) {

        System.out.println("\nCHARGE PAR PROFESSEUR ");
        for (Map.Entry<String, Integer> e : chargeParProf(planning).entrySet()) {
            System.out.println(e.getKey() + " : " + e.getValue());
        }

        System.out.println("REPARTITION PAR FILIERE");
        for (Map.Entry<String, Integer> e : repartitionParFiliere(planning).entrySet()) {
            System.out.println(e.getKey() + " : " + e.getValue());
        }

        System.out.println("SOUTENANCES PAR JOUR ");
        for (Map.Entry<LocalDate, Integer> e : soutenancesParJour(planning).entrySet()) {
            System.out.println(e.getKey() + " : " + e.getValue());
        }

        System.out.println(" CHARGE DES JURYS");
        for (Map.Entry<String, Integer> e : chargeJurys(planning).entrySet()) {
            System.out.println(e.getKey() + " : " + e.getValue());
        }

        System.out.println(" ECART DES JURYS ");
        for (Map.Entry<String, Integer> e : ecartJurys(planning).entrySet()) {
            System.out.println(e.getKey() + " : " + e.getValue());
        }

        System.out.println(" OCCUPATION DES SALLES ");
        for (Map.Entry<String, Integer> e : occupationSalles(planning).entrySet()) {
            System.out.println(e.getKey() + " : " + e.getValue());
        }
    }
}