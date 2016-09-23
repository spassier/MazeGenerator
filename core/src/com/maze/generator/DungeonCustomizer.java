package com.maze.generator;

import java.awt.*;

/**
 * Classe de customization de la génération procédurale d'un dungeon
 *
 * @author  Sebastien PASSIER
 * Created by Sebastien PASSIER on 23/09/2016.
 */
public class DungeonCustomizer
{
    // Constantes par défaut
    private final int DEFAULT_DUNGEON_WIDTH = 31;
    private final int DEFAULT_DUNGEON_HEIGHT = 31;
    private final int DEFAULT_DUNGEON_MAXROOMSIZE = 11;
    private final int DEFAULT_DUNGEON_MINROOMSIZE = 3;
    private final int DEFAULT_DUNGEON_NUMROOMPOSITIONINGTRIES = 100;
    private final int DEFAULT_DUNGEON_CORRIDORSTRAIGHTNESS = 50;
    private final int DEFAULT_DUNGEON_EXTRACONNECTORCHANCE = 25;

    // WARNING : toutes les propriétés doivent être initialisées !!!
    private Dimension bounds = new Dimension(DEFAULT_DUNGEON_WIDTH, DEFAULT_DUNGEON_HEIGHT);
    private int maxRoomSize = DEFAULT_DUNGEON_MAXROOMSIZE;
    private int minRoomSize = DEFAULT_DUNGEON_MINROOMSIZE;
    private int numRoomPositioningTries = DEFAULT_DUNGEON_NUMROOMPOSITIONINGTRIES;
    private int corridorStraightness = DEFAULT_DUNGEON_CORRIDORSTRAIGHTNESS;
    private int extraConnectorChance = DEFAULT_DUNGEON_EXTRACONNECTORCHANCE;


    /**
     *
     * @return
     */
    public Dimension getBounds()
    {
        return bounds;
    }

    /**
     *
     * @param bounds
     */
    public void setBounds(final Dimension bounds)
    {
        if ( bounds.getWidth() % 2 == 0 || bounds.getHeight() % 2 == 0 ) {
            throw new IllegalArgumentException("bounds property requires odd values only");
        }

        this.bounds = bounds;
    }

    /**
     *
     * @return
     */
    public int getMaxRoomSize()
    {
        return maxRoomSize;
    }

    /**
     *
     * @param maxRoomSize
     */
    public void setMaxRoomSize(final int maxRoomSize)
    {
        if ( maxRoomSize < 1 ) {
            throw new IllegalArgumentException("maxRoomSize property must be greater than 0");
        }

        this.maxRoomSize = maxRoomSize;
    }

    /**
     *
     * @return
     */
    public int getMinRoomSize()
    {
        return minRoomSize;
    }

    /**
     *
     * @param minRoomSize
     */
    public void setMinRoomSize(final int minRoomSize)
    {
        if ( minRoomSize < 1 ) {
            throw new IllegalArgumentException("minRoomSize property must be greater than 0");
        }

        this.minRoomSize = minRoomSize;
    }

    /**
     *
     * @return
     */
    public int getNumRoomPositioningTries()
    {
        return numRoomPositioningTries;
    }

    /**
     *
     * @param numRoomPositioningTries
     */
    public void setNumRoomPositioningTries(final int numRoomPositioningTries)
    {
        if ( numRoomPositioningTries < 1 ) {
            throw new IllegalArgumentException("numRoomPositioningTries property must greater than 0");
        }

        this.numRoomPositioningTries = numRoomPositioningTries;
    }

    /**
     *
     * @return
     */
    public int getCorridorStraightness()
    {
        return corridorStraightness;
    }

    /**
     *
     * @param corridorStraightness
     */
    public void setCorridorStraightness(final int corridorStraightness)
    {
        if ( corridorStraightness < 0 || corridorStraightness > 100 ) {
            throw new IllegalArgumentException("corridorStraightness property requires percentage value : [0, 100]");
        }

        this.corridorStraightness = corridorStraightness;
    }

    /**
     *
     * @return
     */
    public int getExtraConnectorChance()
    {
        return extraConnectorChance;
    }

    /**
     *
     * @param extraConnectorChance
     */
    public void setExtraConnectorChance(final int extraConnectorChance)
    {
        if ( extraConnectorChance < 0 || extraConnectorChance > 100 ) {
            throw new IllegalArgumentException("extraConnectorChance property requires percentage value : [0, 100]");
        }

        this.extraConnectorChance = extraConnectorChance;
    }
}
