/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import catchthief.CityMap;
import gps.GPSMonitor;
import informationCentral.InformationCentralMonitor;
import java.awt.Color;
import java.awt.Point;
import static java.lang.System.out;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import pt.ua.gboard.GBoard;
import pt.ua.gboard.ImageGelem;
import pt.ua.gboard.games.Labyrinth;

/**
 *
 * @author joelpinheiro
 */
public class Thief extends Thread {

    static public int pause = 100;
    private final Point[] startPositions;
    private final Map markedPositions;
    private final Color color;
    private static Labyrinth maze;
    static char prisonSymbol;
    static char prisonStartSymbol;
    static char hindingPlaceSymbol;
    static char passerbyHouseSymbol;
    static char objectToStealSymbol;
    static char actualPositionSymbol;
    static InformationCentralMonitor informationCentralMonitor;

    /**
     * 
     * @param informationCentralMonitor
     * @param startPositions
     * @param markedPositions
     * @param extraSymbols
     * @param color 
     */
    public Thief(InformationCentralMonitor informationCentralMonitor, Point[] startPositions, Map markedPositions, char[] extraSymbols, Color color) {
        this.startPositions = startPositions;
        this.markedPositions = markedPositions;
        this.color = color;
        Thief.maze = CityMap.getMaze();

        prisonSymbol = extraSymbols[0];
        hindingPlaceSymbol = extraSymbols[1];
        passerbyHouseSymbol = extraSymbols[2];
        objectToStealSymbol = extraSymbols[3];
        actualPositionSymbol = extraSymbols[4];
        this.informationCentralMonitor = informationCentralMonitor;
    }

    @Override
    public void run() {
        if (!searchPath(0, startPositions[0].y, startPositions[0].x, markedPositions, color)) {
            out.println("Thief stopped!");
        }
    }

    /**
     * randomWalking
     * @param lin
     * @param col
     * @param markedPositions
     * @param color
     * @return 
     */
    public static boolean randomWalking(int lin, int col, Map markedPositions, Color color) {


        boolean result = false;

        if (maze.validPosition(lin, col) && maze.isRoad(lin, col) && !markedPositions.containsKey(String.valueOf(lin) + "_" + String.valueOf(col))) {

            markPosition(lin, col, color);
            markedPositions.put(String.valueOf(lin) + "_" + String.valueOf(col), markedPositions.size());
            unmarkPosition(lin, col, null);

            if (randomWalking(lin - 1, col, markedPositions, color)) // North
            {
                result = true;
            } else if (randomWalking(lin, col + 1, markedPositions, color)) // East
            {
                result = true;
            } else if (randomWalking(lin, col - 1, markedPositions, color)) // West
            {
                result = true;
            } else if (randomWalking(lin + 1, col, markedPositions, color)) // South
            {
                result = true;
            } else {
                markPosition(lin, col, color);
                markedPositions.put(String.valueOf(lin) + "_" + String.valueOf(col), markedPositions.size());
                unmarkPosition(lin, col, null);
            }

            GBoard.sleep(1);
        }
        return result;
    }
    
    /**
     * goToPrison
     * @param positions
     * @param color
     * @return 
     */
    public static boolean goToPrison(Map positions, Color color) {
        Collection c = positions.keySet();
        Iterator itr = c.iterator();

        String[] ses = new String[positions.size()];
        int cont = positions.size() - 1;

        String[] tmp = new String[positions.size()];

        while (itr.hasNext()) {
            String g = (String) itr.next();
            int id = (int) positions.get(g);
            tmp[id] = g;
        }

//        for (int i = tmp.length - 1; i >= 0; i--) {
        for (String se : tmp) {
            int x = se.indexOf('_'); 
            // get line and col from positions
            moveToPosition(Integer.parseInt(se.substring(0, x)), Integer.parseInt(se.substring(x + 1, se.length())), color);
        }
        return true;
    }

    /**
     * goToPosition
     * @param positions
     * @param color
     * @return 
     */
    public static boolean goToPosition(Map positions, Color color) {
        Collection c = positions.keySet();
        Iterator itr = c.iterator();

        String[] ses = new String[positions.size()];
        int cont = positions.size() - 1;

        String[] tmp = new String[positions.size()];

        while (itr.hasNext()) {
            String g = (String) itr.next();
            int id = (int) positions.get(g);
            tmp[id] = g;
        }

        for (int i = tmp.length - 1; i >= 0; i--) {
            String se = tmp[i];
            int x = se.indexOf('_'); 
            // get line and col from positions
            moveToPosition(Integer.parseInt(se.substring(0, x)), Integer.parseInt(se.substring(x + 1, se.length())), color);
        }
        return true;
    }

    /**
     * moveToPosition
     * @param lin
     * @param col
     * @param color
     * @return 
     */
    public static boolean moveToPosition(int lin, int col, Color color) {
        boolean result = false;

        markPosition(lin, col, color);

        GBoard.sleep(pause);

        if (!isSymbolPosition(lin, col)) {
            informationCentralMonitor.setActualThiefPosition(lin, col);
            maze.board.draw(new ImageGelem("/Users/joelpinheiro/Documents/GitHub/CatchThief/src/threads/thief.png", maze.board, 100), lin, col, 1);
        }

        unmarkPosition(lin, col, null);

        return result;
    }

