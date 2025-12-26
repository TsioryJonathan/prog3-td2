package org.td;

import org.td.entity.CategoryEnum;
import org.td.entity.Ingredient;
import org.td.service.DataRetriever;

import java.sql.SQLException;
import java.util.List;


public class Main {
    public static void main(String[] args) throws SQLException {
        DataRetriever dataRetriever= new DataRetriever();
        System.out.println(dataRetriever.findDishById(1));
        System.out.println(" /*  */");
        System.out.println(dataRetriever.findingIngredients(1 , 10));
        System.out.println(" /*  */");
        System.out.println(dataRetriever.createIngredients(List.of(
                new Ingredient( "chocoBANANA", 20.00D, CategoryEnum.ANIMAL)
                ,new Ingredient("chocoMelon2", 20.00D, CategoryEnum.ANIMAL)
        )));
        System.out.println(" /*  */");

        System.out.println(dataRetriever.findDishsByIngredientName("eur"));
        System.out.println(" /*  */");
        System.out.println(dataRetriever.findIngredientByCriteria("cho", null , "gâteau", 1 , 10));
    }
}