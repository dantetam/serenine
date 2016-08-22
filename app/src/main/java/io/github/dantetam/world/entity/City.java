package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.github.dantetam.utilmath.OpstrykonUtil;
import io.github.dantetam.world.action.Action;
import io.github.dantetam.world.action.BuildingAction;
import io.github.dantetam.world.factory.BuildingFactory;
import io.github.dantetam.world.factory.PersonFactory;

/**
 * Created by Dante on 7/25/2016.
 */
public class City extends Building {

    public int population, freeWorkingPopulation;
    public int foodStoredForGrowth, foodNeededForGrowth;

    //where generateCityFoodData[n] represents the needed food to go from n to n+1
    private static int[] generateCityFoodData = null;
    public static int[] generateCityFoodData() {
        if (generateCityFoodData == null) {
            generateCityFoodData = new int[30];
            generateCityFoodData[0] = 0;
            for (int i = 1; i < generateCityFoodData.length; i++) {
                generateCityFoodData[i] = 10 + i*5;
            }
        }
        return generateCityFoodData;
    }

    public HashMap<Tile, Boolean> workedTiles;
    public Collection<Tile> cityTiles;

    public City(World world, Clan clan, BuildingType type, Collection<Tile> tiles) {
        super(world, clan, type);
        workedTiles = new HashMap<>();
        cityTiles = tiles;
    }

