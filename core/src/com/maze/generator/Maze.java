package com.maze.generator;

import com.maze.common.Array2D;
import com.maze.common.Direction;

import java.awt.*;
import java.util.*;
import java.util.List;

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
    private final int extraConnectorChance;

    private ArrayList<Rectangle> rooms;
    private Array2D regions; // Chaque cell est tagguée par l'id de la region dont la valeur [0, n]
    private Array2D dungeon; // Chaque cell est tagguée par 0 ou 1 pour indiqué si la celle est pleine ou vide

    private int regionID = 0; // 0 est l'ID des murs (cell pleine)

    public int width;
    public int height;


    private Maze(MazeBuilder builder) {
        this.bounds = builder.bounds;
        this.maxRoomSize = builder.maxRoomSize;
        this.minRoomSize = builder.minRoomSize;
        this.numRoomPositioningTries = builder.numRoomPositioningTries;
        this.corridorStraightness = builder.corridorStraightness;
        this.extraConnectorChance = builder.extraConnectorChance;

        this.rooms = new ArrayList<Rectangle>();
        this.regions = new Array2D(this.bounds.width, this.bounds.height);
        this.dungeon = new Array2D(this.bounds.width, this.bounds.height);

        this.width = this.bounds.width;
        this.height = this.bounds.height;
    }

    /**
     * Générer le maze
     */
    public void generate() {
        addRooms();
        addCorridors();
        connectRegions();
        removeDeadEnds();
    }

    /**
     * Afficher en ascii le resultat
     */
    public void printAscii() {
        System.out.printf("\n");
        for ( int row = 0; row < bounds.height; row++ ) {
            for ( int col = 0; col < bounds.width; col++ ) {
                if ( dungeon.getCell(col, row) == 0 ) {
                    System.out.printf("#");
                } else if ( dungeon.getCell(col, row) == 2 ) {
                    System.out.printf("+");
                } else {
                    System.out.printf(" ");
                }
            }
            System.out.printf("\n");
        }
        System.out.printf("\n");

        System.out.printf("\n");
        for ( int row = 0; row < bounds.height; row++ ) {
            for ( int col = 0; col < bounds.width; col++ ) {
                if ( regions.getCell(col, row) == 0 ) {
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
     *
     * @param x
     * @param y
     * @return
     */
    public int getCellType(final int x, final int y) {
          return dungeon.getCell(x, y);
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public int getCellID(final int x, final int y) {
        return regions.getCell(x, y);
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
            // Calcul de la largeur et de la hauteur de la room
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

            // Calcul de la position de la room
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
     * Le creusement se fait par pas de 2 cells pour garantir un espace entre les corridors
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
    private void carveJunction(final int x, final int y, final int id) {
        // FIXME: c'est ici qu'on peut introduire des portes (ouvertes ou fermées), pièges, déclencheurs etc
        dungeon.setCell(x, y, 1); // une porte par exemple
        regions.setCell(x, y, id);
    }

    /**
     *
     */
    private void connectRegions() {
        HashMap<Point, HashSet> connectors = new HashMap<Point, HashSet>();
        Random rand = new Random();

        // Identifier les cells candidates à la connexion (appelée connecteur) :
        // - de type solide (attention, tous les connecteurs ont le même ID region (0))
        // - adjacente à 2 regions différentes
        // Attention, on part du principe que la region d'ID = 0 à toutes ses cells à l'état 0 (CELL_FULL)
        for ( int row = 1; row < bounds.height - 1; row ++ ) {
            for ( int col = 1; col < bounds.width - 1; col++ ) {
                if ( dungeon.getCell(col, row) == 0 ) {
                    HashSet<Integer> connectedRegions = new HashSet<Integer>();

                    for ( Direction direction : Direction.CARDINAL ) {
                        if ( dungeon.getCell(col + direction.x, row + direction.y) == 1 )
                        {
                            int id = regions.getCell(col + direction.x, row + direction.y);
                            connectedRegions.add(new Integer(id));
                        }
                    }

                    // Rappel, un connecteur ne peut relier que 2 regions à la fois !
                    if ( connectedRegions.size() == 2 ) {
                        connectors.put(new Point(col, row), connectedRegions);
                        // On change l'état de la cell pour le debug
                        //dungeon.setCell(col, row, 2);
                    }
                }
            }
        }

        // Initialisation du buffer de track des ID des regions restantes
        HashSet<Integer> remainingRegions = new HashSet<Integer>(regionID + 1);
        for ( int index = 0; index <= regionID; index++ ) {
            remainingRegions.add(new Integer(index));
        }

        // Selectionner au hasard la region qui servira de region de base pour fusionner toutes les autres
        int regionIDRef = rand.nextInt(remainingRegions.size());

        // Connecter et fusionner les regions jusqu'à ce qu'il en reste que 2, la région de référence et la région d'ID = 0
        while ( remainingRegions.size() > 2 ) {
            // Liste l'ensemble des connectors qui possèdent la region de référence
            HashMap<Point, HashSet> selectedConnectors = new HashMap<Point, HashSet>();
            Iterator entries = connectors.entrySet().iterator();
            while (entries.hasNext())
            {
                Map.Entry entry = (Map.Entry)entries.next();
                HashSet value = (HashSet)entry.getValue();

                if ( value.contains(regionIDRef) ) {
                    Point key = (Point)entry.getKey();
                    selectedConnectors.put(key, value);
                }
            }

            // Tirer au sort un connector
            List<Point> connectorPoints = new ArrayList<Point>(selectedConnectors.keySet());
            int connectorIndex = rand.nextInt(connectorPoints.size());
            Point connectorPoint = connectorPoints.get(connectorIndex);

            // Creuser la connexion
            carveJunction(connectorPoint.x, connectorPoint.y, regionIDRef);

            // Donne une chance de creuser une autre connexion
            if ( rand.nextInt(100) < extraConnectorChance ) {
                boolean done = false;

                while ( !done )
                {
                    int extraConnectorIndex = rand.nextInt(connectorPoints.size());
                    Point extraConnectorPoint = connectorPoints.get(extraConnectorIndex);
                    // On s'assure que la nouvelle connexion n'est pas acollée à celle déjà créee
                    if ( extraConnectorPoint.distance(connectorPoint) >= 2 ) {
                        carveJunction(extraConnectorPoint.x, extraConnectorPoint.y, regionIDRef);
                        done = true;
                    }
                }
            }

            // Suppression des connecteurs ayant comme regions celles du connecteur sélectionné pour creuser
            HashSet<Integer> regionsToMerge = connectors.get(connectorPoint);
            entries = connectors.entrySet().iterator();
            while (entries.hasNext())
            {
                Map.Entry entry = (Map.Entry)entries.next();
                HashSet value = (HashSet)entry.getValue();

                if ( value.containsAll(regionsToMerge) ) {
                    entries.remove();
                }
            }

            // Remap tous les connectors possedant l'id de la region fusionnée avec l'id de la region de référence
            int regionIDToDelete = 0;
            for ( Integer id : regionsToMerge ) {
                if ( id != regionIDRef ) {
                    regionIDToDelete = id;
                    break;
                }
            }
            entries = connectors.entrySet().iterator();
            while (entries.hasNext())
            {
                Map.Entry entry = (Map.Entry)entries.next();
                HashSet value = (HashSet)entry.getValue();

                if ( value.contains(regionIDToDelete) ) {
                    value.remove(regionIDToDelete);
                    value.add(regionIDRef);
                }
            }

            // Selectionner un connecteur au hasard
            /*
            List<Point> connectorPoints = new ArrayList<Point>(connectors.keySet());
            int connectorIndex = rand.nextInt(connectorPoints.size());
            Point connectorPoint = connectorPoints.get(connectorIndex);

            // Creuser la connection
            carveJunction(connectorPoint.x, connectorPoint.y);

            // Fusionner les régions
            HashSet<Integer> regionsToMerge = connectors.get(connectorPoint);

            Object[] regionsID = regionsToMerge.toArray();
            Integer regionIDToKeep = (Integer)regionsID[0];
            Integer regionIDToDelete = (Integer)regionsID[1];

            Iterator entries = connectors.entrySet().iterator();
            while (entries.hasNext())
            {
                Map.Entry entry = (Map.Entry)entries.next();
                HashSet value = (HashSet)entry.getValue();

                // 2 cas de figures :
                if ( value.containsAll(regionsToMerge) ) {
                    // Supprimer tous les connectors communs des regions fusionnées
                    entries.remove();
                } else if ( value.contains(regionIDToDelete)) {
                    // Remapper tous les connectors ayant l'id de la region supprimée par celui de la région avec laquelle elle a été fusionnée
                    value.remove(regionIDToDelete);
                    value.add(regionIDToKeep);
                }
            }
            */

            // Supprimer la région fusionnée de la liste
            remainingRegions.remove(regionIDToDelete);
        }
    }

    /**
     * Supprimer les culs de sac par "rebouchage"
     * Au moins 2 passes sont nécessaire
     */
    private void removeDeadEnds() {
        boolean done = false;

        while ( !done ) {
            done = true;

            for ( int row = 1; row < bounds.height - 1; row ++ ) {
                for ( int col = 1; col < bounds.width - 1; col++ ) {
                    // Pour chaque cell de type CELL_EMPTY
                    if ( dungeon.getCell(col, row) == 1 ) {
                        int numExits = 0;

                        // Déterminer le nombre de CELL_FULL adjacente
                        for ( Direction direction : Direction.CARDINAL ) {
                            if ( dungeon.getCell(col + direction.x, row + direction.y) == 0 )
                            {
                                numExits++;
                            }
                        }

                        // 3 cells pleine qui entourent une cell vide = deadend
                        if ( numExits == 3 )
                        {
                            done = false; // Une nouvelle passe est nécessaire

                            // On rebouche la cellule
                            // FIXME : et l'ID de regions ?
                            dungeon.setCell(col, row, 0);
                        }
                    }
                }
            }
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
        private int extraConnectorChance = 25; // %

        public MazeBuilder() {}

        public Maze build() {
            return new Maze(this);
        }

        public MazeBuilder bounds(final Dimension bounds) {
            if ( bounds.getWidth() % 2 == 0 || bounds.getHeight() % 2 == 0 ) {
                throw new IllegalArgumentException("Odd values only");
            }

            this.bounds = bounds;

            return this;
        }

        public MazeBuilder bounds(final int width, final int height) {
            if ( width % 2 == 0 || height % 2 == 0 ) {
                throw new IllegalArgumentException("Odd values only");
            }

            this.bounds.width = width;
            this.bounds.height = height;

            return this;
        }

        public MazeBuilder maxRoomSize(final int value) {
            this.maxRoomSize = value;

            return this;
        }

        public MazeBuilder minRoomSize(final int value) {
            this.minRoomSize = value;

            return this;
        }

        public MazeBuilder numRoomPositioningTries(final int value) {
            this.numRoomPositioningTries = value;

            return this;
        }

        public MazeBuilder corridorStraightness(final int value) {
            if ( value < 0 || value > 100 ) {
                throw new IllegalArgumentException("[0, 100] values range only");
            }

            this.corridorStraightness = value;

            return this;
        }

        public MazeBuilder extraConnectorChance(final int value)
        {
            if ( value < 0 || value > 100 ) {
                throw new IllegalArgumentException("[0, 100] values range only");
            }

            this.extraConnectorChance = value;

            return this;
        }
    }
}
