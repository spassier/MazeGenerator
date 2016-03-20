package com.roguebot.mazegenerator;

import com.roguebot.common.Array2D;
import com.roguebot.common.Direction;

import java.awt.*;
import java.util.*;

/**
 * Created by Sebastien PASSIER on 27/02/2016.
 */
public class Maze
{
    private final Dimension bounds;
    private final int maxRoomSize;
    private final int minRoomSize;
    private final int numRoomPositioningTries;
    private final int corridorStraightness;

    private ArrayList<Rectangle> rooms;
    private Array2D regions;
    private Array2D dungeon;

    private int regionID;

    private Maze(MazeBuilder builder) {
        this.bounds = builder.bounds;
        this.maxRoomSize = builder.maxRoomSize;
        this.minRoomSize = builder.minRoomSize;
        this.numRoomPositioningTries = builder.numRoomPositioningTries;
        this.corridorStraightness = builder.corridorStraightness;

        this.rooms = new ArrayList<Rectangle>();
        this.regions = new Array2D(this.bounds.width, this.bounds.height);
        this.dungeon = new Array2D(this.bounds.width, this.bounds.height);
    }

    /**
     * Générer le maze
     */
    void generate() {
        addRooms();
        addCorridors();
        connectRegions();

    }

    /**
     * Afficher en ascii le resultat
     */
    void printAscii() {
        System.out.printf("\n");
        for ( int row = 0; row < bounds.height; row++ ) {
            for ( int col = 0; col < bounds.width; col++ ) {
                if ( dungeon.getCell(col, row) == 0 ) {
                    System.out.printf("#");
                } else {
                    System.out.printf(" ");
                }
            }
            System.out.printf("\n");
        }
        System.out.printf("\n");
    }

    /**
     * Ajouter des corridors dans les espaces restants
     * Implementation de l'algorithmes "flood fill" src: http://www.astrolog.org/labyrnth/algrithm.htm
     */
    private void addCorridors() {
        for ( int row = 1; row < bounds.height; row += 2 ) {
            for ( int col = 1; col < bounds.width; col += 2 ) {
                if ( dungeon.getCell(col, row) == 0 ) {
                    carveCorridor(col, row);
                }
            }
        }
    }

    /**
     * Ajouter des rooms disjointes les unes des autres
     * L'objectif est double:
     *      1 - Garantir que les tailles des rooms est impaire (width et height)
     *      2 - Aligner les positions des rooms uniquement sur des position impaires (row et col)
     */
    private void addRooms() {
        Random rand = new Random();

        for ( int iteration = 0; iteration < numRoomPositioningTries; iteration++ ) {
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

            // La room est insérée uniquement si elle est disjointe des autres
            Rectangle bounds = new Rectangle(room.x - 1, room.y - 1, room.width + 1, room.height + 1);
            boolean intersects = false;
            for ( Rectangle item : rooms ) {
                if ( item.intersects(bounds) ) {
                    intersects = true;
                    break;
                }
            }

            if ( !intersects )
            {
                rooms.add(room);

                // Creuse une nouvelle région
                newRegion();
                for (int row = room.y; row < room.y + room.height; row++)
                {
                    for (int col = room.x; col < room.x + room.width; col++)
                    {
                        regions.setCell(col, row, regionID);
                        dungeon.setCell(col, row, 1);
                    }
                }
            }
        }
    }

