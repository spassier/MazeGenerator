package com.roguebot.common;

/**
 * Created by Sebastien PASSIER on 12/03/2016.
 */
public enum Direction
{
    NONE(0,0),
    N(0, -1),
    NE(1, -1),
    E(1, 0),
    SE(1, 1),
    S(0, 1),
    SW(-1, 1),
    W(-1, 0),
    NW(-1, -1);

    public int x;
    public int y;

    public static Direction[] CARDINAL = {N, E, W, S};

    /**
     * Constructeur
     * @param x Direction sur x
     * @param y Direction sur y
     */
    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
