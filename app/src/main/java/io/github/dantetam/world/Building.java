package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dante on 6/16/2016.
 */
public class Building extends Entity {

    public BuildingType buildingType;
    public Building[] modules;

    public List<Item> inputResources;
    public List<Item> outputResources;

    public double workCompleted, workNeeded;

    public Building(World world, Clan clan, BuildingType type) {
        super(world, clan);
        clan.buildings.add(this);
        buildingType = type;
        name = type.name;
        //this.completionPercentage = completionPercentage;
        inputResources = new ArrayList<>();
        outputResources = new ArrayList<>();
    }

    /*public Building(Tile t, BuildingType type) {
        //super(t);
        //move(t);
        move(t);
        name = type.name;
    }*/

    public void move(Tile t) {
        if (location != null) {
            location.improvement = null;
        }
        t.improvement = this;
        super.location = t;
        //super.move(t);
    }

    public Action.ActionStatus gameProcess() {
        if (location() != null) {
            if (actionPoints <= 0) {
                return Action.ActionStatus.OUT_OF_ENERGY;
            }
            actionPoints--;
            Item[] items = {
                    new Item(ItemType.FOOD, location.food),
                    new Item(ItemType.PRODUCTION, location.production),
                    new Item(ItemType.SCIENCE, location.science),
                    new Item(ItemType.CAPITAL, location.capital)
            };
            addAllToInventory(Arrays.asList(items));
            while (true) {
                if (!hasItemsInInventory(inputResources, true)) {
                    break;
                }
                else {
                    addAllToInventory(outputResources);
                }
            }
            return Action.ActionStatus.CONTINUING;
        }
        else {
            return Action.ActionStatus.IMPOSSIBLE;
        }
    }

    public Action.ActionStatus gameBuildModule(Building building) {
        if (building.workCompleted >= building.workNeeded) {
            return Action.ActionStatus.ALREADY_COMPLETED;
        }
        if (actionPoints <= 0) {
            return Action.ActionStatus.OUT_OF_ENERGY;
        }
        actionPoints--;
        building.workCompleted += location.production;
        if (building.workCompleted >= building.workNeeded) {
            return Action.ActionStatus.EXECUTED;
        }
        return Action.ActionStatus.CONTINUING;
    }

    public void addInput(ItemType type, int quantity) {
        inputResources.add(new Item(type, quantity));
    }
    public void addOutput(ItemType type, int quantity) {
        outputResources.add(new Item(type, quantity));
    }

}
