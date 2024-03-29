package io.github.dantetam.opstrykontest;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import io.github.dantetam.opengl.MousePicker;
import io.github.dantetam.utilmath.OpstrykonUtil;
import io.github.dantetam.world.action.Action;
import io.github.dantetam.world.entity.Building;
import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.world.action.CombatAction;
import io.github.dantetam.world.entity.Entity;
import io.github.dantetam.world.entity.Person;
import io.github.dantetam.world.entity.Tile;

/*
Used by OpenGL ES as a wrapper view for rendering graphics on the screen
The extra functionality to handle mouse clicks and update the menus accordingly,
and deal with mouse picker events.
 */
public class OpenGLSurfaceView extends GLSurfaceView
{
    private LessonSevenActivity mActivity;
	private OpenGLRenderer mRenderer;
    private GuiHandler guiHandler;

	// Offsets for touch events	 
    private float mPreviousX = -1;
    private float mPreviousY = -1;
    
    private float mDensity;

    private MousePicker mousePicker;

    private Clan playerClan;

    //private Tile selectedTile = null;
        	
	public OpenGLSurfaceView(Context context)
	{
		super(context);
	}
	
	public OpenGLSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

    public void init(LessonSevenActivity activity, MousePicker mousePicker, GuiHandler handler, Clan playerClan) {
        mActivity = activity;
        this.mousePicker = mousePicker;
        this.playerClan = playerClan;
        guiHandler = handler;
        mRenderer = mActivity.mRenderer;
        //playerClan = mRenderer.worldSystem.playerClan;
    }

    public static String actionToString(int action) {
        switch (action) {

            case MotionEvent.ACTION_DOWN: return "Down";
            case MotionEvent.ACTION_MOVE: return "Move";
            case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
            case MotionEvent.ACTION_UP: return "Up";
            case MotionEvent.ACTION_POINTER_UP: return "Pointer Up";
            case MotionEvent.ACTION_OUTSIDE: return "Outside";
            case MotionEvent.ACTION_CANCEL: return "Cancel";
        }
        return "";
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		if (event != null)
		{
            //System.out.println(actionToString(MotionEventCompat.getActionMasked(event)));
			float x = event.getX();
			float y = event.getY();
			
			if (event.getAction() == MotionEvent.ACTION_MOVE)
			{
				processMoveAction(x, y);
			}
            else
            {
                //System.out.println(">>>>" + MotionEvent.actionToString(event.getAction()));
            }
			
			mPreviousX = x;
			mPreviousY = y;
			
			return true;
		}
		else
		{
			return super.onTouchEvent(event);
		}		
	}

