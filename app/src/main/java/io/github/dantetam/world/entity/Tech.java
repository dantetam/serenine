package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 7/4/2016.
 */
public class Tech {

    public String name;
    public Tech parent;
    public List<Tech> extraReqs;
    public List<Tech> unlockedTechs;

    private boolean researched = false;

    public List<BuildingType> unlockedBuildings;
    public List<BuildingType> unlockedDistricts;
    public List<PersonType> unlockedUnits;
    public List<ItemType> harvestableResources;
    public List<String> unlockedSpecialAbilities;

    public int researchCompleted, researchNeeded;

    public int offsetX, offsetY;

    public Tech(String n, int researchCompleted, int researchNeeded) {
        name = n;
        this.researchCompleted = researchCompleted;
        this.researchNeeded = researchNeeded;

        //unlocked = false;
        extraReqs = new ArrayList<>();
        unlockedTechs = new ArrayList<>();
        unlockedBuildings = new ArrayList<>();
        unlockedDistricts = new ArrayList<>();
        unlockedUnits = new ArrayList<>();
        harvestableResources = new ArrayList<>();
        unlockedSpecialAbilities = new ArrayList<>();
        //allowedBuildingsAndModules = new HashMap<>();
    }

    public boolean researched() {
        return researched;
    }

    public boolean researchable() {
        for (Tech req: extraReqs) {
            if (!req.researched()) {
                return false;
            }
        }
        if (parent != null) {
            return parent.researched();
        }
        return !researched;
    }

    public void research(int researchAmount) {
        researchCompleted += researchAmount;
        if (researchCompleted >= researchNeeded) {
            researched = true;
        }
    }

    public void forceUnlock() {
        researched = true;
        researchCompleted = researchNeeded;
    }

    public boolean hasUnresearchedChildren() {
        for (Tech tech: unlockedTechs) {
            if (!tech.researched()) {
                return true;
            }
        }
        return false;
    }

}