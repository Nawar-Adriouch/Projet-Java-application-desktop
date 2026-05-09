package Test;
import model.Salle;

import java.util.*;
import service.*;

public class test2 {

    public static void main(String[] args){
        int roomCount = 5;

        // Dynamic column width
        int colWidth = 45;
        List<Salle> salles=new ArrayList<>();

        String separator = "+" + "-".repeat(25);
        for (int i = 0; i < roomCount; i++) {
            separator += "+" + "-".repeat(colWidth);
        }
        separator += "+";

        System.out.println(separator);
        System.out.printf("| %-23s ", "CRÉNEAU");
        for (Salle s : salles) {
            System.out.printf("| %-"+(colWidth-2)+"s ", s.toString());
        }


    }
}
