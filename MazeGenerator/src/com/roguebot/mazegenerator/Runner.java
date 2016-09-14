package com.roguebot.mazegenerator;

import java.awt.*;

/**
 * Created by Sebastien PASSIER on 28/02/2016.
 */
public class Runner
{
    public static void main(String[] args)
    {
        Maze maze = new Maze.MazeBuilder().bounds(new Dimension(31,31)).build();
        maze.generate();
        maze.printAscii();
    }
}
