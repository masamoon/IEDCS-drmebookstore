package com.iedcs.player;

import nl.siegmann.epublib.viewer.Viewer;

import java.io.FileInputStream;

/**
 * Created by Andre on 09-11-2015.
 */
public class GUI {
    public static void view(FileInputStream decinput){

        Viewer viewer = new Viewer(decinput);
    }
}