    public void processMoveAction(float x, float y) {
        if (!mRenderer.inGame) {
            return;
        }
        if (mRenderer != null)
        {
            float deltaX = (x - mPreviousX) / mDensity / 2f;
            float deltaY = (y - mPreviousY) / mDensity / 2f;
                    /*if (mPreviousX == -1 || mPreviousY == -1) {
                        deltaX = 0; deltaY = 0;
                    }*/

            //mRenderer.mDeltaX += deltaX;
            //mRenderer.mDeltaY += deltaY;

            if (mActivity.findViewById(R.id.tech_tree_screen) != null && mActivity.findViewById(R.id.tech_tree_screen).getVisibility() == View.VISIBLE) {
                int oldTechValueX = (int) playerClan.techTree.screenCenterX;
                int oldTechValueY = (int) playerClan.techTree.screenCenterY;
                playerClan.techTree.modifyX(-deltaX / 50f);
                playerClan.techTree.modifyY(deltaY / 30f);
                int newTechValueX = (int) playerClan.techTree.screenCenterX;
                int newTechValueY = (int) playerClan.techTree.screenCenterY;
                //System.out.println("updating " + oldTechValueX + " to " + newTechValueX);
                if (oldTechValueX != newTechValueX || oldTechValueY != newTechValueY) {
                    mActivity.updateTechMenu();
                }
            }
            else if (mousePicker != null) {
                mRenderer.camera.moveShift(-deltaX / 10, 0, -deltaY / 10);
                mRenderer.camera.pointShift(-deltaX/10, 0, -deltaY/10);

                Tile previousSelectedTile = mousePicker.getSelectedTile();
                Entity previousSelectedEntity = mousePicker.getSelectedEntity();
                mousePicker.update(x, y, mRenderer.getCombatMode());
                        /*Vector3f v = mousePicker.rayCastHit;
                        mousePicker.getTileClickedOn();*/
                /*if (mRenderer.getCombatMode()) {
                    if (!mRenderer.worldHandler.world.combatWorld.checkTileWithinZone(mousePicker.getSelectedTile())) {
                        mousePicker.changeSelectedTile(null);
                        mousePicker.changeSelectedUnit(null);
                    }
                    if (previousSelectedEntity != null) {
                        mousePicker.changeSelectedAction("CombatMove");
                    }
                }*/

                if (mousePicker.getSelectedTile() != null && mousePicker.getSelectedTile().improvement != null) {
                    //Force update here
                    mRenderer.worldHandler.needsUpdateOnNextFrame = true;
                }

                executeSelectedAction(mousePicker, previousSelectedTile, previousSelectedEntity);

                if (previousSelectedEntity != null) {
                    if (previousSelectedEntity.actionPoints <= 0 || previousSelectedEntity.actionsQueue.size() > 0) {
                        if (mActivity.turnStyle == LessonSevenActivity.AutomaticTurn.AUTOMATIC) {
                            mRenderer.moveCameraInFramesAfter = 8;
                            mRenderer.nextUnit = mRenderer.findNextUnit();
                        }
                    }
                }

                guiHandler.updateGui(mousePicker);

            }
        }
    }

