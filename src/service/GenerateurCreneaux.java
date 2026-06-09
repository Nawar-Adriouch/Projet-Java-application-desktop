package service;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.*;
import model.*;

public class GenerateurCreneaux {

    public int verifierCapacite(int nombreJours, int nombreEtudiants, int nombreSalles) {
        int capaciteTotal = nombreJours * 8 * nombreSalles;
        if (capaciteTotal >= nombreEtudiants) {
            return 0; // suffisant
        } else {
            return (int) Math.ceil((double) nombreEtudiants / (8.0 * nombreSalles));
        }
    }

    public List<Creneau> generer(LocalDate dateDebut, int nombreJours) {
        ArrayList<Creneau> list = new ArrayList<>();
        LocalTime[] heures = {
                LocalTime.of(8,  30), LocalTime.of(9,  30),
                LocalTime.of(10, 30), LocalTime.of(11, 30),
                LocalTime.of(14, 30), LocalTime.of(15, 30),
                LocalTime.of(16, 30), LocalTime.of(17, 30)
        };
        for (int i = 0; i < nombreJours; i++) {
            LocalDate date = dateDebut.plusDays(i);
            for (LocalTime h : heures) {
                list.add(new Creneau(date, h, h.plusHours(1)));
            }
        }
        return list;
    }
}