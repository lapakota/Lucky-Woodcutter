package visualizer;

import config.GameConfig;
import game.Game;
import graphics.sprites.EffectsSprites;
import map.Cell;
import network.Client;
import network.Lock;
import utils.FPSCounter;
import gameLogic.handlers.ClientKeyHandler;
import gameLogic.handlers.ClientMouseHandler;
import worldObjects.destructibleObjects.Resources;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GameWindow extends JPanel implements Runnable {
    private boolean running;
    private final int width;
    private final int height;
    private BufferedImage bufferedImage;
    private Graphics2D graphics2D;
    private static Thread thread;
    private final Game game;

    public GameWindow(int width, int height, Game game) {
        this.width = width;
        this.height = height;
        this.game = game;
        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        requestFocus();
    }

    @Override
    public void run() {
        long now;
        long updateTime;
        long wait;

        final int TARGET_FPS = 60;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
        init();
        while (running) {
            FPSCounter.StartCounter();
            now = System.nanoTime();
            update();
            updateTime = System.nanoTime() - now;
            wait = (OPTIMAL_TIME - updateTime) / 1000000;
            if (!game.isSoloGame() && !Lock.isLockClient) {
                try {
                    Client.start(null);
                    game.setGameMap(Client.getGameMap());
                    Thread.sleep(100);
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(wait);
            } catch (Exception e) {
                e.printStackTrace();
            }
            FPSCounter.StopAndPost();
        }

        setVisible(false);
    }

    public void init() {
        addNotify();
        running = true;
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        graphics2D = bufferedImage.createGraphics();
        this.addKeyListener(new ClientKeyHandler(game.getPlayer(), game));
        var mouseHandler = new ClientMouseHandler(game.getPlayer(), game);
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
    }

    @Override
    public void paintComponent(Graphics G) {
        Graphics2D Graph2D = (Graphics2D) G;
        if (bufferedImage == null) {
            System.err.println("BuffImg is null");
        }
        graphics2D = bufferedImage.createGraphics();
        graphics2D.clearRect(0, 0, GameConfig.getScreenWidth(), GameConfig.getScreenHeight());
        for (var point : game.getPlayer().getVisibleArea().getActiveCords()) {
            Cell cell = game.getGameMap().getMap().get(point);
            if (cell != null) {
                for (var worldObj : cell.getObjectsInCell()) {
                    worldObj.setSpriteSheet();
                    graphics2D.drawImage(worldObj.getSpriteSheet(),
                            cell.getX() - game.getPlayer().getCamera().getXOffset(),
                            cell.getY() - game.getPlayer().getCamera().getYOffset(),
                            Cell.cellSize, Cell.cellSize, null);
                }
            }

        }
        // отрисовка активной области игрока
        for (var point : game.getPlayer().getHandsArea().getActiveCords()) {
            Cell cell = game.getGameMap().getMap().get(point);
            if (cell != null) {
                graphics2D.drawImage(EffectsSprites.ACTIVE,
                        cell.getX() - game.getPlayer().getCamera().getXOffset(),
                        cell.getY() - game.getPlayer().getCamera().getYOffset(),
                        Cell.cellSize, Cell.cellSize, null);
            }
        }
        graphics2D.setFont(new Font("Segoe UI Black", Font.PLAIN, 30));
        //graphics2D.setColor(Color.MAGENTA);
        graphics2D.drawString("Scores: " + game.getPlayer().getScores(), 10, 30);
        var inv = game.getPlayer().getInventory().getContainer();
        var woodCount = inv.get(Resources.Wood);
        var stoneCount = inv.get(Resources.Stone);
        if (woodCount == null)
            woodCount = 0;
        if (stoneCount == null)
            stoneCount = 0;
        graphics2D.setFont(new Font("Segoe UI Black", Font.PLAIN, 22));
        graphics2D.drawString("Wood: " + woodCount, 10, 60);
        graphics2D.drawString("Stone: " + stoneCount, 10, 90);

        graphics2D.dispose();
        Graph2D.drawImage(bufferedImage, 0, 0, this);
    }


    public synchronized void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this, "GameThread");
            thread.start();
        }
    }

    public void update() {
        repaint();
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }
}
