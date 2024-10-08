package sprites.geometry;

import biuoop.DrawSurface;
import sprites.collision.HitListener;
import sprites.collision.HitNotifier;
import game.Block;
import util.Constants;
import game.GameLevel;
import game.GameEnvironment;
import sprites.Sprite;
import sprites.collision.CollisionInfo;
import sprites.Velocity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a ball in the GUI.
 *
 * @author Yuval Anteby
 */
public class Ball implements Sprite, HitNotifier {
    private Point center;
    private final int r;
    private Color color;
    private Velocity velocity;
    private GameEnvironment environment;
    private final List<HitListener> hitListeners = new ArrayList<>();

    /**
     * Constructor for the ball class using integers for center point.
     *
     * @param x        x value of the center.
     * @param y        y value of the center.
     * @param r        ball's radius.
     * @param color    color of the ball to be filled by.
     * @param velocity the velocity of the ball for animations.
     */
    public Ball(int x, int y, int r, Color color, Velocity velocity) {
        this.center = new Point(x, y);
        this.r = r;
        this.color = color;
        this.velocity = velocity;
    }

    /**
     * Get the x value of the center of this ball.
     *
     * @return x integer value of the center point.
     */
    public int getX() {
        return (int) this.center.getX();
    }

    /**
     * Get the y value of the center of this ball.
     *
     * @return y integer value of the center point.
     */
    public int getY() {
        return (int) this.center.getY();
    }

    /**
     * Get the radius size of this ball.
     *
     * @return integer value of this ball's radius.
     */
    public int getSize() {
        return this.r;
    }

    /**
     * Get the color of the ball.
     *
     * @return color of the ball.
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Setter for the ball's color.
     *
     * @param color new color for the ball.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Get the ball's velocity variable.
     *
     * @return the ball's velocity.
     */
    public Velocity getVelocity() {
        return this.velocity;
    }

    /**
     * Change the game environment variable of the ball.
     *
     * @param gameEnvironment new game environment for the ball.
     */
    public void setGameEnvironment(GameEnvironment gameEnvironment) {
        this.environment = gameEnvironment;
    }

    /**
     * Change the center point of the ball according to the current velocity.
     */
    public void moveOneStep() {
        //Make sure we have a game environment set for the ball.
        if (environment == null) {
            return;
        }
        //Calculate the trajectory of the ball and get info for potential sprites.collision.
        Line path = new Line(center, this.velocity.applyToPoint(center));
        CollisionInfo hitInfo = this.environment.getClosestCollision(path);
        if (hitInfo == null) {
            //No sprites.collision was detected, keep moving.
            this.center = this.getVelocity().applyToPoint(this.center);
        } else {
            //Check if we hit a death block.
            if (hitInfo.collisionObject().getCollisionBlock().isDeathBlock()) {
                notifyExit(hitInfo.collisionObject().getCollisionBlock(), this);
            }
            // Calculate the new velocity after hitting an object.
            Velocity newVelocity = hitInfo.collisionObject().hit(this, hitInfo.collisionPoint(), this.velocity);
            // Adjust the position to be slightly away from the sprites.collision point.
            this.center = moveToCollision(hitInfo.collisionPoint(), this.velocity);
            this.velocity = newVelocity;
        }
        checkBoundaryCollision();
    }

    /**
     * Move the ball to collision point and adjust to prevent sticking to blocks.
     *
     * @param collisionPoint point of sprites.collision.
     * @param velocity       current velocity of the ball.
     * @return the new center point of the ball.
     */
    public Point moveToCollision(Point collisionPoint, Velocity velocity) {
        double adjustedX = collisionPoint.getX();
        double adjustedY = collisionPoint.getY();
        if (velocity.getDx() < 0) {
            adjustedX += this.r;
        } else if (velocity.getDx() > 0) {
            adjustedX -= this.r;
        }
        if (velocity.getDy() < 0) {
            adjustedY += this.r;
        } else if (velocity.getDy() > 0) {
            adjustedY -= this.r;
        }
        return new Point(adjustedX, adjustedY);
    }

    /**
     * Check collision on GUI boundaries and adjust accordingly the center and velocity.
     * The ball must stay entirely in the GUI, including the boundaries.
     */
    private void checkBoundaryCollision() {
        //Check top boundary.
        int minY = Constants.BOUNDS_HEIGHT + this.r;
        if (this.center.getY() <= minY) {
            this.velocity.setDy(-this.velocity.getDy());
            this.center.setY(this.r + Constants.BOUNDS_HEIGHT);
        }
        //Check right boundary.
        int maxX = Constants.GUI_WIDTH - Constants.BOUNDS_WIDTH - this.r;
        if (this.center.getX() >= maxX) {
            this.velocity.setDx(-this.velocity.getDx());
            this.center.setX(Constants.GUI_WIDTH - this.r - Constants.BOUNDS_WIDTH);
        }
        //Check left boundary.
        int minX = Constants.BOUNDS_WIDTH + this.r;
        if (this.center.getX() <= minX) {
            this.velocity.setDx(-this.velocity.getDx());
            this.center.setX(this.r + Constants.BOUNDS_WIDTH);
        }
    }

    /**
     * Add the ball to the game as a sprite.
     *
     * @param g instance of a game.
     */
    public void addToGame(GameLevel g) {
        g.addSprite(this);
    }

    /**
     * Function to update all hit listeners upon a ball's GUI exit.
     *
     * @param beingHit the death block that the ball hit.
     * @param exitBall the ball that exist the GUI.
     */
    private void notifyExit(Block beingHit, Ball exitBall) {
        // Make a copy of the hitListeners before iterating over them.
        List<HitListener> listeners = new ArrayList<>(this.hitListeners);
        // Notify all listeners about a hit event:
        for (HitListener hl : listeners) {
            hl.hitEvent(beingHit, exitBall);
        }
    }

    /**
     * Function to remove this ball from the game.
     *
     * @param gameLevel game reference to remove the ball from.
     */
    public void removeFromGame(GameLevel gameLevel) {
        if (gameLevel != null) {
            gameLevel.removeSprite(this);
            //Changing to iterator for (enhanced for) requires further code changes for it.
            for (int i = 0; i < this.hitListeners.size(); i++) {
                removeHitListener(this.hitListeners.get(i));
            }
        }
    }

    @Override
    public void drawOn(DrawSurface surface) {
        surface.setColor(this.color);
        surface.fillCircle(this.getX(), this.getY(), this.getSize());
        surface.setColor(Color.BLACK);
        surface.drawCircle(this.getX(), this.getY(), this.getSize());
    }

    @Override
    public void timePassed() {
        moveOneStep();
    }

    @Override
    public void addHitListener(HitListener hl) {
        if (hl != null) {
            this.hitListeners.add(hl);
        }
    }

    @Override
    public void removeHitListener(HitListener hl) {
        if (hl != null) {
            this.hitListeners.remove(hl);
        }
    }

    @Override
    public String toString() {
        return "center: " + center + ", r: " + r + ", color: " + color + ", velocity: " + velocity;
    }
}

