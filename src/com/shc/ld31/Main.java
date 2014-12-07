package com.shc.ld31;

import com.radirius.mercury.framework.Core;
import com.radirius.mercury.framework.Window;
import com.radirius.mercury.framework.splash.SplashScreen;

import com.radirius.mercury.audio.Audio;

import com.radirius.mercury.graphics.Color;
import com.radirius.mercury.graphics.Graphics;
import com.radirius.mercury.graphics.Texture;

import com.radirius.mercury.input.Input;

import com.radirius.mercury.math.geometry.Polygon;
import com.radirius.mercury.math.geometry.Rectangle;
import com.radirius.mercury.math.geometry.Vector2f;

import com.radirius.mercury.resource.Loader;

import com.radirius.mercury.scene.GameObject;
import com.radirius.mercury.scene.GameScene;
import com.radirius.mercury.scene.ShapedEntity;

import com.shc.ld31.entities.Cat;
import com.shc.ld31.entities.Cheese;
import com.shc.ld31.entities.Mouse;
import com.shc.ld31.entities.MouseHole;
import com.shc.ld31.entities.MouseTrap;
import com.shc.ld31.entities.Stone;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Game class. Coordinates everything in this game. This
 * game 'MousyDare' is made for LD31 in just 48 hours, and is
 * also the example game for the Mercury 2D game library.
 *
 * @author Sri Harsha Chilakapati
 */
public class Main extends Core
{
    // The total number of LEVELS in this game
    public static final int TOTAL_LEVELS = 11;

    // Background Textures
    public static Texture GREEN;
    public static Texture STONE;
    public static Texture CHEESE;

    // Mouse Textures
    public static Texture MOUSE_HOLE;
    public static Texture MOUSE_TRAP;
    public static Texture MOUSE_UP;
    public static Texture MOUSE_DOWN;
    public static Texture MOUSE_LEFT;
    public static Texture MOUSE_RIGHT;

    // Cat Textures
    public static Texture CAT_UP;
    public static Texture CAT_DOWN;
    public static Texture CAT_LEFT;
    public static Texture CAT_RIGHT;

    // Polygons for collision detection
    public static Polygon POLY_CAT_UP;
    public static Polygon POLY_CAT_DOWN;
    public static Polygon POLY_CAT_LEFT;
    public static Polygon POLY_CAT_RIGHT;

    // The Grid, to resolve collisions
    public static Grid GRID;

    // Some house-keeping!
    public static int   mapNumber;
    public static int   mapCheese;
    public static float score;

    // Map switching flags
    public static boolean moveNextMap;
    public static boolean reloadCurrentMap;

    // The mouse and the scene
    private Mouse     mouse;
    private GameScene scene;

    // Lists for the cats and the instruction strings
    private List<Cat>    cats;
    private List<String> mapInstructions;

    // Current instruction index
    private int   currentInstruction;

    public static Audio music;
    public static Audio cheese;
    public static Audio hurt;

    /**
     * The constructor to initialize Core
     */
    public Main()
    {
        super("MousyDare Alpha", 800, 600);
        Display.setResizable(true);
    }

    public static void main(String[] args)
    {
        new Main().start();
    }

    @Override
    public void init()
    {
        // Use the splash screen to show some <3 (Not necessary)
        addSplashScreen(SplashScreen.getMercuryDefault());

        // GoHarsha splash screen
        addSplashScreen(new SplashScreen(Texture.loadTexture(Loader.streamFromClasspath("res/logo.png")), 4000));

        // Load the textures
        GREEN = Texture.loadTexture(Loader.streamFromClasspath("res/textures/green.png"));
        STONE = Texture.loadTexture(Loader.streamFromClasspath("res/textures/stone.png"));
        CHEESE = Texture.loadTexture(Loader.streamFromClasspath("res/textures/cheese.png"));

        MOUSE_HOLE = Texture.loadTexture(Loader.streamFromClasspath("res/textures/mouse_hole.png"));
        MOUSE_TRAP = Texture.loadTexture(Loader.streamFromClasspath("res/textures/mouse_trap.png"));

        MOUSE_UP = Texture.loadTexture(Loader.streamFromClasspath("res/textures/mouse.png"));
        MOUSE_DOWN = Texture.loadTexture(Loader.streamFromClasspath("res/textures/mouse_down.png"));
        MOUSE_LEFT = Texture.loadTexture(Loader.streamFromClasspath("res/textures/mouse_left.png"));
        MOUSE_RIGHT = Texture.loadTexture(Loader.streamFromClasspath("res/textures/mouse_right.png"));

        CAT_UP = Texture.loadTexture(Loader.streamFromClasspath("res/textures/cat_up.png"));
        CAT_DOWN = Texture.loadTexture(Loader.streamFromClasspath("res/textures/cat_down.png"));
        CAT_LEFT = Texture.loadTexture(Loader.streamFromClasspath("res/textures/cat_left.png"));
        CAT_RIGHT = Texture.loadTexture(Loader.streamFromClasspath("res/textures/cat_right.png"));

        // Load Polygons
        POLY_CAT_UP    = createFromTexture(CAT_UP,    64, 64);
        POLY_CAT_DOWN  = createFromTexture(CAT_DOWN,  64, 64);
        POLY_CAT_LEFT  = createFromTexture(CAT_LEFT,  64, 64);
        POLY_CAT_RIGHT = createFromTexture(CAT_RIGHT, 64, 64);

        // Load the sounds
        music = Audio.getAudio(Audio.getWAVBuffer(Loader.streamFromClasspath("res/sounds/music.wav")));
        cheese = Audio.getAudio(Audio.getWAVBuffer(Loader.streamFromClasspath("res/sounds/cheese.wav")));
        hurt = Audio.getAudio(Audio.getWAVBuffer(Loader.streamFromClasspath("res/sounds/hurt.wav")));

        // Create the GRID and the scene
        GRID = new Grid(Window.getWidth(), Window.getHeight(), 32);
        scene = new GameScene();

        // Initialize lists
        mapInstructions = new ArrayList<>();
        cats = new ArrayList<>();

        // Load first map
        loadMap(mapNumber = 1);

        // Loop the music!
        music.setLooping(true);
        music.play();
    }