    public void queueActionBuildModule(BuildingType buildingType) {
        clan.resources.subtractFromInventory(new Item(ItemType.fromString(buildingType.resourceNeeded)));
        Building queueBuilding = BuildingFactory.newBuilding(world, clan, buildingType, location, 0);
        actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_MODULE, queueBuilding));
    }

    public void queueActionBuildUnit(PersonType personType) {
        clan.resources.subtractFromInventory(new Item(ItemType.fromString(personType.resourceNeeded)));
        Person queuePerson = PersonFactory.newPerson(personType, world, clan, 0);
        actionsQueue.add(new BuildingAction(Action.ActionType.QUEUE_BUILD_UNIT, queuePerson));
    }

    public List<BuildingType> computePossibleBuildingsForCity() {
        TechTree tree = this.clan.techTree;
        List<BuildingType> results = new ArrayList<>();
        for (Map.Entry<BuildingType, Boolean> entry: tree.allowedBuildings.entrySet()) {
            BuildingType buildingType = entry.getKey();
            if (entry.getValue()) {
                if (buildingType.resourceNeeded != null) {
                    ItemType resourceNeeded = ItemType.fromString(buildingType.resourceNeeded);
                    if (!clan.resources.hasItemInInventory(resourceNeeded, false)) {
                        continue;
                    }
                }
                boolean foundCopy = false;
                for (Building building: modules) {
                    if (building.buildingType.equals(buildingType)) {
                        foundCopy = true;
                        break;
                    }
                }
                if (!foundCopy)
                    results.add(buildingType);
            }
        }
        return results;
    }

    public List<PersonType> computePossibleUnitsForCity() {
        TechTree tree = this.clan.techTree;
        List<PersonType> results = new ArrayList<>();
        for (Map.Entry<PersonType, Boolean> entry: tree.allowedUnits.entrySet()) {
            PersonType personType = entry.getKey();
            if (entry.getValue()) {
                if (buildingType.resourceNeeded != null) {
                    ItemType resourceNeeded = ItemType.fromString(buildingType.resourceNeeded);
                    if (!clan.resources.hasItemInInventory(resourceNeeded, false)) {
                        continue;
                    }
                }
                results.add(personType);
            }
        }
        return results;
    }

    public void executeQueue() {
        /*while (true) {
            if (actionsQueue.size() == 0) {
                return;
            }
            Action action = actionsQueue.get(0);
            Action.ActionStatus status = action.execute(this);

            if (status == Action.ActionStatus.ALREADY_COMPLETED || status == Action.ActionStatus.EXECUTED) {
                actionsQueue.remove(0);
            }
            else if (status == Action.ActionStatus.IMPOSSIBLE) {
                actionsQueue.remove(0);
                //TODO: Error code? Print info about errant action?
            }
            else if (status == Action.ActionStatus.OUT_OF_ENERGY) {
                break;
            }
            else if (status == Action.ActionStatus.CONTINUING) {
                //do nothing, keep the action in the first slot, it'll be repeated.
            }
        }*/
    }

    public Object[] gameYield() {
        if (freeWorkingPopulation > 0) {
            pickBestTiles();
        }

        double food = 0, production = 0, science = 0, capital = 0;
        Inventory inventory = new Inventory();

        HashMap<ItemType, Boolean> allowedHarvestable = clan.techTree.allowedHarvestable;

        for (Tile tile: workedTiles.keySet()) {
            food += tile.food;
            production += tile.production;
            science += tile.science;
            capital += tile.capital;
            if (tile.improvement != null) {
                int[] imprYield = tile.improvement.getYieldWithModules();
                food += imprYield[0];
                production += imprYield[1];
                science += imprYield[2];
                capital += imprYield[3];
                for (Recipe recipe: tile.improvement.recipes) {
                    for (Item item: recipe.output) {
                        if (allowedHarvestable.containsKey(item)) {
                            inventory.addToInventory(item);
                        }
                    }
                }
            }
        }

        int workingPopulation = population - freeWorkingPopulation;
        foodStoredForGrowth += food - workingPopulation;
        if (foodStoredForGrowth >= foodNeededForGrowth) {
            foodStoredForGrowth -= foodNeededForGrowth;
        }
        population++; freeWorkingPopulation++;
        foodNeededForGrowth = generateCityFoodData[population];
        //System.out.println(food + " " + production + " " + science + " " + capital);

        //lastYield = new int[]{(int)food, (int)production, (int)science, (int)capital};

        return new Object[]{new int[]{(int)food, (int)production, (int)science, (int)capital}, inventory};
    }

    public static int[] evalTile(Tile tile) {
        int[] yield = new int[4];
        yield[0] += tile.food;
        yield[1] += tile.production;
        yield[2] += tile.science;
        yield[3] += tile.capital;
        if (tile.improvement != null) {
            int[] imprYield = tile.improvement.getYieldWithModules();
            yield[0] += imprYield[0];
            yield[1] += imprYield[1];
            yield[2] += imprYield[2];
            yield[3] += imprYield[3];
        }
        return yield;
    }

    public void addTileToTerritory(Tile t) {
        if (world.getTileOwner(t) == null && !cityTiles.contains(t)) {
            cityTiles.add(t);
        }
    }

    public void pickBestTiles() {
        //workedTiles.clear();
        freeWorkingPopulation = population - workedTiles.size();
        TreeMap<Tile, Double> scoreTiles = new TreeMap<>(new Comparator<Tile>() {
            public int compare(Tile lhs, Tile rhs) {
                return lhs.hashCode() - rhs.hashCode();
            }
        });
        for (Tile tile: cityTiles) {
            double score = 0;
            score += tile.food*3 + tile.production*2 + tile.science + tile.capital;
            if (tile.resources.size() > 0) {
                if (tile.resources.get(0).type != ItemType.NO_RESOURCE) {
                    score += tile.resources.size()*3;
                }
            }
            scoreTiles.put(tile, score);
        }
        Map<Tile, Double> sorted = OpstrykonUtil.sortMapByValue(scoreTiles);
        Set<Tile> tilesToPick = sorted.keySet();
        for (Tile tile: tilesToPick) {
            if (freeWorkingPopulation > 0) {
                pickTile(tile);
            }
            else {
                break;
            }
        }

        /*for (Map.Entry<Tile, Double> en: sorted.entrySet()) {
            System.out.println(en.getKey().toString() + " " + en.getValue());
        }*/
    }

    public boolean pickTile(Tile t) {
        if (workedTiles.get(t) == null && cityTiles.contains(t) && freeWorkingPopulation > 0) {
            workedTiles.put(t, true);
            freeWorkingPopulation--;
            return true;
        }
        return false;
    }

    public boolean freeTile(Tile t) {
        if (workedTiles.get(t) != null && cityTiles.contains(t)) {
            workedTiles.remove(t);
            freeWorkingPopulation++;
            return true;
        }
        return false;
    }

}