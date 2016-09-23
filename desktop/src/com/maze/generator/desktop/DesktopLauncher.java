package com.maze.generator.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.maze.generator.MazeGenerator;

import java.util.Date;


public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Maze Generator v0.2 " + new Date().toString();
		config.width = 1024 - 32;
		config.height = 1024 - 32;
		new LwjglApplication(new MazeGenerator(), config);

	}

}
