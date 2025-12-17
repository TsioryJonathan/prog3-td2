package org.td.util;

import org.td.entity.CategoryEnum;
import org.td.entity.DishTypeEnum;

public class Util {
    public DishTypeEnum getDishType(String dishType) {
        return switch (dishType) {
            case "START" -> DishTypeEnum.START;
            case "MAIN" -> DishTypeEnum.MAIN;
            case "DESSERT" -> DishTypeEnum.DESSERT;
            default -> null;
        };
    }

    public CategoryEnum getCategoryType(String categoryType) {
        return switch (categoryType) {
            case "VEGETABLE" -> CategoryEnum.VEGETABLE;
            case "ANIMAL" -> CategoryEnum.ANIMAL;
            case "DAIRY" -> CategoryEnum.DAIRY;
            case "MARI?E" -> CategoryEnum.MARINE;
            case "OTHER" -> CategoryEnum.OTHER;
            default -> null;
        };
    }
}
