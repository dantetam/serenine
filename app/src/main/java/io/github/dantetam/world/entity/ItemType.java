package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dante on 7/21/2016.
 */

public class ItemType {

    public String name;
    public String iconName;
    public String[] modelName;
    public String textureName;
    //public int food, production, science, capital, happiness, health;

    public int[] noImprYield, imprYield;
    public String imprName;

    public ItemType(String n, int[] noImpr, int[] impr) {
        name = n;
        iconName = "rock";
        noImprYield = noImpr;
        imprYield = impr;
    }

    public boolean equals(Object other) {
        if (!(other instanceof ItemType)) {
            return false;
        }
        ItemType type = (ItemType) other;
        return name.equals(type.name);
    }

    public String getAndroidResourceName() {
        return name.replace(" ", "_").toLowerCase();
    }

}

/*public enum ItemType {
    NO_RESOURCE       (-1,  "No resource"),

    FOOD              (0,   "Food"), //These are not actual items but they're used for item recipes
    PRODUCTION        (1,   "Production"),
    SCIENCE           (2,   "Science"),
    CAPITAL           (3,   "Capital"),
    LABOR             (4,   "Labor"),
    NECESSITY         (5,   "Necessity"),
    LUXURY            (6,   "Luxury"),

    GRAIN             (10,  "Grain"), //Farm+
    ASCENDIA          (15,  "Ascendia"),
    EXTROMASS         (20,  "Extromass"),

    IRON              (30,  "Iron"), //Mine+
    ASH_STONE         (54,  "Ash Stone"),
    HELLENIA          (55,  "Hellenia"),

    ABYSS_MATTER      (60,  "Abyss Matter"), //Boats+

    STEEL             (100, "Steel"), //Workshop+

    GLASS_FIRE        (120, "Glass Fire"), //Lab+
    CRYSTAL_CELLS     (121, "Crystal Cells"),
    PROGENITOR_MATTER (130, "Progenitor Matter"),
    XENOVOLTAIC_CELLS (131, "Xenovoltaic Cells"),
    ;
    public static int[] ranges = {-1,0,10,30,60,100,120,999999};
    public static String[] nameRanges = {"NoResource", "Base", "Farm", "Mine", "Boats", "Workshop", "Lab"};
    public static boolean withinCategory(String target, int id) {
        int targetIndex = -1;
        for (int i = 0; i < nameRanges.length; i++) {
            if (nameRanges[i].equalsIgnoreCase(target)) {
                targetIndex = i;
            }
        }
        if (targetIndex == -1) {
            throw new IllegalArgumentException("Could not find target string item category: " + target);
        }
        return id >= ranges[targetIndex] && id < ranges[targetIndex + 1];
    }
    public static List<Item> itemsWithinCategory(Tile tile, String target) {
        List<Item> items = new ArrayList<>();
        for (Item item: tile.resources) {
            if (withinCategory(target, item.type.id)) {
                items.add(item);
            }
        }
        return items;
    }
    public int id;
    //public int quantity; //For data manipulation purposes, not for the game
    public String renderName;
    ItemType(int n, String name) {
        id = n;
        renderName = name;
    }
    *//*ItemType(int i, int q) {
        id = i;
        quantity = q;
    }*//*
    ItemType(ItemType type) {
        id = type.id;
        renderName = type.renderName;
    }
    private static HashMap<Integer, ItemType> types;
    *//*private static String[] names = {
            "No resource",
            "Food",
            "Production",
            "Science",
            "Capital",
            "Labor",
            "Necessity",
            "Luxury",
            "Wheat",
            "Fish",
            "Branches",
            "Logs",
            "Rocks",
            "Ice",
            "Stone",
            "Clay",
            "Sand",
            "Copper Ore",
            "Iron Ore",
            "Coal",
            "Bread",
            "Lumber",
            "Brick",
            "Glass",
            "Metal",
            "Steel",
            "Tools",
            "Strong Tools",
            "Weapons",
            "Strong Weapons"
    };*//*
    public static int numItems;

    public static void init() {
        types = new HashMap<>();
        ItemType[] allEnum = ItemType.values();
        for (int i = 0; i < allEnum.length; i++) {
            ItemType item = allEnum[i];
            //item.renderName = names[i];
            types.put(item.id, item);
        }
        numItems = types.size();
    }

    public static ItemType fromString(String name) {
        if (types == null) {
            init();
        }
        for (ItemType item: values()) {
            if (item.renderName.equalsIgnoreCase(name)) {
                return item;
            }
        }
        System.out.println("Could not find resource name: " + name);
        return null;
    }
    public String toString() {
        return types.get(id).renderName;
    }

    public String getAndroidResourceName() {
        return renderName.replace(" ", "_").toLowerCase();
    }

    public static ItemType fromInt(int n) {
        if (types == null) {
            init();
        }
        if (n >= 0 && n < numItems) {
            return types.get(n);
        }
        throw new IllegalArgumentException("Invalid item type: " + n);
    }
    public static String nameFromInt(int n) {
        return fromInt(n).renderName;
        //throw new IllegalArgumentException("Invalid item type: " + n);
    }
    public static ItemType randomResource() {
        return ItemType.fromInt((int) (Math.random() * (numItems - 1)) + 1);
    }
}*/