    private boolean isTransparent(int pixel)
    {
        return (pixel >> 24) == 0x00;
    }

    // Utility function to create polygons from textures
    private Polygon createFromTexture(Texture texture, int newWidth, int newHeight)
    {
        ArrayList<Vector2f> vertices = new ArrayList<>();

        BufferedImage img = texture.getSourceImage();

        int width  = img.getWidth();
        int height = img.getHeight();

        int pixel;
        int prevPixel = 0;

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                pixel = img.getRGB(x, y);

                if (isTransparent(pixel))
                {
                    if (!isTransparent(prevPixel))
                        vertices.add(new Vector2f((newWidth/width) * (x-1), (newHeight/height) * y));
                }
                else
                {
                    if (isTransparent(prevPixel))
                        vertices.add(new Vector2f((newWidth/width) * x, (newHeight/height) * y));
                }

                prevPixel = pixel;
            }
        }

        Vector2f[] verts = new Vector2f[vertices.size()];
        verts = vertices.toArray(verts);

        return new Polygon(verts);
    }

    // Utility function to load maps
    private void loadMap(int mapNumber)
    {
        // Read the map
        BufferedReader reader = new BufferedReader(new InputStreamReader(Loader.streamFromClasspath("res/maps/Map" + mapNumber + ".txt")));
        String line;

        // To position the entities in the scene
        float posX = 0;
        float posY = 0;
        float tileWidth = 32;
        float tileHeight = 32;

        // Create a new scene
        scene.cleanup();
        scene = new GameScene();
        GRID.clear();

        // Clear previous level data if exists
        cats.clear();
        mapInstructions.clear();
        currentInstruction = 0;
        mapCheese = 0;

        // Clear map switching flags
        moveNextMap = false;
        reloadCurrentMap = false;

        try
        {
            while ((line = reader.readLine()) != null)
            {
                // The lines that start with # are comments
                if (line.startsWith("#"))
                    continue;

                // The lines which start with @ provide instructions
                if (line.startsWith("@"))
                {
                    mapInstructions.add(line.replaceFirst("@", ""));
                    continue;
                }

                for (char ch : line.toCharArray())
                {
                    ShapedEntity entity = null;

                    switch (ch)
                    {
                        case 'T':
                            entity = new MouseTrap(new Rectangle(posX, posY, tileWidth, tileHeight));
                            break;
                        case 'S':
                            entity = new Stone(new Rectangle(posX, posY, tileWidth, tileHeight));
                            break;

                        case 'M':
                            entity = new Mouse(new Rectangle(posX, posY, tileWidth, tileHeight));
                            scene.add(new MouseHole(new Rectangle(posX, posY, tileWidth, tileHeight)));
                            break;

                        case 'C':
                            mapCheese++;
                            entity = new Cheese(new Rectangle(posX, posY, tileWidth, tileHeight));
                            break;

                        case 'H': // horizontal cat
                            entity = new Cat(new Rectangle(posX, posY, 2 * tileWidth, 2 * tileHeight), true);
                            break;

                        case 'V': // Vertical cat
                            entity = new Cat(new Rectangle(posX, posY, 2 * tileWidth, 2 * tileHeight), false);
                            break;

                        case ' ':
                            break;
                    }

                    // If entity is Mouse, preserve it for collisions
                    if (entity instanceof Mouse)
                        mouse = (Mouse) entity;

                    // If entity is Cat, preserve it for collisions
                    if (entity instanceof Cat)
                        cats.add((Cat) entity);

                    // Add non null entities to the scene
                    if (entity != null)
                        scene.add(entity);

                    posX += tileWidth;
                }

                posX = 0;
                posY += tileHeight;
            }

            // Close the reader
            reader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Add empty map instruction
        mapInstructions.add("");

        // Populate the GRID
        GRID.insertAll(scene.getObjects());
    }

    @Override
    public void update()
    {
        // Q ends the game
        if (Input.keyDown(Input.KEY_Q))
            end();

        // SPACE moves on to the next instruction
        if (Input.keyClicked(Input.KEY_SPACE))
        {
            if (mapInstructions.size() > currentInstruction + 1)
                currentInstruction++;
        }

        // N skips a map (Only for Testing)
        if (mapInstructions.size() == currentInstruction + 1 && Input.keyClicked(Input.KEY_N))
            moveNextMap = true;

        // Pressing ENTER | RETURN in last level restarts game
        if (mapNumber == TOTAL_LEVELS && (Input.keyDown(Input.KEY_ENTER) || Input.keyDown(Input.KEY_RETURN)))
        {
            score = 0;
            loadMap((mapNumber = 1)); // Restart the game
        }

        // Escape skips the instruction
        if (Input.keyClicked(Input.KEY_ESCAPE))
            currentInstruction = mapInstructions.size() - 1;

        // Don't update the scene when we're showing instructions
        if (mapInstructions.size() - 1 != currentInstruction)
            return;

        // If the last level is passed, end the game
        if (mapNumber == TOTAL_LEVELS && mapInstructions.size() == currentInstruction + 1)
            end();

        if (moveNextMap)
            loadMap(++mapNumber);

        if (reloadCurrentMap)
            loadMap(mapNumber);

        // Update the game
        scene.update();

        // Collision detection
        List<GameObject> list = GRID.retrieve(mouse);

        for (GameObject e : list)
        {
            if (((ShapedEntity) (e)).getBounds().intersects(mouse.getBounds()))
            {
                // Pixel Perfect collisions with Cats
                if (e instanceof Cat)
                {
                    Cat cat = (Cat) e;
                    if (cat.getPolygon().intersects(mouse.getBounds()))
                        mouse.collision(cat);
                }
                else
                    mouse.collision((ShapedEntity) e);
            }
        }

        // Check collisions for cats
        for (Cat cat : cats)
        {
            list = GRID.retrieve(cat);

            for (GameObject e : list)
            {
                // Cats only collide with stones
                if (e instanceof Stone)
                    if (((ShapedEntity) e).getBounds().intersects(cat.getBounds()))
                        cat.collision((ShapedEntity) e);
            }
        }

        // Repopulate GRID
        GRID.clear();
        GRID.insertAll(scene.getObjects());
    }

    @Override
    public void render(Graphics g)
    {
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());

        // Draw Tiled background with Grass!!
        for (float posX = 0; posX < 800; posX += 32)
        {
            for (float posY = 0; posY < 600; posY += 32)
            {
                g.drawTexture(GREEN, posX, posY, 32, 32);
            }
        }

        // Render the scene
        scene.render(g);

        // Render a transparent grey box over the scene if
        // instructions haven't been passed
        if (mapInstructions.size() - 1 != currentInstruction)
        {
            Color grey = new Color(0.4f, 0.4f, 0.4f, 0.8f);
            g.setColor(grey);
            g.drawRectangle(0, 0, Display.getWidth(), Display.getHeight());

            // A darker rectangle in the center
            Color black = new Color(0, 0, 0, 0.8f);
            g.setColor(black);
            g.drawRectangle(100, 200, 600, 200);

            // Render the instruction
            float fontX = 400 - g.getFont().getWidth(mapInstructions.get(currentInstruction)) / 2;
            float fontY = 300 - g.getFont().getHeight() / 2;

            g.setColor(Color.WHITE);
            g.drawString(mapInstructions.get(currentInstruction), fontX, fontY);

            String string = "[ESC] [SPACE]";

            g.drawString(string, 690 - g.getFont().getWidth(string),
                    390 - g.getFont().getHeight());

            g.drawTexture(MOUSE_RIGHT, 50, 190, 120, 100);
        }

        // Draw the score
        String scoreStr = "Score: " + (int) score;
        g.drawString(scoreStr, 590 - g.getFont().getWidth(scoreStr), 10);
    }

    @Override
    public void cleanup()
    {
        music.stop();
        music.clean();
    }
}
