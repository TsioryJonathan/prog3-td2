package org.td.service;

import org.td.config.DBConnection;
import org.td.entity.CategoryEnum;
import org.td.entity.Dish;
import org.td.entity.DishTypeEnum;
import org.td.util.Util;
import org.td.entity.Ingredient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private final Util util = new Util();
    private final DBConnection dbConnection = new DBConnection();
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

        try{
            Connection dbCon = dbConnection.getConnection();
            PreparedStatement dishStmt = dbCon.prepareStatement(dishSql);
            dishStmt.setInt(1, id);
            ResultSet dishRs = dishStmt.executeQuery();
            Dish dish = new Dish();
            List<Ingredient> ingredients = new ArrayList<>();
            if(dishRs.next()){
                dish.setId(dishRs.getInt("id"));
                dish.setName(dishRs.getString("name"));
                dish.setDishType(util.getDishType(dishRs.getString("dish_type")));
            }
            try(PreparedStatement ingredientStmt = dbCon.prepareStatement(ingredientSql.toString())){
                ingredientStmt.setInt(1, id);
                ResultSet ingredientRs = ingredientStmt.executeQuery();
                while(ingredientRs.next()){
                    ingredients.add(new Ingredient(
                            ingredientRs.getInt("id"),
                            ingredientRs.getString("name"),
                            ingredientRs.getDouble("price"),
                            util.getCategoryType(ingredientRs.getString("category"))
                    ));
                }

                dish.setIngredient(ingredients);
            } catch (SQLException e) {
                throw new SQLException(e);
            }
            dbConnection.closeConnection(dbCon);
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
        try{
            Connection dbCon = dbConnection.getConnection();
            PreparedStatement ingredientStmt = dbCon.prepareStatement(ingredientSql);
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

            dbConnection.closeConnection(dbCon);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Return a list of every ingredients id */
    public List<String> getIngredientsName(){
        List<String> ids = new ArrayList<>();
        StringBuilder ingredientSql = new StringBuilder();
        ingredientSql.append(
                """
                SELECT name from "Ingredient"
                """);
        try{
            Connection dbCon = dbConnection.getConnection();
            PreparedStatement ingredientStmt = dbCon.prepareStatement(ingredientSql.toString());
            ResultSet ingredientRs = ingredientStmt.executeQuery();
            while(ingredientRs.next()){
                ids.add(ingredientRs.getString("name"));
            }

            dbConnection.closeConnection(dbCon);
            return ids;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) throws SQLException {
        StringBuilder ingredientSql = new StringBuilder();
        ingredientSql.append(
                """
                INSERT INTO "Ingredient"(name, price, category, id_dish) VALUES (?, ?, ?::ingredient_category_enum,?)
                """
        );
        if (newIngredients.size() > 1){
            ingredientSql.append(
                    """
                    ,(?,?,?::ingredient_category_enum,?)
                    """.repeat(newIngredients.size() - 1)
            );
        }
        Connection con = null;
        try{
            /* check if an ingredient already exists */
            List<String> ingNames = this.getIngredientsName();
            for(Ingredient ing : newIngredients){
                if (ingNames.contains(ing.getName())){
                    throw new RuntimeException("Ingredient already exists");
                }
            }

            con = dbConnection.getConnection();
            con.setAutoCommit(false);
            int i = 1;
            PreparedStatement ingredientStmt = con.prepareStatement(ingredientSql.toString());
            for(Ingredient ing : newIngredients){
                ingredientStmt.setString(i , ing.getName());
                i++;
                ingredientStmt.setDouble(i , ing.getPrice());
                i++;
                ingredientStmt.setString(i , ing.getCategory().toString());
                i++;
                ingredientStmt.setObject(i , ing.getDish() != null ? ing.getDish().getId() : null);
                i++;
            }
            ingredientStmt.executeUpdate();
            con.commit();
            dbConnection.closeConnection(con);
            return newIngredients;
        } catch (Exception e) {
            if (con != null) {
                con.rollback();
            }
            throw new RuntimeException(e);
        }
    }

    public Dish saveDish(Dish dishToSave) throws SQLException {
        Connection con = null;
        /* With ID */
        if( dishToSave.getId() != 0){
            //Check if dish dont exist
        StringBuilder sql = new StringBuilder();
        sql.append("""
                   SELECT id FROM "Dish" WHERE id = ?
                   """);
        try{
            con = dbConnection.getConnection();
            PreparedStatement dishStmt = con.prepareStatement(sql.toString());
            dishStmt.setInt(1, dishToSave.getId());
            ResultSet dishRs = dishStmt.executeQuery();
            /* If dish does not exist */
            if(!dishRs.next()){
                String dishSql = """
                        INSERT INTO "Dish" (id, name, dish_type) VALUES (?,?,?::dish_type_enum)
                        """;
                PreparedStatement dishStmt1 = con.prepareStatement(dishSql);
                dishStmt1.setInt(1, dishToSave.getId());
                dishStmt1.setString(2, dishToSave.getName());
                dishStmt1.setString(3, String.valueOf(DishTypeEnum.valueOf(dishToSave.getDishType().toString())));
                dishStmt1.executeUpdate();
                List<Ingredient> ingredients = this.createIngredients(dishToSave.getIngredient());
                con.commit();
                dbConnection.closeConnection(con);
                return dishToSave;
            }
            /* If dish already exists */
            else{
                /* Update the dish */
                String updateSQL = """
                        UPDATE "Dish"
                        SET id = ?,
                        name = ?,
                        dish_type = ?
                        WHERE id = ?
                        """;
                PreparedStatement dishStmt1 = con.prepareStatement(updateSQL);
                dishStmt1.setInt(1, dishToSave.getId());
                dishStmt1.setString(2, dishToSave.getName());
                dishStmt1.setString(3, dishToSave.getDishType().toString());
                dishStmt1.executeUpdate();
                /* Update ingredients */

            }

            return new Dish();

        } catch (Exception e) {
            if (con != null) {
                con.rollback();
            }
            throw new RuntimeException(e);
        }}
        else{
            return new Dish();
        }
    }

    /* Return every dish's ingredients */
    public List<Ingredient> getIngredientsOfADish(int dishId){
        Connection con = null;
        String sql = """
                SELECT i.id, i.name, i.price, i.category FROM "Ingredient" i
                JOIN "Dish" d ON i.id_dish = d.id
                WHERE i.id_dish = ?
                """;
        try{
            con = dbConnection.getConnection();
            PreparedStatement ingredientStmt = con.prepareStatement(sql);
            ingredientStmt.setInt(1, dishId);
            ResultSet ingredientRs = ingredientStmt.executeQuery();
            return util.mapResultSetToIngredientList(ingredientRs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public List<Dish> findDishesByIngredientName(String ingredientName){
        Connection con = null;
        try{
            con = dbConnection.getConnection();
            List<Dish> dishs = new ArrayList<>();
            String dishSql = """
                     SELECT d.id, d.name, dish_type FROM "Dish" d JOIN "Ingredient" i ON d.id = i.id_dish WHERE i.name LIKE ?\s
                    \s""";
            PreparedStatement dishStmt = con.prepareStatement(dishSql);
            dishStmt.setString(1, "%" + ingredientName + "%");
            ResultSet dishRs = dishStmt.executeQuery();
            while(dishRs.next()){
                Dish dish = new Dish();
                dish.setId(dishRs.getInt("id"));
                dish.setName(dishRs.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(dishRs.getString("dish_type")));
                List<Ingredient> ingredientList = this.getIngredientsOfADish(dish.getId());
                dish.setIngredient(ingredientList);
                dishs.add(dish);
            }
            dbConnection.closeConnection(con);
            return dishs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredientByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size){
        Connection con = null;
        StringBuilder ingredientSql = new StringBuilder();
        ingredientSql.append("""
                SELECT i.id, i.name,i.price, i.category, i.id_dish, d.id dish_id, d.name dish_name, d.dish_type FROM "Ingredient" i
                 JOIN "Dish" d ON i.id_dish = d.id
                 WHERE 0=0
                """);
        List<String> toAdd = new ArrayList<>();
        if(ingredientName != null){
            toAdd.add("%" + ingredientName + "%");
            ingredientSql.append("AND i.name ILIKE ?");
        }
        if(category != null){
            toAdd.add("%" + category.toString() + "%");
            ingredientSql.append(" AND i.category ILIKE ?::ingredient_category_enum");
        }
        if(dishName != null){
            toAdd.add("%" + dishName + "%");
            ingredientSql.append(" AND d.name ILIKE ?");
        }
        ingredientSql.append(" OFFSET ? LIMIT ?");
        try{
            con = dbConnection.getConnection();
            PreparedStatement ingredientStmt = con.prepareStatement(ingredientSql.toString());
            List<Ingredient> ingredients = new ArrayList<>();
            for (int i = 1; i <= toAdd.size(); i++){
                ingredientStmt.setObject(i , toAdd.get(i-1));
            }
            ingredientStmt.setInt(toAdd.size() + 1, size * (page - 1));
            ingredientStmt.setInt(toAdd.size() + 2, size);

            ResultSet rs = ingredientStmt.executeQuery();
            while(rs.next()){
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category"))
                        ));
            }
            dbConnection.closeConnection(con);
            return ingredients;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
