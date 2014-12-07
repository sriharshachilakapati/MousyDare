package com.shc.ld31.entities;

import com.radirius.mercury.graphics.Graphics;
import com.radirius.mercury.graphics.Texture;
import com.radirius.mercury.math.geometry.Polygon;
import com.radirius.mercury.math.geometry.Rectangle;
import com.radirius.mercury.scene.ShapedEntity;
import com.shc.ld31.Main;

/**
 * @author Sri Harsha Chilakapati
 */
public class Cat extends ShapedEntity
{
    private boolean horizontal;
    private Texture texture;

    private int direction; // Negative (left/up) Positive (right/down)

    public static final float VELOCITY = 4;

    public Cat(Polygon bounds, boolean horizontal)
    {
        super(bounds);

        this.horizontal = horizontal;

        if (horizontal)
            texture = Main.CAT_RIGHT;
        else
            texture = Main.CAT_DOWN;

        direction = (texture == Main.CAT_RIGHT || texture == Main.CAT_DOWN) ? 1 : -1;
    }

    public void update()
    {
        if (horizontal)
            translate(direction * VELOCITY, 0);
        else
            translate(0, direction * VELOCITY);
    }

    public void collision(ShapedEntity other)
    {
        if (other instanceof Stone || other instanceof Cat)
        {
            // Prevent penetrating the boxes
            Rectangle bounds = getBounds();
            Rectangle otherBounds = other.getBounds();

            if (texture == Main.CAT_UP)
                translateTo(bounds.getX(), otherBounds.getY() + otherBounds.getHeight() + 1);

            else if (texture == Main.CAT_DOWN)
                translateTo(bounds.getX(), otherBounds.getY() - bounds.getHeight() - 1);

            else if (texture == Main.CAT_LEFT)
                translateTo(otherBounds.getX() + otherBounds.getWidth() + 1, bounds.getY());

            else if (texture == Main.CAT_RIGHT)
                translateTo(otherBounds.getX() - bounds.getWidth() - 1, bounds.getY());

            // reverse direction
            direction = -direction;

            if (horizontal)
                texture = (direction == -1) ? Main.CAT_LEFT : Main.CAT_RIGHT;
            else
                texture = (direction == -1) ? Main.CAT_UP : Main.CAT_DOWN;
        }
    }

    public void render(Graphics g)
    {
        g.drawTexture(texture, getBounds());
    }

    public Polygon getPolygon()
    {
        Polygon polygon;

        if (horizontal)
            polygon = (direction == -1) ? Main.POLY_CAT_LEFT : Main.POLY_CAT_RIGHT;
        else
            polygon = (direction == -1) ? Main.POLY_CAT_UP : Main.POLY_CAT_DOWN;

        polygon.translateTo(getPosition().getX(), getPosition().getY());

        return polygon;
    }
}
