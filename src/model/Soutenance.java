package model;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Spliterator;

public class Soutenance {
    private Etudiant etudiant;
    private Encadrant encadrant;
    private Professeur jury1;
    private Professeur jury2;
    private Salle salle ;
    private Creneau creneau;

    public Soutenance(Etudiant etudiant,Encadrant encadrant,Professeur jury1,Professeur jury2,Salle salle,Creneau creneau){
        this.etudiant=etudiant;
        this.encadrant=encadrant;
        this.jury1=jury1;
        this.jury2=jury2;
        this.salle=salle;
        this.creneau=creneau;

    }
    public Etudiant getEtudiant(){return etudiant;}
    public Encadrant getEncadrant() {return encadrant;}
    public Professeur getJury1() {return jury1;}
    public Professeur getJury2() {return jury2;}
    public Salle getSalle(){return salle ;}
    public Creneau getCreneau(){return creneau;}
    

    public String toString(){
        return etudiant+" "+encadrant+" "+jury1+" "+jury2+" "+salle+" "+creneau;
    }


    }
