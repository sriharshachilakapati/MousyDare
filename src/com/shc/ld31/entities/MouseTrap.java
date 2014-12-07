package com.shc.ld31.entities;

import com.radirius.mercury.graphics.Graphics;
import com.radirius.mercury.math.geometry.Rectangle;
import com.radirius.mercury.scene.ShapedEntity;
import com.shc.ld31.Main;

/**
 * @author Sri Harsha Chilakapati
 */
public class MouseTrap extends ShapedEntity
{
    public MouseTrap(Rectangle bounds)
    {
        super(bounds);
    }

    public void render(Graphics g)
    {
        g.drawTexture(Main.MOUSE_TRAP, this.getBase());
    }
}
