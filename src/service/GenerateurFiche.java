package service;
import model.*;
import java.util.*;
public class GenerateurFiche {
    public List<FicheEvaluation> generer (Planning planning){
        List<FicheEvaluation> Fiches=new ArrayList<>();
        for(Soutenance s: planning.getSoutenances()){
            FicheEvaluation fiche=new FicheEvaluation(
                    s.getEtudiant(),
                    s.getEncadrant(),
                    s.getCreneau(),
                    s.getSalle(),
                    s.getJury1(),
                    s.getJury2()
            );
            Fiches.add(fiche);
        }
        return Fiches;
    }
}
