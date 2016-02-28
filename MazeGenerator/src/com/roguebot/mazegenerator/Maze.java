package com.roguebot.mazegenerator;

import java.awt.*;

/**
 * Created by Sebastien on 27/02/2016.
 */
public class Maze
{
    private final Dimension dimension;
    private Rectangle[] rooms;

    private Maze(MazeBuilder builder) {
        this.dimension = builder.dimension;
    }

    void generate() {

        // Ajouter des pieces

    }

    private void addRooms() {

    }

    /**
     * Class builder
     */
    public static class MazeBuilder
    {
        private Dimension dimension;
        private int numRoomPositioningTries = 10;

        public MazeBuilder() {}

        public Maze build() {
            return new Maze(this);
        }

        public MazeBuilder dimension(Dimension dimension) {
            if ( dimension.getWidth() % 2 == 0 || dimension.getHeight() % 2 == 0 ) {
                throw new IllegalArgumentException("Odd values only");
            }

            this.dimension = dimension;

            return this;
        }

        public MazeBuilder numRoomPositioningTries(int value) {
            this.numRoomPositioningTries = value;

            return this;
        }
    }
}
