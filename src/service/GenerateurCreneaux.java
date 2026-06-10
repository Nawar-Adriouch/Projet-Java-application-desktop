package service;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.*;

import Exceptions.nombreJoursInvalideException;
import model.*;

public class GenerateurCreneaux {

    public void verifierCpacite(int nombreJours,int nombreEtudiants,int nombreSalles )
            throws nombreJoursInvalideException {
        int capaciteTotal=nombreJours*7*nombreSalles;
        int nombreJoursMinimal=(int) Math.ceil((double) nombreEtudiants / (8.0 * nombreSalles));
        if(capaciteTotal<nombreEtudiants){
            throw new nombreJoursInvalideException("nombre de jours insuffisant , " +
                    " nombre de jours minimal est "+nombreJoursMinimal);
        }
    }

    public List<Creneau> generer(LocalDate dateDebut, int nombreJours) {
        ArrayList<Creneau> list = new ArrayList<>();
        LocalTime[] heures = {
                LocalTime.of(9,  00), LocalTime.of(10,  00),
                LocalTime.of(11, 00),
                LocalTime.of(14, 00), LocalTime.of(15, 00),
                LocalTime.of(16, 00), LocalTime.of(17, 00)
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