package io.github.dantetam.world;

/**
 * Created by Dante on 7/17/2016.
 */
public class BuildingAction extends Action {

    public BuildingAction(ActionType t, Object obj) {
        super(t, obj);
    }

    public ActionStatus execute(Object object) {
        if (!(object instanceof Building)) return ActionStatus.IMPOSSIBLE;
        Building building = (Building) object;
        switch (type) {
            case PROCESS:
                return building.gameProcess();
            case QUEUE_BUILD_MODULE:
                return building.gameBuildModule((Building) data);
            case QUEUE_BUILD_PERSON:
                return building.gameBuildUnit((Person) data);
            default:
                System.out.println("Invalid action type: " + type);
                return ActionStatus.IMPOSSIBLE;
        }
    }

    public String toString() {
        switch (type) {
            case PROCESS:
                return "";
            case QUEUE_BUILD_MODULE:
                return "Add: " + ((Building) data).name;
            case QUEUE_BUILD_PERSON:
                return "Build: " +  ((Person) data).name;
            default:
                System.out.println("Invalid action type: " + type);
                return null;
        }
    }

}
