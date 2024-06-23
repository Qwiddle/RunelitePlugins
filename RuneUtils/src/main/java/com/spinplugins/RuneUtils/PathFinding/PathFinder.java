package com.spinplugins.RuneUtils.PathFinding;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.EthanApiPlugin.PathFinding.GlobalCollisionMap;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.ObjectPackets;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Tile;
import net.runelite.api.WallObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Singleton
public class PathFinder {
    private static final Logger log = LoggerFactory.getLogger(PathFinder.class);

    @Setter
    public List<WorldPoint> path = new ArrayList<>();
    public List<WorldPoint> fullPath = new ArrayList<>();
    public WorldPoint currentPathDestination = null;
    public WorldPoint goal = null;
    public Plugin activePlugin = null;
    private Client client = null;

    Random rand = new Random();

    public PathFinder(Plugin activePlugin, Client client) {
        this.activePlugin = activePlugin;
        this.client = client;
    }

    public boolean pathingTo(WorldPoint point){
        return this.goal != null && this.goal.equals(point);
    }

    public boolean pathing(){
        return this.goal != null;
    }

    public boolean walkTo(WorldPoint goal){
        this.currentPathDestination = null;
        this.path = GlobalCollisionMap.findPath(goal);

        if(this.path == null || this.path.isEmpty()){
            return false;
        }

        this.fullPath = new ArrayList<>(this.path);
        this.goal = goal;

        return true;
    }

    public boolean hasFinishedPathing(){
        if (goal != null && goal.equals(this.client.getLocalPlayer().getWorldLocation())) {
            log.info("reached goal");

            goal = null;
            path = null;
            currentPathDestination = null;

            return true;
        }

        return false;
    }

    public void path() {
        if (this.path != null && !this.path.isEmpty()) {
            if(this.currentPathDestination != null
                    && !this.currentPathDestination.equals(EthanApiPlugin.playerPosition())
                    && !EthanApiPlugin.isMoving()) {
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(this.currentPathDestination);
                log.info("stopped walking. clicking destination again");
            }

            if (this.currentPathDestination == null
                    || this.currentPathDestination.equals(EthanApiPlugin.playerPosition())
                    || this.currentPathDestination.distanceTo(EthanApiPlugin.playerPosition()) <= 5
                    || !EthanApiPlugin.isMoving()) {
                int step = rand.nextInt((35 - 10) + 1) + 10;
                int max = step;

                for (int i = 0; i < step; i++) {
                    if (path.size() - 2 >= i) {
                        if (isDoored(path.get(i), path.get(i + 1))) {
                            max = i;
                            break;
                        }
                    }
                }

                if(isDoored(EthanApiPlugin.playerPosition(), path.get(0))){
                    log.info("doored");
                    WallObject wallObject = null;
                    Tile pathTile = getTile(EthanApiPlugin.playerPosition());

                    if (pathTile != null) {
                        wallObject = pathTile.getWallObject();
                    }

                    if (wallObject == null){
                        Tile pathTile2 = getTile(path.get(0));

                        if (pathTile2 != null) {
                            wallObject = pathTile2.getWallObject();
                        }
                    }

                    ObjectPackets.queueObjectAction(wallObject,false,"Open","Close");
                    return;
                }

                step = Math.min(max, path.size() - 1);
                this.currentPathDestination = path.get(step);

                if (path.indexOf(this.currentPathDestination) == path.size() - 1) {
                    path = null;
                } else {
                    path = path.subList(step + 1, path.size());
                }

                if (this.currentPathDestination.distanceTo(EthanApiPlugin.playerPosition()) <= 4) {
                    return;
                }

                log.info("taking a step to " + this.currentPathDestination.toString());
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(this.currentPathDestination);
            }
        }
    }

    private boolean isDoored(WorldPoint a, WorldPoint b) {
        Tile tA = getTile(a);
        Tile tB = getTile(b);
        if (tA == null || tB == null) {
            return false;
        }
        return isDoored(tA, tB);
    }

    private boolean isDoored(Tile a, Tile b) {
        WallObject wallObject = a.getWallObject();
        if (wallObject != null) {
            ObjectComposition objectComposition = EthanApiPlugin.getClient().getObjectDefinition(wallObject.getId());
            if (objectComposition == null) {
                return false;
            }
            boolean found = false;
            for (String action : objectComposition.getActions()) {
                if (action != null && action.equals("Open")) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
            int orientation = wallObject.getOrientationA();
            if (orientation == 1) {
                //blocks west
                if (a.getWorldLocation().dx(-1).equals(b.getWorldLocation())) {
                    return true;
                }
            }
            if (orientation == 4) {
                //blocks east
                if (a.getWorldLocation().dx(+1).equals(b.getWorldLocation())) {
                    return true;
                }
            }
            if (orientation == 2) {
                //blocks north
                if (a.getWorldLocation().dy(1).equals(b.getWorldLocation())) {
                    return true;
                }
            }
            if (orientation == 8) {
                //blocks south
                return a.getWorldLocation().dy(-1).equals(b.getWorldLocation());
            }
        }
        WallObject wallObjectb = b.getWallObject();
        if (wallObjectb == null) {
            return false;
        }
        ObjectComposition objectCompositionb = EthanApiPlugin.getClient().getObjectDefinition(wallObjectb.getId());
        if (objectCompositionb == null) {
            return false;
        }
        boolean foundb = false;
        for (String action : objectCompositionb.getActions()) {
            if (action != null && action.equals("Open")) {
                foundb = true;
                break;
            }
        }
        if (!foundb) {
            return false;
        }
        int orientationb = wallObjectb.getOrientationA();
        if (orientationb == 1) {
            //blocks east
            if (b.getWorldLocation().dx(-1).equals(a.getWorldLocation())) {
                return true;
            }
        }
        if (orientationb == 4) {
            //blocks south
            if (b.getWorldLocation().dx(+1).equals(a.getWorldLocation())) {
                return true;
            }
        }
        if (orientationb == 2) {
            //blocks south
            if (b.getWorldLocation().dy(+1).equals(a.getWorldLocation())) {
                return true;
            }
        }
        if (orientationb == 8) {
            //blocks north
            return b.getWorldLocation().dy(-1).equals(a.getWorldLocation());
        }
        return false;
    }

    private Tile getTile(WorldPoint point) {
        LocalPoint a = LocalPoint.fromWorld(EthanApiPlugin.getClient(), point);
        if (a == null) {
            return null;
        }
        return EthanApiPlugin.getClient().getScene().getTiles()[point.getPlane()][a.getSceneX()][a.getSceneY()];
    }
}