    /**
     * Backtracking path search algorithm
     */
    public static boolean searchPath(int distance, int lin, int col, Map markedPositions, Color color) {

        if(informationCentralMonitor.thiefInPrison())
            return false;
        
        if(informationCentralMonitor.copFoundThief()){
                
            Point begin = new Point(lin, col);

            Point[] endPosition = maze.roadSymbolPositions(prisonSymbol);

            Map gpsPositions = new TreeMap<>();
            GPSMonitor gpsMonitor = new GPSMonitor(maze, CityMap.getExtraSymbols());        
            gpsPositions = GPSMonitor.getGPSPositions(endPosition[0], begin);

            goToPrison(gpsPositions, Color.BLACK);  
            
            informationCentralMonitor.setThiefInPrison(true);
            
            out.println("Thief arrived prison");
            
            return false;
        }
        
        
        
        boolean result = false;

        if (maze.validPosition(lin, col) && maze.isRoad(lin, col)) {
            if (maze.roadSymbol(lin, col) == objectToStealSymbol) {
                
                maze.board.erase(lin, col, 1, 1);

                unmarkPosition(lin, col, markedPositions);

                out.println("Destination found at " + distance + " steps from start position.");
                out.println();
                result = true;

                goToPosition(markedPositions, Color.black);

                System.out.println(entriesSortedByValues(markedPositions));
                

            } else if (freePosition(lin, col, markedPositions)) {
                markPosition(lin, col, color);

                markedPositions.put(String.valueOf(lin) + "_" + String.valueOf(col), markedPositions.size());
                unmarkPosition(lin, col, markedPositions);

                if (searchPath(distance + 1, lin - 1, col, markedPositions, color)) // North
                {
                    result = true;
                } else if (searchPath(distance + 1, lin, col + 1, markedPositions, color)) // East
                {
                    result = true;
                } else if (searchPath(distance + 1, lin, col - 1, markedPositions, color)) // West
                {
                    result = true;
                } else if (searchPath(distance + 1, lin + 1, col, markedPositions, color)) // South
                {
                    result = true;
                } else {
                    if(informationCentralMonitor.thiefInPrison())
                        return false;
                    markPosition(lin, col, color);
                    markedPositions.put(String.valueOf(lin) + "_" + String.valueOf(col), markedPositions.size());
                    unmarkPosition(lin, col, markedPositions);
                }

                GBoard.sleep(1);
                clearPosition(lin, col, markedPositions);
            }
        }

        return result;
    }
    
    /**
     * isSymbolPosition
     * @param lin
     * @param col
     * @return 
     */
    static boolean isSymbolPosition(int lin, int col) {
        assert maze.isRoad(lin, col);

        return maze.roadSymbol(lin, col) == objectToStealSymbol ||
               maze.roadSymbol(lin, col) == hindingPlaceSymbol || 
               maze.roadSymbol(lin, col) == prisonSymbol ||
               maze.roadSymbol(lin, col) == passerbyHouseSymbol;
    }

    /**
     * isObjectToStealPosition
     * @param lin
     * @param col
     * @return 
     */
    static boolean isObjectToStealPosition(int lin, int col) {
        assert maze.isRoad(lin, col);

        return maze.roadSymbol(lin, col) == objectToStealSymbol;
    }
    
    /**
     * freePosition
     * @param lin
     * @param col
     * @param markedPositions
     * @return 
     */
    static boolean freePosition(int lin, int col, Map markedPositions) {
        assert maze.isRoad(lin, col);

        if (markedPositions.containsKey(String.valueOf(lin) + "_" + String.valueOf(col))) {
            return false;
        }

        return maze.roadSymbol(lin, col) == ' '
                || isSymbolPosition(lin, col);
    }

    /**
     * markPosition
     * @param lin
     * @param col
     * @param color 
     */
    static void markPosition(int lin, int col, Color color) {
        assert maze.isRoad(lin, col);

        if (!isSymbolPosition(lin, col)) //maze.putRoadSymbol(lin, col, markedStartSymbol);
        {
            informationCentralMonitor.setActualThiefPosition(lin, col);
            maze.board.draw(new ImageGelem("/Users/joelpinheiro/Documents/GitHub/CatchThief/src/threads/thief.png", maze.board, 100), lin, col, 1);       
        }

        GBoard.sleep(pause);
    }

    /**
     * clearPosition
     * @param lin
     * @param col
     * @param markedPositions 
     */
    static void clearPosition(int lin, int col, Map markedPositions) {
        assert maze.isRoad(lin, col);

        markedPositions.remove(String.valueOf(lin) + "_" + String.valueOf(col));

        if (!isSymbolPosition(lin, col) || isObjectToStealPosition(lin, col)) {
            maze.board.erase(lin, col, 1, 1);
        }
        //GBoard.sleep(pause);
    }

    /**
     * unmarkPosition
     * @param lin
     * @param col
     * @param markedPositions 
     */
    static void unmarkPosition(int lin, int col, Map markedPositions) {
        assert maze.isRoad(lin, col);

        if (!isSymbolPosition(lin, col)) {
            maze.board.erase(lin, col, 1, 1);
        }
        //GBoard.sleep(pause);
    }

    /**
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    static <K, V extends Comparable<? super V>>
            SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                int res = e1.getValue().compareTo(e2.getValue());
                return res != 0 ? res : 1;
            }
        }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}
