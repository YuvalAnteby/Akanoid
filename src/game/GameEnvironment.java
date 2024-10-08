package game;

import sprites.geometry.Line;
import sprites.geometry.Point;
import sprites.collision.Collidable;
import sprites.collision.CollisionInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to calculate collisions of the collidable objects and hold constant variables related to the game.
 *
 * @author Yuval Anteby
 */
public class GameEnvironment {

    private final List<Collidable> collidables;

    /**
     * Default constructor for the class, will create an empty array list.
     */
    public GameEnvironment() {
        this.collidables = new ArrayList<>();
    }

    /**
     * Function to add collidable objects to the game environment.
     *
     * @param c new collidable object to be added.
     */
    public void addCollidable(Collidable c) {
        if (c != null) {
            collidables.add(c);
        }
    }

    /**
     * Function to remove collidable objects to the game environment.
     *
     * @param c collidable object to be removed.
     */
    public void removeCollidable(Collidable c) {
        if (c != null) {
            collidables.remove(c);
        }
    }

    /**
     * Assuming the object is moving from the start point to the end point, check if there are any collision in its
     * path. Add every collision to the list.
     *
     * @param trajectory a line of the movement from start to end.
     * @return the closest collision point to the start point of the trajectory. If there are no collisions - null.
     */
    public CollisionInfo getClosestCollision(Line trajectory) {
        CollisionInfo closestCollision = null;
        double closestDistance = Double.MAX_VALUE;

        for (Collidable c : collidables) {
            Point intersection = trajectory.closestIntersectionToStartOfLine(c.getCollisionRectangle());
            if (intersection != null) {
                double distance = trajectory.start().distance(intersection);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestCollision = new CollisionInfo(intersection, c);
                }
            }
        }

        return closestCollision;
    }
}
