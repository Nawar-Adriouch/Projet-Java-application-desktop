package org.example;

import service.*;
import model.*;
import ui.MainWindow;
import config.*;
import javax.swing.SwingUtilities;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        OutputConfig.afficherConfig();
        SwingUtilities.invokeLater(MainWindow::new);
        Planning planning = new Planning();
        List<Soutenance> soutenances = planning.getSoutenances();

        boolean resultat = ValidationPlanning.verifierTout(soutenances);

        if (resultat) {
            System.out.println("Planning VALIDE ");
        } else {
            System.out.println("Planning NON VALIDE ");
        }
    }
}


