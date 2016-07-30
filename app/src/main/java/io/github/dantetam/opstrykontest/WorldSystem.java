package io.github.dantetam.opstrykontest;

import io.github.dantetam.world.ArtificialIntelligence;
import io.github.dantetam.world.Building;
import io.github.dantetam.world.City;
import io.github.dantetam.world.Clan;
import io.github.dantetam.world.Pathfinder;
import io.github.dantetam.world.Person;
import io.github.dantetam.world.World;

/**
 * Created by Dante on 7/13/2016.
 */
public class WorldSystem {

    public World world;
    public ArtificialIntelligence artificialIntelligence;
    public static WorldPathfinder worldPathfinder;

    public int turnNumber = 0;

    public Clan playerClan;

    public WorldSystem(WorldHandler worldHandler) {
        world = worldHandler.world;
        initClan(world.getClans().get(0));
        artificialIntelligence = new ArtificialIntelligence(world);
        worldPathfinder = new WorldPathfinder(world);
    }

    public void initClan(Clan c) {
        playerClan = c;
    }

    public void turn() {
        processClan(playerClan);
        for (Clan clan: world.getClans()) {
            if (!clan.equals(playerClan)) {
                artificialIntelligence.computerClanActions(clan);
            }
            processClan(clan);
        }

        for (Clan c: world.getClans()) {
            for (Person person: c.people) {
                person.actionPoints = person.maxActionPoints;
            }
            for (Building building: c.buildings) {
                building.actionPoints = building.maxActionPoints;
                building.executeQueue();
            }
        }
        turnNumber++;
        System.err.println("#turns passed: " + turnNumber);
    }

    private void processClan(Clan clan) {
        for (Building building: clan.buildings) {
            building.executeQueue();
        }
        for (Person person: clan.people) {
            person.executeQueue();
        }
        int totalScience = 0, totalGold = 0;
        for (City city: clan.cities) {
            //Determine yield here? Don't separate process.
            totalScience += city.lastYield[2];
            totalGold += city.lastYield[3];
        }
    }

}
