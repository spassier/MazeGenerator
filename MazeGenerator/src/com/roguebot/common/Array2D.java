package com.roguebot.common;

/**
 * Created by Sebastien PASSIER on 27/02/2016.
 */
public class Array2D
{
    private int[][] data;
    private int width;
    private int height;

    /**
     * Constructeur
     * @param width
     * @param height
     */
    public Array2D(int width, int height) /* throws IllegalArgumentException */ {
        /*
        if ( width < 0 ) {
            throw new IllegalArgumentException("Illegal negative width : " + width);
        } else if ( height < 0 ) {
            throw new IllegalArgumentException("Illegal negative height : " + height);
        }
        */

        this.width = width;
        this.height = height;

        data = new int[width][height];
        this.fill(0);
    }

    /**
     *
     * @return
     */
    public int getArea() {
        return width * height;
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    public int getCell(int row, int col) {
        return data[row][col];
    }

    /**
     *
     * @param row
     * @param col
     * @param value
     */
    public void setCell(int row, int col, int value) {
        data[row][col] = value;
    }

    /**
     *
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     *
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     *
     * @param value
     */
    private void fill(int value) {
        for ( int row = 0; row < width; row++ ) {
            for ( int col = 0; col < height; col++ ) {
                data[row][col] = value;
            }
        }
    }
}
