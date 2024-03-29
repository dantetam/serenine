package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.List;

import io.github.dantetam.opstrykontest.WorldSystem;
import io.github.dantetam.world.action.Action;
import io.github.dantetam.world.action.Combat;
import io.github.dantetam.world.action.PersonAction;
import io.github.dantetam.world.factory.PersonFactory;

import static io.github.dantetam.world.action.Action.ActionType;
import static io.github.dantetam.world.action.Action.ActionStatus;

/**
 * Created by Dante on 6/13/2016.
 */
public class Person extends Entity {

    public PersonType personType;

    public int age;
    public List<Tech> skills;

    public Person(World world, Clan clan, String name) {
        super(world, clan);
        if (clan != null)
            clan.people.add(this);
        this.name = name;
        skills = new ArrayList<>();
    }

    public void executeQueue() {
        if (!enabled) {
            return;
        }
        while (true) {
            if (actionsQueue.size() == 0) return;
            Action action = actionsQueue.get(0);
            ActionStatus status = action.execute(this);
            /*if (action.execute() == ActionStatus.ALREADY_COMPLETED || action.execute() == ActionStatus.EXECUTED) {
                actionsQueue.remove(0);
            } else {
                break;
            }*/
            if (status == ActionStatus.CONSUME_UNIT) {
                PersonFactory.removePerson(this);
                return;
            }
            else if (status == ActionStatus.ALREADY_COMPLETED || status == ActionStatus.EXECUTED) {
                actionsQueue.remove(0);
            }
            else if (status == ActionStatus.IMPOSSIBLE) {
                actionsQueue.remove(0);
                //TODO: Error code? Print info about errant action?
            }
            else if (status == ActionStatus.OUT_OF_ENERGY) {
                break;
            }
            else if (status == ActionStatus.CONTINUING) {
                //do nothing, keep the action in the first slot, it'll be repeated.
            }
        }
    }

    /*
    Move a person within the game by one tile and if the unit has action points.
    Return true if the game move was successful.
     */
    /*public ActionStatus gameMove(Tile t) {
        ActionStatus moved = super.gameMove(t);
        if (moved == ActionStatus.EXECUTED) {
            actionPoints--;
        }
        return moved;
    }*/

    public ActionStatus gameMove(Tile t) {
        Tile location = location();
        if (location != null) {
            float dist = location.dist(t);
            if (actionPoints <= 0) {
                return ActionStatus.OUT_OF_ENERGY;
            }
            if (dist == 1) {
                for (Entity en: t.occupants) {
                    if (en instanceof Person) {
                        Person unit = (Person) en;
                        if (personType.category.equals("peaceful")) {
                            return ActionStatus.IMPOSSIBLE;
                        }
                        if (world.worldSystem.atWar(clan, unit.clan)) {
                            return gameAttack(unit);
                        } else {
                            return ActionStatus.IMPOSSIBLE;
                        }
                    }
                    else if (en instanceof City) {
                        City city = (City) en;
                        if (personType.category.equals("peaceful")) {
                            return ActionStatus.IMPOSSIBLE;
                        }
                        return gameAttack(city);
                    }
                }
                actionPoints--;
                move(t);
                return ActionStatus.EXECUTED;
            }
            else if (dist == 0) {
                return ActionStatus.ALREADY_COMPLETED;
            }
        }
        System.err.println("Invalid game move: ");
        System.err.println(location + "; " + location.dist(t) + "; " + actionPoints);
        return ActionStatus.IMPOSSIBLE;
    }

    public ActionStatus gameAttack(City defender) {
        if (personType.range > 0) {
            Combat.attackRanged(this, defender);
        }
        else {
            Combat.attackMelee(this, defender);
        }
        if (defender.health <= 0) {
            defender.health = defender.maxHealth / 3;
            defender.clan.cities.remove(defender);
            defender.clan = clan;
            clan.cities.add(defender);

            System.out.println("City changed hands");
        }
        if (this.health <= 0) {
            //actionsQueue.clear();
            //actionsQueue.add(new PersonAction(ActionType.COMBAT_DEATH, defender));
            return ActionStatus.CONSUME_UNIT;
        }
        actionPoints = 0;
        return ActionStatus.EXECUTED;
    }

