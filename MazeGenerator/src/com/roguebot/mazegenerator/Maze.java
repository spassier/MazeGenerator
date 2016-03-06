package com.roguebot.mazegenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Sebastien PASSIER on 27/02/2016.
 */
public class Maze
{
    private final Dimension bounds;
    private final int maxRoomSize;
    private final int minRoomSize;
    private final int numRoomPositioningTries;

    private ArrayList<Rectangle> rooms;


    private Maze(MazeBuilder builder) {
        this.bounds = builder.bounds;
        this.maxRoomSize = builder.maxRoomSize;
        this.minRoomSize = builder.minRoomSize;
        this.numRoomPositioningTries = builder.numRoomPositioningTries;
    }

    void generate() {

        // Ajouter des pieces
        // L'objectif est double:
        // 1 - garantir que les tailles des rooms est impaire (width et height)
        // 2 - aligné les positions des rooms uniquement sur des position impaires (row et col)

        addRooms();
    }

    private void addRooms() {
        for ( int iteration = 0; iteration < numRoomPositioningTries; iteration++ ) {
            Random rand = new Random();

            int width = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize;
            int height = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize;

            // Pour ajouter de l'aléatoire dans le redimentionnement de la largeur ou de la longueure lorsque la valeur est paire
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

            int x = rand.nextInt((bounds.width - width) / 2) * 2 + 1;
            int y = rand.nextInt((bounds.height - height) / 2) * 2 + 1;

            Rectangle room = new Rectangle(x, y, width, height);
            

            //System.out.printf("room : w=%d / h=%d %n", width, height);

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
        private Dimension bounds;
        private int maxRoomSize = 11;
        private int minRoomSize = 3;
        private int numRoomPositioningTries = 100;


        public MazeBuilder() {}

        public Maze build() {
            return new Maze(this);
        }

        public MazeBuilder bounds(Dimension bounds) {
            if ( bounds.getWidth() % 2 == 0 || bounds.getHeight() % 2 == 0 ) {
                throw new IllegalArgumentException("Odd values only");
            }

            this.bounds = bounds;

            return this;
        }

        public MazeBuilder bounds(int width, int height) {
            if ( width % 2 == 0 || height % 2 == 0 ) {
                throw new IllegalArgumentException("Odd values only");
            }

            this.bounds.width = width;
            this.bounds.height = height;

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
