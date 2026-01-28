package org.td.util;

import org.td.entity.UnitType;

public class UnitConverter {
    public static Double convertTo(String ingredientName , UnitType from, UnitType to, Double value) {
        ingredientName = ingredientName.trim();
        switch (ingredientName) {
            case "Tomate":
                if(from == to) return value;
                if(from == UnitType.KG && to == UnitType.PCS) return value * 10;
                if(from == UnitType.PCS && to == UnitType.KG) return value / 10;
                throw new RuntimeException("Cannot convert to L");
            case "Laitue":
                if(from == to) return value;
                if(from == UnitType.KG && to == UnitType.PCS) return value * 2;
                if(from == UnitType.PCS && to == UnitType.KG) return value / 2;
                throw new RuntimeException("Cannot convert to L");
            case "Chocolat":
                if(from == to) return value;
                if(from == UnitType.KG && to == UnitType.PCS) return value * 10;
                if(from == UnitType.PCS && to == UnitType.KG) return value / 10;
                if(from == UnitType.KG && to == UnitType.L) return value * 2.5;
                if (from == UnitType.L && to == UnitType.KG) return value / 2.5;
                if(from == UnitType.PCS && to == UnitType.L) return value * 0.25;
                if (from == UnitType.L && to == UnitType.PCS) return value * 4;
            case "Poulet":
                if(from == to) return value;
                if(from == UnitType.KG && to == UnitType.PCS) return value * 8;
                if(from == UnitType.PCS && to == UnitType.KG) return value / 8;
                throw new RuntimeException("Cannot convert to L");
            case "Beurre":
                if(from == to) return value;
                if(from == UnitType.KG && to == UnitType.PCS) return value * 4;
                if(from == UnitType.PCS && to == UnitType.KG) return value / 4;
                if(from == UnitType.KG && to == UnitType.L) return value * 5;
                if (from == UnitType.L && to == UnitType.KG) return value / 5;
                if (from == UnitType.PCS && to == UnitType.L) return value * 5 / 4;
                if (from == UnitType.L && to == UnitType.PCS) return value * 4 / 5;
        }
        return value;
    }
}