    public void executeSelectedAction(MousePicker mousePicker, Tile previousSelectedTile, Entity previousSelectedEntity) {
        String action = mousePicker.getSelectedAction();
        if (action == null || action.equals("")) {
            return; //Default, select the unit and only display its stats.
        }
        if (mRenderer.getCombatMode()) {
            if (action.equals("CombatMove")) {
                if (previousSelectedEntity == null) {
                    System.err.println("Invalid 'CombatMove' action, no selected entity before click");
                    mousePicker.changeSelectedAction("");
                    return;
                }
                Tile locationToMove = mousePicker.getSelectedTile();
                Entity entityToChase = mousePicker.getSelectedEntity();
                if (previousSelectedEntity instanceof Person) {
                    Person personSelected = (Person) previousSelectedEntity;
                    if (entityToChase != null) {
                        Action.ActionType actionType = Action.ActionType.COMBAT_MOVE;
                        if (mRenderer.worldSystem.atWar(entityToChase.clan, personSelected.clan)) {
                            /*if (entityToChase.location().dist(personSelected.location()) > 1) {
                                actionType = Action.ActionType.COMBAT_CHASE;
                            } else {
                                actionType = Action.ActionType.COMBAT_ATTACK;
                            }*/
                            actionType = Action.ActionType.COMBAT_CHASE;
                        }
                        personSelected.actionsQueue.add(new CombatAction(actionType, entityToChase));
                        personSelected.executeQueue();
                    }
                    else if (locationToMove != null) {
                        personSelected.actionsQueue.add(new CombatAction(Action.ActionType.COMBAT_MOVE, locationToMove));
                        personSelected.executeQueue();
                    }
                }
                OpenGLRenderer.debounceFrames = 10;
                mousePicker.changeSelectedTile(null);
                mousePicker.changeSelectedAction("");
            }
            /*else if (action.equals("CombatAttack")) {

            }*/
            else {
                System.err.println("Invalid action identifier: " + action);
                mousePicker.changeSelectedTile(null);
                mousePicker.changeSelectedAction("");
            }
        }
        else {
            if (action.equals("Move")) {
                if (previousSelectedEntity == null) {
                    System.err.println("Invalid 'Move' action, no selected entity before click");
                    mousePicker.changeSelectedAction("");
                    return;
                }
                Tile selected = mousePicker.getSelectedTile();
                if (selected == null) {

                } else {
                    if (previousSelectedEntity instanceof Person) {
                        Person personSelected = (Person) previousSelectedEntity;
                        if (!personSelected.location().equals(selected)) {
                            if (mRenderer.worldSystem.allowedToAccessTile(personSelected, selected)) {
                                personSelected.gameMovePath(selected);
                            }
                        }
                    }
                    //previousSelectedEntity.move(mousePicker.getSelectedTile());
                }
                OpenGLRenderer.debounceFrames = 10;
                mousePicker.changeSelectedTile(null);
                mousePicker.changeSelectedAction("");
            }
            else if (action.equals("Fortify")) {
                if (previousSelectedEntity == null) {
                    System.err.println("Invalid 'Fortify' action, no selected entity before click");
                    mousePicker.changeSelectedAction("");
                    return;
                }
                Tile selected = mousePicker.getSelectedTile();
                if (selected == null) {

                } else {
                    if (previousSelectedEntity instanceof Person) {
                        Person personSelected = (Person) previousSelectedEntity;
                        if (!personSelected.location().equals(selected)) {
                            personSelected.gameFortify();
                        }
                    }
                    //previousSelectedEntity.move(mousePicker.getSelectedTile());
                }
                OpenGLRenderer.debounceFrames = 10;
                mousePicker.changeSelectedTile(null);
                mousePicker.changeSelectedAction("");
            }
            /*else if (action.startsWith("Build/")) {
                if (previousSelectedEntity == null) {
                    System.err.println("Invalid 'Build' action, no selected entity before click");
                    mousePicker.changeSelectedAction("");
                    return;
                }
                if (mousePicker.getSelectedTile() == null) {

                } else {
                    if (previousSelectedEntity instanceof Person) {
                        Person personSelected = (Person) previousSelectedEntity;
                        String buildingToBuild = action.substring(6);
                        Tile buildAt = mousePicker.getSelectedTile();
                        Building newBuilding = BuildingFactory.newBuilding(previousSelectedEntity.world, previousSelectedEntity.clan, BuildingType.fromString(buildingToBuild), buildAt, 0);

                        if (!buildAt.equals(personSelected.location())) {
                            personSelected.gameMovePath(buildAt);
                        }
                        personSelected.actionsQueue.add(new PersonAction(Action.ActionType.BUILD, newBuilding));
                        personSelected.executeQueue();
                    }
                }
                OpenGLRenderer.debounceFrames = 10;
                mousePicker.changeSelectedTile(null);
                mousePicker.changeSelectedAction("");
            }*/
            else if (action.equals("InitiateCombat")) {
                mRenderer.setCombatMode(true);
                mousePicker.changeSelectedTile(null);
                mousePicker.changeSelectedAction("");
            }
            else {
                System.err.println("Invalid action identifier: " + action);
                mousePicker.changeSelectedTile(null);
                mousePicker.changeSelectedAction("");
            }
        }
    }

