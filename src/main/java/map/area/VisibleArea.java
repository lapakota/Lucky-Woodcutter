package map.area;

import config.Config;
import creatures.Direction;
import map.Cell;
import map.Point;


public class VisibleArea extends Area {
    private final int leftXBorderDist = Config.getScreenWidth() / 2 / Cell.cellSize;
    private final int leftYBorderDist = Config.getScreenHeight() / 2 / Cell.cellSize;
    private final int rightXBorderDist = Config.getScreenWidth() / 2 / Cell.cellSize + 1;
    private final int rightYBorderDist = Config.getScreenHeight() / 2 / Cell.cellSize + 1;

    public VisibleArea(int x, int y) {
        super(x, y);
        setUpdatedActiveCords(new Point(x, y));
    }

    public VisibleArea(Point cords) {
        super(cords);
        setUpdatedActiveCords(cords);
    }


    protected int getLeftBoundX() {
        return getX() - leftXBorderDist;
    }

    protected int getLeftBoundY() {
        return getY() - leftYBorderDist;
    }

    protected int getRightBoundX() {
        return getX() + rightXBorderDist;
    }

    protected int getRightBoundY() {
        return getY() + rightYBorderDist;
    }


    @Override
    public void updateBounds(Direction dir) {
    }
}
