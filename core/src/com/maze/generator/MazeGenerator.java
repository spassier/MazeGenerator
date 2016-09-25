package com.maze.generator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class MazeGenerator extends ApplicationAdapter {
	private final int CELL_SIZE = 16;
	private Dungeon maze;
	private OrthographicCamera camera;

	SpriteBatch batch;
	Texture img;

	Sprite cellEmpty;
	Sprite cellFull;
	Sprite cellConnector;

	@Override
	public void create () {
		initMaze();

		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		// Constructs a new OrthographicCamera, using the given viewport width and height
		// Height is multiplied by aspect ratio.
		camera = new OrthographicCamera(1024 - 32 , (1024 - 32) * (h / w));
		camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Inversion de l'axe y
		// FIXME : If you use TextureRegions and/or a TextureAtlas, all you need to do in addition to that is call region.flip(false, true).
		camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
		camera.update();

		batch = new SpriteBatch();
		//img = new Texture("badlogic.jpg");

		cellEmpty = new Sprite(new Texture("Tile02.png"));
		cellEmpty.setSize(CELL_SIZE , CELL_SIZE);
		cellFull = new Sprite(new Texture("Tile01.png"));
		cellFull.setSize(CELL_SIZE, CELL_SIZE);
		cellConnector = new Sprite(new Texture("Tile03.png"));
		cellConnector.setSize(CELL_SIZE, CELL_SIZE);
	}

	@Override
	public void render () {
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		//batch.draw(img, 0, 0);
		for( int y = 0; y < maze.height; y++ ) {
			for( int x = 0; x < maze.width; x++ ) {
				drawCell(maze.getCellType(x, y), x, y);
			}
		}
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		//img.dispose();
	}

	void initMaze() {
		this.maze = new Dungeon.DungeonBuilder().build();//new Dungeon.MazeBuilder().bounds(new Dimension(31,31)).build();
		maze.generate();
		maze.printAscii();
	}

	void drawCell(final int state, final int x, final int y) {
		switch (state) {
			case 0 :
				cellFull.setX(x * CELL_SIZE);
				cellFull.setY(y * CELL_SIZE);
				cellFull.draw(batch);
				break;
			case 1 :
				cellEmpty.setX(x * CELL_SIZE);
				cellEmpty.setY(y * CELL_SIZE);
				cellEmpty.draw(batch);
				break;
			case 2 :
				cellConnector.setX(x * CELL_SIZE);
				cellConnector.setY(y * CELL_SIZE);
				cellConnector.draw(batch);
				break;
		}
	}

	com.badlogic.gdx.graphics.Color generateColor(int index) {
		int max = 32;
		float s = 0.99f;
		float v = 0.99f;
		float h = 1.0f / (float)(max + 1) * index;

		com.badlogic.gdx.graphics.Color color = hsv2rgb(h, s, v);

		return color;
	}

	com.badlogic.gdx.graphics.Color hsv2rgb(float h, float s, float v) {
		// Adapted from http://www.easyrgb.com/math.html
		// hsv values = 0 - 1, rgb values = 0 - 255
		float r, g, b;
		com.badlogic.gdx.graphics.Color RGB;

		if (s == 0.0f) {
			RGB = new com.badlogic.gdx.graphics.Color(Math.round(v * 255.0f), Math.round(v * 255.0f),Math.round(v * 255.0f), 0.2f);
		} else {
			// h must be < 1
			float var_h = h * 6.0f;
			if (var_h == 6.0f)
				var_h = 0.0f;
			//Or ... var_i = floor( var_h )
			float var_i = (float)Math.floor(var_h);
			float var_1 = v * (1.0f - s);
			float var_2 = v * (1.0f - s * (var_h - var_i));
			float var_3 = v * (1.0f - s * (1.0f - (var_h - var_i)));
			float var_r;
			float var_g;
			float var_b;
			if (var_i == 0.0f) {
				var_r = v;
				var_g = var_3;
				var_b = var_1;
			} else if (var_i == 1.0f) {
				var_r = var_2;
				var_g = v;
				var_b = var_1;
			} else if (var_i == 2.0f) {
				var_r = var_1;
				var_g = v;
				var_b = var_3;
			} else if (var_i == 3.0f) {
				var_r = var_1;
				var_g = var_2;
				var_b = v;
			} else if (var_i == 4.0f) {
				var_r = var_3;
				var_g = var_1;
				var_b = v;
			} else {
				var_r = v;
				var_g = var_1;
				var_b = var_2;
			}
			//rgb results = 0 ÷ 255
			RGB = new com.badlogic.gdx.graphics.Color(var_r, var_g, var_b, 0.2f);
		}

		return RGB;
	}
}
