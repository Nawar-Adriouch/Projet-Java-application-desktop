package repository;
import java.util.*;
import model.*;
public interface ExcelReader {
    List<Etudiant> lireEtudiants();
    List<Professeur> lireProfesseurs();
    List<Salle> lireSalles();
}
