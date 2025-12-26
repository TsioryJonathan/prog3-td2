package org.td;

import org.td.entity.CategoryEnum;
import org.td.entity.Ingredient;
import org.td.service.DataRetriever;

import java.sql.SQLException;
import java.util.List;


public class Main {
    public static void main(String[] args) throws SQLException {
        DataRetriever dataRetriever= new DataRetriever();
        //System.out.println(dataRetriever.findDishById(1));
        //System.out.println(dataRetriever.findingIngredients(1 , 10));
        //System.out.println(dataRetriever.createIngredients(List.of(
         //       new Ingredient(1, "choco", 20.00D, CategoryEnum.ANIMAL)
          //      ,new Ingredient(1, "choco", 20.00D, CategoryEnum.ANIMAL)
        //)));
        System.out.println(dataRetriever.createIngredients(List.of(
                new Ingredient(2, "chocoBANANA", 20.00D, CategoryEnum.ANIMAL)
                ,new Ingredient(2, "chocoMelon", 20.00D, CategoryEnum.ANIMAL)
        )));
    }
}