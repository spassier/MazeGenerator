package com.maze.common;

/**
 * Created by Sebastien on 25/09/2016.
 */
public enum Cell
{
    //private int STATE_FULL;
    //private int TYPE_FULL;


    CELL_FULL(0, 1),
    CELL_EMPTY(10, 1);



    private int state;
    private int type;

    private Cell(final int state, final int type)
    {
        this.state = state;
        this.type = type;
    }
}
