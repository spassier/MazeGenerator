package com.maze.generator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.maze.common.Array2D;
import com.maze.common.Direction;

import java.awt.*;
import java.util.*;
import java.util.List;


/**
 * Created by Sebastien PASSIER on 27/02/2016.
 */
public class Dungeon
{
    private DungeonCustomizer customizer;

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


    private Dungeon(DungeonBuilder builder) {
        this.customizer = builder.customizer;

        this.bounds = builder.customizer.getBounds();//builder.bounds;
        this.maxRoomSize = builder.customizer.getMaxRoomSize();
        this.minRoomSize = builder.customizer.getMinRoomSize();
        this.numRoomPositioningTries = builder.customizer.getNumRoomPositioningTries();
        this.corridorStraightness = builder.customizer.getCorridorStraightness();
        this.extraConnectorChance = builder.customizer.getExtraConnectorChance();

        this.rooms = new ArrayList<Rectangle>();
        this.regions = new Array2D(this.bounds.width, this.bounds.height); // TODO : A flush car ne sert que pour la construction du dungeon ou a placer en local d'une methode
        this.dungeon = new Array2D(this.bounds.width, this.bounds.height);

        this.width =  customizer.getBounds().width;
        this.height = customizer.getBounds().height;
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
     * Pour des raisons pratiques les corridors sont crées uniquement dans les lignes et les colonnes impaires
     */
    private void addCorridors() {
        for ( int row = 1; row < customizer.getBounds().height; row += 2 ) {
            for ( int col = 1; col < customizer.getBounds().width; col += 2 ) {
                if ( dungeon.getCell(col, row) == 0 ) {
                    carveCorridor(col, row);
                }
            }
        }
    }

    /**
     * Ajouter des rooms dont les dimensions sont aléatoires et disjointes les unes des autres
     * L'objectif est double:
     *      1 - Garantir que les tailles des rooms est impaire (width et height)
     *      2 - Aligner les positions des rooms uniquement sur des position impaires (row et col)
     */
    private void addRooms() {
        Random rand = new Random();

        for ( int iteration = 0; iteration < numRoomPositioningTries; iteration++ ) {
            // Calcul de la largeur et de la hauteur de la room
            int roomWidth = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize;
            int roomHeight = rand.nextInt(maxRoomSize - minRoomSize + 1) + minRoomSize;

            // Pour ajouter de l'aléatoire dans le redimentionnement de la largeur ou de la longueure lorsque la valeur est paire
            int lessOrMore;
            if ( rand.nextInt(1) == 0 ) {
                lessOrMore = 1;
            } else {
                lessOrMore = -1;
            }

            // Traitement des cas aux limites du redimentionnement
            if ( (roomWidth & 1) == 0 ) {
                if ( (roomWidth + lessOrMore) <= maxRoomSize ) {
                    roomWidth++;
                } else {
                    roomWidth--;
                }
            }

            if ( (roomHeight & 1) == 0 ) {
                if ( (roomHeight + lessOrMore) <= maxRoomSize ) {
                    roomHeight++;
                } else {
                    roomHeight--;
                }
            }

            // Calcul de la position de la room
            int x = rand.nextInt((width - roomWidth) / 2) * 2 + 1;
            int y = rand.nextInt((height - roomHeight) / 2) * 2 + 1;

            Rectangle room = new Rectangle(x, y, roomWidth, roomHeight);

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
                carveRegion(room);
                /*
                newRegion();
                for (int row = room.y; row < room.y + room.height; row++)
                {
                    for (int col = room.x; col < room.x + room.width; col++)
                    {
                        regions.setCell(col, row, regionID);
                        dungeon.setCell(col, row, 1);
                    }
                }
                */
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
     * Creuser une region
     * @param room Rectangle définissant une room
     */
    private void carveRegion(final Rectangle room) {
        newRegion();

        for ( int row = room.y; row < room.y + room.height; row++ )
        {
            for ( int col = room.x; col < room.x + room.width; col++ )
            {
                regions.setCell(col, row, regionID);
                dungeon.setCell(col, row, 1);
            }
        }
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

            // Liste les ID des 2 régions à fusionner
            HashSet<Integer> regionsToMerge = connectors.get(connectorPoint);

            // Creuser la connexion
            carveJunction(connectorPoint.x, connectorPoint.y, regionIDRef);

            // Donne une chance de creuser une autre connexion POUR LE MËME COUPLE DE REGION
            if ( rand.nextInt(100) < extraConnectorChance ) {
                boolean done = false;

                while ( !done )
                {
                    int maxTries = 32;
                    Point extraConnectorPoint;

                    // La sélection du connector est faite aléatoirement et pour ne pas être bloqué dans une boucle infinie le nombre de tentatives est limité
                    while ( maxTries > 0 ) {
                        int extraConnectorIndex = rand.nextInt(connectorPoints.size());
                        extraConnectorPoint = connectorPoints.get(extraConnectorIndex);

                        // La technique est un peu lourde car on creuse une connexion pour le même couple de region à fusionner alors que la région peut avoir des connectors avec d'autres régions.
                        // Si on ne fait pas comme ça il est possible que les passes suivantes creusent un connector adjacent...
                        if ( selectedConnectors.get(extraConnectorPoint).containsAll(regionsToMerge) ) {
                            // On s'assure que la nouvelle connexion n'est pas acollée à celle déjà créee
                            if ( Math.abs(extraConnectorPoint.x - connectorPoint.x) >= 2 || Math.abs(extraConnectorPoint.y - connectorPoint.y) >= 2 ) {
                                carveJunction(extraConnectorPoint.x, extraConnectorPoint.y, regionIDRef);
                                done = true;
                            }
                            break;
                        }
                        maxTries--;
                    }
                }
            }

            // Suppression des connecteurs ayant comme regions celles du connecteur sélectionné pour creuser
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
    public static class DungeonBuilder
    {
        private DungeonCustomizer customizer;


        public DungeonBuilder() {
            customizer = new DungeonCustomizer();

            if ( Gdx.files.internal("dungeon.cfg").exists() )
            {
                FileHandle file = Gdx.files.internal("dungeon.cfg");
                JsonReader reader = new JsonReader(file.reader());
                customizer = new Gson().fromJson(reader, DungeonCustomizer.class);
            }
        }

        public Dungeon build() {
            return new Dungeon(this);
        }

        public DungeonBuilder bounds(final Dimension bounds) {
            this.customizer.setBounds(bounds);

            return this;
        }

        public DungeonBuilder maxRoomSize(final int value) {
            this.customizer.setMaxRoomSize(value);

            return this;
        }

        public DungeonBuilder minRoomSize(final int value) {
            this.customizer.setMinRoomSize(value);

            return this;
        }

        public DungeonBuilder numRoomPositioningTries(final int value) {
            this.customizer.setNumRoomPositioningTries(value);

            return this;
        }

        public DungeonBuilder corridorStraightness(final int value) {
            this.customizer.setCorridorStraightness(value);

            return this;
        }

        public DungeonBuilder extraConnectorChance(final int value)
        {
            this.customizer.setExtraConnectorChance(value);

            return this;
        }
    }
}