    public void forceUpdate() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateMenu(true);
            }
        });
    }

    public void update() {
        mRenderer.mLessonSevenActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateMenu(false);
            }
        });
    }

    private void updateMenu(boolean forceUpdate) {
        if (mRenderer.getCombatMode()) {
            Button quickSummaryMenu = (Button) mActivity.findViewById(R.id.quick_summary_view);
            quickSummaryMenu.setVisibility(View.INVISIBLE);

            mActivity.findViewById(R.id.build_menu).setVisibility(View.INVISIBLE);

            Button selectedEntityMenu = (Button) mActivity.findViewById(R.id.selected_unit_menu);
            selectedEntityMenu.setVisibility(View.INVISIBLE);

            Button unitMenu = (Button) mActivity.findViewById(R.id.unit_menu);
            unitMenu.setVisibility(View.INVISIBLE);

            mActivity.findViewById(R.id.tech_menu).setVisibility(View.INVISIBLE);

            Button queueMenu = (Button) mActivity.findViewById(R.id.queue_menu);
            queueMenu.setVisibility(View.INVISIBLE);

            PercentRelativeLayout selectedStatMenu = (PercentRelativeLayout) mActivity.findViewById(R.id.selected_stat_menu);
            selectedStatMenu.setVisibility(View.INVISIBLE);

            Button infoMenu = (Button) mActivity.findViewById(R.id.info_menu);
            infoMenu.setVisibility(View.INVISIBLE);

            Button exitCombatMenu = (Button) mActivity.findViewById(R.id.combat_exit_menu);
            exitCombatMenu.setVisibility(View.VISIBLE);

            //mActivity.setContentView(R.layout.combat_view_menu);
        }
        else {
            Button exitCombatMenu = (Button) mActivity.findViewById(R.id.combat_exit_menu);
            exitCombatMenu.setVisibility(View.INVISIBLE);

            if (mousePicker.selectedNeedsUpdating() || forceUpdate) {
                mousePicker.nextFrameSelectedNeedsUpdating = false;

                Tile selectedTile = mousePicker.getSelectedTile();
                Entity selectedEntity = mousePicker.getSelectedEntity();
                Building selectedImprovement = null;
                boolean selectedTileExists = selectedTile != null;
                boolean selectedEntityExists = selectedEntity != null;

                if (selectedTileExists) {
                    selectedImprovement = selectedTile.improvement;
                }
                boolean selectedImprovementExists = selectedImprovement != null;

                Button quickSummaryMenu = (Button) mActivity.findViewById(R.id.quick_summary_view);
                quickSummaryMenu.setVisibility(selectedEntityExists || selectedTileExists ? View.VISIBLE : View.INVISIBLE);
                String stringy = "";
                if (selectedTileExists || selectedEntityExists) {
                    Tile subjectTile = selectedTile;
                    if (!selectedTileExists) {
                        subjectTile = selectedEntity.location();
                    }
                    Clan owner = subjectTile.world.getTileOwner(subjectTile); //, influence = selectedTile.world.getTileInfluence(selectedTile);
                    String affiliation = "";
                    if (owner != null) {
                        affiliation = owner.name + " <{culture}>";
                    } else {
                        affiliation = "Free <{forest}>";
                    }
                    stringy += affiliation;
                    stringy += " " + Tile.Biome.nameFromInt(subjectTile.biome.type) + ", " + Tile.Terrain.nameFromInt(subjectTile.terrain.type);
                    stringy += " <{" + Tile.Terrain.imageName(subjectTile.terrain) + "}>";
                    if (subjectTile.improvement != null) {
                        stringy += " " + subjectTile.improvement.name + " <{building}>";
                    }
                }
                if (selectedEntityExists) {
                    //if (selectedTileExists) {
                        stringy += "\n";
                    //}
                    stringy += selectedEntity.name;
                    /*Clan owner = selectedEntity.clan;
                    String affiliation = "";
                    if (owner != null) {
                        affiliation = owner.name + " <{culture}>";
                    } else {
                        affiliation = "Free <{forest}>";
                    }
                    stringy += " " + affiliation;*/
                    if (selectedEntity instanceof Person) {
                        Person person = (Person) selectedEntity;
                        stringy += " " + person.actionPoints + "/" + person.maxActionPoints + " AP <{action_points}>";
                    }
                    //stringy += " " + selectedEntity.location();
                }
                quickSummaryMenu.setText(stringy);
                OpstrykonUtil.processImageSpan(mActivity, quickSummaryMenu);

                mActivity.findViewById(R.id.build_menu).setVisibility(selectedImprovementExists && playerClan.equals(selectedTile.world.getTileOwner(selectedTile)) ? View.VISIBLE : View.INVISIBLE);
                InfoHelper.addInfoOnLongClick((Button) mActivity.findViewById(R.id.build_menu), new String[]{
                        "This city can build units and improvements.",
                        "These use production from the city."
                });

                mActivity.findViewById(R.id.city_citizen_auto_menu).setVisibility(selectedImprovementExists &&
                        playerClan.equals(selectedTile.world.getTileOwner(selectedTile)) &&
                        selectedImprovement instanceof City
                        ? View.VISIBLE : View.INVISIBLE);

                Button selectedEntityMenu = (Button) mActivity.findViewById(R.id.selected_unit_menu);
                selectedEntityMenu.setVisibility(selectedEntityExists && playerClan.equals(selectedEntity.clan) ? View.VISIBLE : View.INVISIBLE);
                if (selectedEntityExists) {
                    //selectedEntityMenu.setText(mousePicker.getSelectedEntity().name);
                    selectedEntityMenu.setText("Actions");
                }
                InfoHelper.addInfoOnLongClick((Button) mActivity.findViewById(R.id.selected_unit_menu), new String[]{
                        "Units have action points, or AP <{action_points}>,",
                        "which can be used each turn."
                });

                Button unitMenu = (Button) mActivity.findViewById(R.id.unit_menu);
                unitMenu.setVisibility(
                        selectedTileExists || selectedEntityExists ? View.VISIBLE : View.INVISIBLE
                );
                if (selectedTileExists) {
                    unitMenu.setText("Units (" + mousePicker.getSelectedTile().occupants.size() + ")");
                } else if (selectedEntityExists) {
                    unitMenu.setText("Units (" + mousePicker.getSelectedEntity().location().occupants.size() + ")");
                }
                InfoHelper.addInfoOnLongClick((Button) mActivity.findViewById(R.id.unit_menu), new String[]{
                        "This lists the units here,",
                        "which you can select."
                });

                Button queueMenu = (Button) mActivity.findViewById(R.id.queue_menu);
                queueMenu.setVisibility(
                        selectedImprovementExists || selectedEntityExists ? View.VISIBLE : View.INVISIBLE
                );
                if (selectedImprovementExists) {
                    queueMenu.setText("Queue (" + mousePicker.getSelectedTile().improvement.actionsQueue.size() + ")");
                } else if (selectedEntityExists) {
                    queueMenu.setText("Queue (" + mousePicker.getSelectedEntity().actionsQueue.size() + ")");
                }
                InfoHelper.addInfoOnLongClick((Button) mActivity.findViewById(R.id.queue_menu), new String[]{
                        "This is a set of actions",
                        "which are planned for the future."
                });

                LinearLayout cityQueueMenu = (LinearLayout) mActivity.findViewById(R.id.city_queue_menu);
                if (cityQueueMenu != null && cityQueueMenu.getVisibility() == View.VISIBLE) {
                    cityQueueMenu.setVisibility(View.INVISIBLE);
                }

                PercentRelativeLayout selectedStatMenu = (PercentRelativeLayout) mActivity.findViewById(R.id.selected_stat_menu);
                selectedStatMenu.setVisibility(
                        selectedTileExists || selectedEntityExists ? View.VISIBLE : View.INVISIBLE
                );
                //if (selectedEntityExists || selectedTileExists) generateSelectionStatMenu(selectedStatMenu);
                //generateSelectionStatMenu(selectedStatMenu);

                Button infoMenu = (Button) mActivity.findViewById(R.id.info_menu);
                infoMenu.setVisibility(
                        selectedTileExists || selectedEntityExists ? View.VISIBLE : View.INVISIBLE
                );

                mActivity.findViewById(R.id.tech_menu).setVisibility(View.VISIBLE);
                InfoHelper.addInfoOnLongClick((Button) mActivity.findViewById(R.id.tech_menu), new String[]{
                        "This opens the technology tree",
                        "which lists the tech you can research.",
                        "This uses your total science <{science}> output."
                });
                OpstrykonUtil.processImageSpan(mActivity, (Button) mActivity.findViewById(R.id.tech_menu));
            }
        }
    }

    // Hides superclass method.
	public void setRenderer(OpenGLRenderer renderer, float density)
	{
		mRenderer = renderer;
		mDensity = density;
		super.setRenderer(renderer);
	}
}
