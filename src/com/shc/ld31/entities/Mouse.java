package com.shc.ld31.entities;

import com.radirius.mercury.graphics.Graphics;
import com.radirius.mercury.graphics.Texture;
import com.radirius.mercury.input.Input;
import com.radirius.mercury.math.geometry.Rectangle;
import com.radirius.mercury.scene.ShapedEntity;
import com.shc.ld31.Main;

/**
 * @author Sri Harsha Chilakapati
 */
public class Mouse extends ShapedEntity
{
    private Texture texture;
    private int     cheese;

    public static final float VELOCITY = 4;

    public Mouse(Rectangle bounds)
    {
        super(bounds);

        texture = Main.MOUSE_UP;
    }

    public void update()
    {
        // Input
        if (Input.keyDown(Input.KEY_UP))
        {
            texture = Main.MOUSE_UP;
            translate(0, -VELOCITY);
        }

        else if (Input.keyDown(Input.KEY_DOWN))
        {
            texture = Main.MOUSE_DOWN;
            translate(0, VELOCITY);
        }

        else if (Input.keyDown(Input.KEY_LEFT))
        {
            texture = Main.MOUSE_LEFT;
            translate(-VELOCITY, 0);
        }

        else if (Input.keyDown(Input.KEY_RIGHT))
        {
            texture = Main.MOUSE_RIGHT;
            translate(VELOCITY, 0);
        }
    }

    public void collision(ShapedEntity other)
    {
        if (other instanceof Stone)
        {
            Rectangle bounds = getBounds();
            Rectangle otherBounds = other.getBounds();

            // Find the direction based on texture?
            if (texture == Main.MOUSE_UP)
                translateTo(bounds.getX(), otherBounds.getY() + otherBounds.getHeight());

            else if (texture == Main.MOUSE_DOWN)
                translateTo(bounds.getX(), otherBounds.getY() - bounds.getHeight());

            else if (texture == Main.MOUSE_LEFT)
                translateTo(otherBounds.getX() + otherBounds.getWidth(), bounds.getY());

            else if (texture == Main.MOUSE_RIGHT)
                translateTo(otherBounds.getX() - bounds.getWidth(), bounds.getY());
        }

        if (other instanceof Cheese)
        {
            if (!((Cheese) other).isWiped())
            {
                cheese++;
                Main.score += 1.5f * Main.mapNumber + 10;
                Main.cheese.play();
            }

            // Remove from scene
            other.wipe();

            // Remove from grid
            Main.GRID.remove(other);
        }

        if (other instanceof MouseHole)
        {
            Main.moveNextMap = cheese == Main.mapCheese;
        }

        if (other instanceof MouseTrap || other instanceof Cat)
        {
            // Decrease the score
            Main.score -= Main.mapNumber * 10;

            // Clamp the score to zero
            Main.score = Math.max(0, Main.score);

            // Play the sound
            Main.hurt.play();

            // Reload current level
            Main.reloadCurrentMap = true;
        }
    }

    public void render(Graphics g)
    {
        g.drawTexture(texture, getBounds());
    }
}