    /**
     * Determine si un corridor peut être creusé depuis une cell à une position (x,y) vers une cell adjacent selon une direction
     * Le creusement se fait par pas de 2 cell pour garantir un espace entre les corridors
     * @param x Position x du corridor
     * @param y Position y du corridor
     * @param direction Direction du creusement souhaité
     */
    private boolean canCarve(int x, int y, Direction direction) {
        boolean result = false;
        int xDirection = x + direction.x * 3;
        int yDirection = y + direction.y * 3;

        // La destination doit être dans le périmétre
        if ( xDirection > 0 && xDirection < bounds.width && yDirection > 0 && yDirection < bounds.height ) {
            xDirection = x + direction.x * 2;
            yDirection = y + direction.y * 2;
            // La destination doit être pleine
            if ( dungeon.getCell(xDirection , yDirection ) == 0 ) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Creuser des corridors dans les zones restantes entres les rooms
     * Implementation des algorithmes "growing tree" src: http://www.astrolog.org/labyrnth/algrithm.htm
     * @param x Position x de départ
     * @param y Posiiton y de départ
     */
    private void carveCorridor(int x, int y) {
        Stack<Point> cells = new Stack<Point>();
        Direction lastDirection = Direction.NONE;
        Random rand = new Random();

        newRegion();
        regions.setCell(x, y, regionID);
        dungeon.setCell(x, y, 1);

        cells.push(new Point(x, y));

        while ( !cells.isEmpty() ) {
            Point cell = cells.peek();

            // Determine toutes les directions possible au creusement
            Stack<Direction> possibleCells = new Stack<Direction>();
            for ( Direction direction : Direction.CARDINAL ) {
                if ( canCarve(cell.x, cell.y, direction) ) {
                    possibleCells.push(direction);
                }
            }

            if ( !possibleCells.isEmpty() ) {
                Direction direction;

                // On préférer creuser dans la même direction mais elle reste pondérée par une variable aléatoire de customisation
                if ( possibleCells.contains(lastDirection) && rand.nextInt(100) < corridorStraightness) {
                    direction = lastDirection;
                } else {
                    direction = possibleCells.elementAt(rand.nextInt(possibleCells.size()));
                }

                // Le creusement se fait par pas de 2 pour garantir un espace entre les corridors
                regions.setCell(cell.x + direction.x, cell.y + direction.y, regionID);
                regions.setCell(cell.x + direction.x * 2, cell.y + direction.y * 2, regionID);
                dungeon.setCell(cell.x + direction.x, cell.y + direction.y, 1);
                dungeon.setCell(cell.x + direction.x * 2, cell.y + direction.y * 2, 1);

                // Le choix de la futur direction est faite depuis le "bout du corridor" c'est pourquoi la cell intermédiare n'est pas ajoutée dans la pile
                cells.push(new Point(cell.x + direction.x * 2, cell.y + direction.y * 2));
                lastDirection = direction;
            } else {
                cells.pop();
                lastDirection = Direction.NONE;
            }
        }
    }

    /**
     * Creuser une jonction entre 2 regions différentes
     * @param x Position x de la jonction
     * @param y Position y de la jonction
     */
    private void carveJunction(int x, int y) {
        // FIXME: c'est ici qu'on peut introduire des portes (ouvertes ou fermées), pièges, déclencheurs etc
        dungeon.setCell(x, y, 1);
    }

    /**
     *
     */
    private void connectRegions() {
        HashMap<Point, HashSet> connectorRegions = new HashMap<Point, HashSet>();

        // Identifier les cells candidates à la connexion, c'est à dire :
        // - de type solide
        // - adjacente à 2 regions différentes
        for ( int row = 1; row < bounds.height - 1; row ++ ) {
            for ( int col = 1; col < bounds.width - 1; col++ ) {
                if ( dungeon.getCell(col, row) == 0 ) {
                    HashSet<Integer> connectedRegions = new HashSet<Integer>();

                    for ( Direction direction : Direction.CARDINAL ) {
                        int regionID = regions.getCell(col + direction.x, row + direction.y);
                        if ( regionID != 0 ) {
                            connectedRegions.add(new Integer(regionID));
                        }
                    }

                    if ( connectedRegions.size() >= 2 ) {
                        connectorRegions.put(new Point(col, row), connectedRegions);
                    }
                }
            }
        }

        // Initialisation des buffer de track des ID des regions fusionnées et restantes
        ArrayList<Integer> mergedRegions = new ArrayList<Integer>();
        HashSet<Integer> remainingRegions = new HashSet<Integer>();
        for ( int index = 0; index <= regionID; index++ ) {
            mergedRegions.set(index, new Integer(index));
            remainingRegions.add(new Integer(index));
        }


        // Connecter et fusionner les regions jusqu'à ce qu'il en reste qu'une seule
        while ( remainingRegions.size() > 1 ) {
            
        }

    }
    /**
     * Création d'un nouvel ID de région
     */
    private void newRegion() {
        regionID++;
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
        private int corridorStraightness = 50;


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

        public MazeBuilder corridorStraightness(int value) {
            this.corridorStraightness = value;

            return this;
        }
    }
}
