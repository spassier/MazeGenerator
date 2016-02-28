package com.roguebot.mazegenerator;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Sebastien PASSIER on 27/02/2016.
 */
public class Maze
{
    private final Dimension dimension;
    private final int maxRoomSize;
    private final int minRoomSize;
    private final int numRoomPositioningTries;

    private ArrayList<Rectangle> rooms;


    private Maze(MazeBuilder builder) {
        this.dimension = builder.dimension;
        this.maxRoomSize = builder.maxRoomSize;
        this.minRoomSize = builder.minRoomSize;
        this.numRoomPositioningTries = builder.numRoomPositioningTries;
    }

    void generate() {

        // Ajouter des pieces
        // L'objectif est double:
        // 1 - garantir que les tailles des rooms est impaire (width et height)
        // 2 - aligné les positions des rooms uniquement sur des position impaires (row et col)

    }

    private void addRooms() {
        for ( int iteration = 0; iteration < numRoomPositioningTries; iteration++ ) {
            Random rand = new Random();

            int width = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize;
            int height = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize;

            // Pour ajouter de l'aléatoire dans le redimentionnement de la largeur ou de la longueure dont la valeur est paire
            int lessOrMore;
            if ( rand.nextInt(1) == 0 ) {
                lessOrMore = 1;
            } else {
                lessOrMore = -1;
            }

            // Traitement des cas aux limites du redimentionnement
            if ( (width & 1) == 0 ) {
                if ( (width + lessOrMore) <= maxRoomSize ) {
                    width++;
                } else {
                    width--;
                }
            }

            if ( (height & 1) == 0 ) {
                if ( (height + lessOrMore) <= maxRoomSize ) {
                    height++;
                } else {
                    height--;
                }
            }

            // Traitement de l'insertion de la room
            

        }
    }

    private boolean roomIntersects(Rectangle roomInserted, Rectangle roomToInsert) {
        boolean result = false;


        return result;
    }

    /**
     * Class builder
     */
    public static class MazeBuilder
    {
        private Dimension dimension;
        private int maxRoomSize = 11;
        private int minRoomSize = 3;
        private int numRoomPositioningTries = 100;


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

        public MazeBuilder maxRoomSize(int value) {
            this.maxRoomSize = value;

            return this;
        }

        public MazeBuilder minRoomSize(int value) {
            this.minRoomSize = value;

            return this;
        }

        public MazeBuilder numRoomPositioningTries(int value) {
            this.numRoomPositioningTries = value;

            return this;
        }
    }
}
