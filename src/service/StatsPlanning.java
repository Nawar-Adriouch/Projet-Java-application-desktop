package service;

import model.*;
import java.time.LocalDate;
import java.util.*;
import model.Planning;
import model.Soutenance;
import model.Professeur;

public class StatsPlanning {

    // ─────────────────────────────────────────────
    // 1. CHARGE PAR PROFESSEUR
    //    Retourne : nom complet → nombre total de soutenances (enc + jury)
    // ─────────────────────────────────────────────
    public static Map<String, Integer> chargeParProf(Planning planning) {
        Map<String, Integer> total = new LinkedHashMap<>();
        for (Soutenance s : planning.getSoutenances()) {
            List<Professeur> profs = List.of(
                    s.getEncadrant(), s.getJury1(), s.getJury2()
            );
            for (Professeur p : profs) {
                String key = p.getNom() + " " + p.getPrenom();
                total.put(key, total.getOrDefault(key, 0) + 1);
            }
        }
        // Tri décroissant par charge
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(total.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());
        Map<String, Integer> trie = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : entries) trie.put(e.getKey(), e.getValue());
        return trie;
    }

    // ─────────────────────────────────────────────
    // 2. RÉPARTITION PAR FILIÈRE
    //    Retourne : filière → nombre d'étudiants soutenus
    // ─────────────────────────────────────────────
    public static Map<String, Integer> repartitionParFiliere(Planning planning) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Soutenance s : planning.getSoutenances()) {
            String filiere = s.getEtudiant().getFiliere();
            if (filiere == null || filiere.isBlank()) filiere = "Non définie";
            map.put(filiere, map.getOrDefault(filiere, 0) + 1);
        }
        return map;
    }

    // ─────────────────────────────────────────────
    // 3. SOUTENANCES PAR JOUR
    //    Retourne : date → nombre de soutenances ce jour
    // ─────────────────────────────────────────────
    public static Map<LocalDate, Integer> soutenancesParJour(Planning planning) {
        Map<LocalDate, Integer> map = new TreeMap<>(); // TreeMap = trié par date
        for (Soutenance s : planning.getSoutenances()) {
            LocalDate date = s.getCreneau().getDate();
            map.put(date, map.getOrDefault(date, 0) + 1);
        }
        return map;
    }

    // ─────────────────────────────────────────────
    // 4. ÉCART MIN/MAX JURYS
    //    Retourne une map avec "max", "min", "ecart", "moyenne"
    //    + la map détaillée nom → count jury
    // ─────────────────────────────────────────────
    public static Map<String, Integer> chargeJurys(Planning planning) {
        Map<String, Integer> juryCount = new LinkedHashMap<>();
        for (Soutenance s : planning.getSoutenances()) {
            for (Professeur p : List.of(s.getJury1(), s.getJury2())) {
                String key = p.getNom() + " " + p.getPrenom();
                juryCount.put(key, juryCount.getOrDefault(key, 0) + 1);
            }
        }
        return juryCount;
    }

    public static Map<String, Integer> ecartJurys(Planning planning) {
        Map<String, Integer> juryCount = chargeJurys(planning);
        if (juryCount.isEmpty()) return Map.of("max", 0, "min", 0, "ecart", 0, "moyenne", 0);

        int max = Collections.max(juryCount.values());
        int min = Collections.min(juryCount.values());
        int total = juryCount.values().stream().mapToInt(Integer::intValue).sum();
        int moyenne = juryCount.isEmpty() ? 0 : total / juryCount.size();

        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("max", max);
        result.put("min", min);
        result.put("ecart", max - min);
        result.put("moyenne", moyenne);
        return result;
    }

    // ─────────────────────────────────────────────
    // 5. TAUX D'OCCUPATION DES SALLES
    //    Retourne : nomSalle → nombre de créneaux utilisés
    // ─────────────────────────────────────────────
    public static Map<String, Integer> occupationSalles(Planning planning) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Soutenance s : planning.getSoutenances()) {
            String salle = s.getSalle().getNom();
            map.put(salle, map.getOrDefault(salle, 0) + 1);
        }
        // Tri décroissant
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());
        Map<String, Integer> trie = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : entries) trie.put(e.getKey(), e.getValue());
        return trie;
    }

    // ─────────────────────────────────────────────
    // MÉTHODE GLOBALE (console) — conservée pour compatibilité
    // ─────────────────────────────────────────────
    public static void analyser(Planning planning) {
        System.out.println("\n===== CHARGE PAR PROFESSEUR =====");
        chargeParProf(planning).forEach((k, v) ->
                System.out.println("  " + k + " : " + v + " soutenance(s)"));

        System.out.println("\n===== RÉPARTITION PAR FILIÈRE =====");
        repartitionParFiliere(planning).forEach((k, v) ->
                System.out.println("  " + k + " : " + v));

        System.out.println("\n===== SOUTENANCES PAR JOUR =====");
        soutenancesParJour(planning).forEach((k, v) ->
                System.out.println("  " + k + " : " + v));

        System.out.println("\n===== ÉCART JURYS =====");
        ecartJurys(planning).forEach((k, v) ->
                System.out.println("  " + k + " : " + v));

        System.out.println("\n===== OCCUPATION SALLES =====");
        occupationSalles(planning).forEach((k, v) ->
                System.out.println("  " + k + " : " + v));
    }
}