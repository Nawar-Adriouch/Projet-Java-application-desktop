package model;

public class FicheEvaluation {
    private Etudiant etudiant;
    private Encadrant encadrant;
    private Creneau creneau;
    private Salle salle;
    private Professeur jury1;
    private Professeur jury2;


    public FicheEvaluation(Etudiant etudiant, Encadrant encadrant, Creneau creneau, Salle salle,
                           Professeur jury1, Professeur jury2 ){
        this.etudiant=etudiant;
        this.encadrant=encadrant;
        this.creneau=creneau;
        this.salle=salle;
        this.jury1=jury1;
        this.jury2=jury2;

    }
    public Etudiant getEtudiant(){return etudiant;}
    public Encadrant getEncadrant(){return encadrant;}
    public Creneau getCreneau(){return creneau;}
    public Salle getSalle(){return salle;}
    public Professeur getJury1(){return jury1; }
    public Professeur getJury2(){return jury2;}

    public String toString(){
        return
                "___________________________________\n"+
                        "Fiche d'évoluation "+
                        "______________________________\n"+
                        "Etudiant           :        "+etudiant.getNom()+"  "+etudiant.getPrenom()+"\n\n"+
                        "Sujet de Soutenance:        "+etudiant.getSujet()+"\n\n"+
                        "Filière            :        "+etudiant.getFiliere()+"\n\n"+
                        "Encadrant          :        "+encadrant.getNom()+"  "+encadrant.getPrenom()+"\n\n"+
                        "Jury 1             :        "+jury1.getNom()+"  "+jury1.getPrenom()+"\n\n"+
                        "Jury 2             :        "+jury2.getNom()+"  "+jury2.getNom()+"\n\n"+
                        "Salle              :        "+salle.getNom()+ "\n\n"+
                        "Date               :        "+creneau.getDate()+"\n\n"+
                        "Heure debut        :        "+creneau.getHeureDebut()+"\n\n"+
                        "Heure Fin          :        "+creneau.getHeureFin()+"\n\n"+
                        "Note               : _____________________________________\n\n"+
                        "Remarques          : __________________________________________\n\n" +
                        "                     ____________________________________________\n\n";

    }


}