package org.td.service;

import org.td.config.DBConnection;
import org.td.entity.Dish;
import org.td.util.Util;
import org.td.util.Util.*;
import org.td.entity.Ingredient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private final Util util = new Util();
    private final Connection dbConnection = new DBConnection().getConnection();
    public DataRetriever() throws SQLException {
    }
    public Dish findDishById(int id) {
        StringBuilder ingredientSql = new StringBuilder();
        String dishSql = """
                SELECT d.id, d.name, d.dish_type from "Dish" d
                WHERE d.id = ?
                """;
        ingredientSql.append(
                """
                SELECT i.id, i.name,i.price, i.category from "Ingredient" i
                JOIN "Dish" d ON i.id_dish = d.id
                WHERE d.id = ?
                """
        );

        try(PreparedStatement dishStmt = dbConnection.prepareStatement(dishSql)){
            dishStmt.setInt(1, id);
            ResultSet dishRs = dishStmt.executeQuery();
            Dish dish = new Dish();
            List<Ingredient> ingredients = new ArrayList<>();
            if(dishRs.next()){
                dish.setId(dishRs.getInt("id"));
                dish.setName(dishRs.getString("name"));
                dish.setDishType(util.getDishType(dishRs.getString("dish_type")));
            }
            try(PreparedStatement ingredientStmt = dbConnection.prepareStatement(ingredientSql.toString())){
                ingredientStmt.setInt(1, id);
                ResultSet ingredientRs = ingredientStmt.executeQuery();
                while(ingredientRs.next()){
                    ingredients.add(new Ingredient(
                            ingredientRs.getInt(id),
                            ingredientRs.getString("name"),
                            ingredientRs.getDouble("price"),
                            util.getCategoryType(ingredientRs.getString("category"))
                    ));
                }

                dish.setIngredient(ingredients);
            } catch (SQLException e) {
                throw new SQLException(e);
            }
            return dish;
        } catch (SQLException e){
            throw new RuntimeException(e);
        }


    }

    public List<Ingredient> findingIngredients(int page, int size){
        String ingredientSql = """
                SELECT i.id, i.name,i.price, i.category from "Ingredient" i
                ORDER BY i.id
                LIMIT ? OFFSET ?
                """;
        List<Ingredient> ingredients = new ArrayList<>();
        try(PreparedStatement ingredientStmt = dbConnection.prepareStatement(ingredientSql)){
            ingredientStmt.setInt(1, size);
            ingredientStmt.setInt(2, (page - 1) * size);
            ResultSet ingredientRs = ingredientStmt.executeQuery();
            while(ingredientRs.next()){
                ingredients.add(new Ingredient(
                        ingredientRs.getInt("id"),
                        ingredientRs.getString("name"),
                        ingredientRs.getDouble("price"),
                        util.getCategoryType(ingredientRs.getString("category"))
                ));
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
