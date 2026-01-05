package org.td.service;

import org.td.config.DBConnection;
import org.td.entity.CategoryEnum;
import org.td.entity.Dish;
import org.td.entity.DishTypeEnum;
import org.td.entity.Ingredient;
import org.td.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private final Util util = new Util();
    private final DBConnection dbConnection = new DBConnection();

    public DataRetriever() throws SQLException {}


    public Dish findDishById(int id) {
        Connection con = null;
        PreparedStatement dishStmt = null;
        PreparedStatement ingredientStmt = null;
        ResultSet dishRs = null;
        ResultSet ingredientRs = null;

        String dishSql = """
                SELECT id, name, dish_type
                FROM "Dish"
                WHERE id = ?
                """;

        String ingredientSql = """
                SELECT id, name, price, category
                FROM "Ingredient"
                WHERE id_dish = ?
                """;

        try {
            con = dbConnection.getConnection();

            dishStmt = con.prepareStatement(dishSql);
            dishStmt.setInt(1, id);
            dishRs = dishStmt.executeQuery();

            if (!dishRs.next()) {
                throw new RuntimeException("Dish doesn't exist");
            }

            Dish dish = new Dish();
            dish.setId(dishRs.getInt("id"));
            dish.setName(dishRs.getString("name"));
            dish.setDishType(util.getDishType(dishRs.getString("dish_type")));

            ingredientStmt = con.prepareStatement(ingredientSql);
            ingredientStmt.setInt(1, id);
            ingredientRs = ingredientStmt.executeQuery();

            List<Ingredient> ingredients = new ArrayList<>();
            while (ingredientRs.next()) {
                ingredients.add(new Ingredient(
                        ingredientRs.getInt("id"),
                        ingredientRs.getString("name"),
                        ingredientRs.getDouble("price"),
                        util.getCategoryType(ingredientRs.getString("category"))
                ));
            }

            dish.setIngredient(ingredients);
            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (ingredientRs != null) ingredientRs.close();
                if (dishRs != null) dishRs.close();
                if (ingredientStmt != null) ingredientStmt.close();
                if (dishStmt != null) dishStmt.close();
                if (con != null) con.close();
            } catch (SQLException ignored) {}
        }
    }


    public List<Ingredient> findIngredients(int page, int size) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = """
                SELECT id, name, price, category
                FROM "Ingredient"
                ORDER BY id
                LIMIT ? OFFSET ?
                """;

        List<Ingredient> ingredients = new ArrayList<>();

        try {
            con = dbConnection.getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, size);
            stmt.setInt(2, (page - 1) * size);

            rs = stmt.executeQuery();
            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        util.getCategoryType(rs.getString("category"))
                ));
            }

            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException ignored) {}
        }
    }

    public List<String> getIngredientsName() {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = """
                SELECT name
                FROM "Ingredient"
                """;

        List<String> names = new ArrayList<>();

        try {
            con = dbConnection.getConnection();
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                names.add(rs.getString("name"));
            }

            return names;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException ignored) {}
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        Connection con = null;
        PreparedStatement insertStmt = null;
        PreparedStatement checkStmt = null;

        String insertSql = """
            INSERT INTO "Ingredient"(name, price, category, id_dish)
            VALUES (?, ?, ?::ingredient_category_enum, ?)
            """;

        String checkSql = """
            SELECT 1 FROM "Ingredient" WHERE name = ?
            """;

        for (int i = 0; i < newIngredients.size(); i++) {
            for (int j = i + 1; j < newIngredients.size(); j++) {
                if (newIngredients.get(i).getName()
                        .equalsIgnoreCase(newIngredients.get(j).getName())) {
                    throw new RuntimeException(
                            "Duplicate ingredient in input list: " +
                                    newIngredients.get(i).getName()
                    );
                }
            }
        }

        try {
            con = dbConnection.getConnection();
            con.setAutoCommit(false);

            checkStmt = con.prepareStatement(checkSql);
            for (Ingredient ing : newIngredients) {
                checkStmt.setString(1, ing.getName());
                if (checkStmt.executeQuery().next()) {
                    throw new RuntimeException(
                            "Ingredient already exists in database: " +
                                    ing.getName()
                    );
                }
            }

            insertStmt = con.prepareStatement(insertSql);
            for (Ingredient ing : newIngredients) {
                insertStmt.setString(1, ing.getName());
                insertStmt.setDouble(2, ing.getPrice());
                insertStmt.setString(3, ing.getCategory().toString());
                insertStmt.setObject(4,
                        ing.getDish() != null ? ing.getDish().getId() : null
                );
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
            con.commit();

            return newIngredients;

        } catch (Exception e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        } finally {
            try {
                if (checkStmt != null) checkStmt.close();
                if (insertStmt != null) insertStmt.close();
                if (con != null) con.close();
            } catch (SQLException ignored) {}
        }
    }

    public Dish saveDish(Dish dishToSave) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String findDishSql = """
            SELECT id FROM "Dish" WHERE id = ?
            """;

        String insertDishSql = """
            INSERT INTO "Dish"(name, dish_type)
            VALUES (?, ?::dish_type_enum)
            RETURNING id
            """;

        String updateDishSql = """
            UPDATE "Dish"
            SET name = ?, dish_type = ?::dish_type_enum
            WHERE id = ?
            """;

        String clearIngredientsSql = """
            UPDATE "Ingredient"
            SET id_dish = NULL
            WHERE id_dish = ?
            """;

        String attachIngredientSql = """
            UPDATE "Ingredient"
            SET id_dish = ?
            WHERE id = ?
            """;

        try {
            con = dbConnection.getConnection();
            con.setAutoCommit(false);

            boolean exists = false;
            stmt = con.prepareStatement(findDishSql);
            stmt.setInt(1, dishToSave.getId());
            rs = stmt.executeQuery();
            exists = rs.next();
            rs.close();
            stmt.close();

            if (!exists) {
                stmt = con.prepareStatement(insertDishSql);
                stmt.setString(1, dishToSave.getName());
                stmt.setString(2, dishToSave.getDishType().toString());

                rs = stmt.executeQuery();
                if (rs.next()) {
                    dishToSave.setId(rs.getInt(1));
                }
                rs.close();
                stmt.close();

            } else {
                stmt = con.prepareStatement(updateDishSql);
                stmt.setString(1, dishToSave.getName());
                stmt.setString(2, dishToSave.getDishType().toString());
                stmt.setInt(3, dishToSave.getId());
                stmt.executeUpdate();
                stmt.close();
            }

            stmt = con.prepareStatement(clearIngredientsSql);
            stmt.setInt(1, dishToSave.getId());
            stmt.executeUpdate();
            stmt.close();

            if (dishToSave.getIngredient() != null) {
                stmt = con.prepareStatement(attachIngredientSql);
                for (Ingredient ing : dishToSave.getIngredient()) {
                    stmt.setInt(1, dishToSave.getId());
                    stmt.setInt(2, ing.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                stmt.close();
            }

            con.commit();
            return dishToSave;

        } catch (Exception e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);

        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException ignored) {}
        }
    }



    public List<Ingredient> getIngredientsOfADish(int dishId) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = """
                SELECT id, name, price, category
                FROM "Ingredient"
                WHERE id_dish = ?
                """;

        try {
            con = dbConnection.getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, dishId);
            rs = stmt.executeQuery();

            return util.mapResultSetToIngredientList(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException ignored) {}
        }
    }

    public List<Dish> findDishesByIngredientName(String ingredientName) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = """
                SELECT DISTINCT d.id, d.name, d.dish_type
                FROM "Dish" d
                JOIN "Ingredient" i ON d.id = i.id_dish
                WHERE i.name ILIKE ?
                """;

        List<Dish> dishes = new ArrayList<>();

        try {
            con = dbConnection.getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, "%" + ingredientName + "%");
            rs = stmt.executeQuery();

            while (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                dish.setIngredient(getIngredientsOfADish(dish.getId()));
                dishes.add(dish);
            }

            return dishes;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException ignored) {}
        }
    }


    public List<Ingredient> findIngredientByCriteria(
            String ingredientName,
            CategoryEnum category,
            String dishName,
            int page,
            int size
    ) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        StringBuilder sql = new StringBuilder("""
                SELECT i.id, i.name, i.price, i.category
                FROM "Ingredient" i
                LEFT JOIN "Dish" d ON i.id_dish = d.id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (ingredientName != null) {
            sql.append(" AND i.name ILIKE ?");
            params.add("%" + ingredientName + "%");
        }
        if (category != null) {
            sql.append(" AND i.category = ?::ingredient_category_enum");
            params.add(category.toString());
        }
        if (dishName != null) {
            sql.append(" AND d.name ILIKE ?");
            params.add("%" + dishName + "%");
        }

        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(size * (page - 1));

        List<Ingredient> ingredients = new ArrayList<>();

        try {
            con = dbConnection.getConnection();
            stmt = con.prepareStatement(sql.toString());

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category"))
                ));
            }

            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException ignored) {}
        }
    }
}