    public ActionStatus gameAttack(Person defender) {
        if (personType.range > 0) {
            Combat.attackRanged(this, defender);
        }
        else {
            Combat.attackMelee(this, defender);
        }
        if (defender.health <= 0) {
            defender.actionsQueue.clear();
            defender.actionsQueue.add(new PersonAction(ActionType.COMBAT_DEATH, defender));
            defender.executeQueue();
        }
        if (this.health <= 0) {
            //actionsQueue.clear();
            //actionsQueue.add(new PersonAction(ActionType.COMBAT_DEATH, defender));
            return ActionStatus.CONSUME_UNIT;
        }
        actionPoints = 0;
        return ActionStatus.EXECUTED;
    }

    public ActionStatus gameFortify() {
        if (fortify) {
            return ActionStatus.ALREADY_COMPLETED;
        }
        if (actionPoints > 0) {
            actionPoints--;
            fortify = true;
            return ActionStatus.EXECUTED;
        }
        return ActionStatus.OUT_OF_ENERGY;
    }

    public ActionStatus gameBuild(Building b) {
        if (b.location() == null) {
            b.move(location);
        }
        if (b.completionPercentage() < 1) {
            if (actionPoints <= 0) {
                return ActionStatus.OUT_OF_ENERGY;
            }
            b.workNeeded += calculateWorkAdded();
            actionPoints--;
            if (b.completionPercentage() < 1) {
                return ActionStatus.CONTINUING;
            }
            else
                return ActionStatus.EXECUTED;
        }
        else {
            return ActionStatus.ALREADY_COMPLETED;
        }
    }

    private double calculateWorkAdded() {
        return 3;
    }

    /*private ActionStatus gameHealHealth() {
        if (health < maxHealth) {
            if (actionPoints <= 0) {
                return ActionStatus.OUT_OF_ENERGY;
            }
            health += (int) (0.05d * maxHealth);
            if (health > maxHealth) {
                health = maxHealth;
            }
            actionPoints--;
            return ActionStatus.EXECUTED;
        }
        return ActionStatus.ALREADY_COMPLETED;
    }*/

    public ActionStatus gameMovePath(Tile destination) {
        List<Tile> path = WorldSystem.worldPathfinder.findPath(location, destination);
        if (path == null) {
            return ActionStatus.IMPOSSIBLE;
        }
        path.remove(0);
        while (true) {
            for (Entity en: destination.occupants) {
                if (en instanceof Person) {
                    Person unit = (Person) en;
                    if (world.worldSystem.atWar(clan, unit.clan)) {
                        return gameAttack(unit);
                    } else {
                        return ActionStatus.IMPOSSIBLE;
                    }
                }
                else if (en instanceof City) {
                    City city = (City) en;
                }
            }
            if (path.size() == 0) {
                return ActionStatus.ALREADY_COMPLETED;
            }
            ActionStatus status = gameMove(path.get(0));
            if (status == ActionStatus.OUT_OF_ENERGY) { //Out of AP
                for (Tile t: path) {
                    actionsQueue.add(new PersonAction(ActionType.MOVE, t));
                }
                return ActionStatus.OUT_OF_ENERGY;
            } else {
                path.remove(0);
            }
        }
    }

    public ActionStatus gameChase(Entity entity) {
        List<Tile> path = WorldSystem.worldPathfinder.findPath(location, entity.location());
        if (path == null) {
            return ActionStatus.IMPOSSIBLE;
        }
        path.remove(0);
        while (true) {
            if (path.size() == 1) {
                //TODO: Attack the enemy here
                actionPoints = 0;
                if (entity.health <= 0) {
                    return ActionStatus.ALREADY_COMPLETED;
                }
                else {
                    return ActionStatus.CONTINUING;
                }
            }
            ActionStatus status = gameMove(path.get(0));
            if (status == ActionStatus.OUT_OF_ENERGY) { //Out of AP
                /*for (Tile t: path) {
                    actionsQueue.add(new PersonAction(ActionType.MOVE, t));
                }*/
                return ActionStatus.OUT_OF_ENERGY;
            } else {
                path.remove(0);
            }
        }
    }

    public ActionStatus consumeUnit() {
        return ActionStatus.CONSUME_UNIT;
    }

    /*public enum PersonType {
        WARRIOR ("Warrior"),
        SETTLER ("Settler");
        String renderName;
        PersonType(String name) {
            renderName = name;
        }
        public String toString() {
            return renderName;
        }
        private static PersonType[] types = null;
        public static PersonType fromString(String name) {
            if (types == null) {
                types = PersonType.values();
            }
            for (PersonType personType: types) {
                if (personType.renderName.equals(name)) {
                    return personType;
                }
            }
            System.err.println("Invalid person type: " + name);
            return null;
        }
    }*/

}
