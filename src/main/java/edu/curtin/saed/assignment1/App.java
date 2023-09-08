package edu.curtin.saed.assignment1;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;
import javax.swing.*;

public class App 
{
    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static BlockingQueue<Map<String, XandYObject>> blockingQueue = new LinkedBlockingQueue<>();
    private static ExecutorService executorService;
    private static SwingArena arena;
    private static int robotNumber = 1;
    private BlockingQueue<SwingArena> queue = new ArrayBlockingQueue<>(81);

    public static void main(String[] args) 
    {
        executorService = Executors.newFixedThreadPool(81);

        // Note: SwingUtilities.invokeLater() is equivalent to JavaFX's Platform.runLater().
        SwingUtilities.invokeLater(() ->
        {
            JFrame window = new JFrame("Example App (Swing)");
            arena = new SwingArena();

            CompletableFuture.runAsync(App::RobotsAppearing, executorService);
            System.out.println("Main thread " + executorService);

//            CompletableFuture.runAsync(App::MoveAttempt, executorService);
//            CompletableFuture.runAsync(App::methodThree, executorService);
//            CompletableFuture.runAsync(App::methodFour, executorService);

            arena.addListener((x, y) ->
            {
                System.out.println("Arena click at (" + x + "," + y + ")");
            });
            
            JToolBar toolbar = new JToolBar();
//             JButton btn1 = new JButton("My Button 1");
//             JButton btn2 = new JButton("My Button 2");
            JLabel label = new JLabel("Score: 999");
//             toolbar.add(btn1);
//             toolbar.add(btn2);
            toolbar.add(label);
            
//             btn1.addActionListener((event) ->
//             {
//                 System.out.println("Button 1 pressed");
//             });
            
            JTextArea logger = new JTextArea();
            JScrollPane loggerArea = new JScrollPane(logger);
            loggerArea.setBorder(BorderFactory.createEtchedBorder());
            logger.append("Hello\n");
            logger.append("World\n");
            
            JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, arena, logger);

            Container contentPane = window.getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.add(toolbar, BorderLayout.NORTH);
            contentPane.add(splitPane, BorderLayout.CENTER);
            
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setPreferredSize(new Dimension(800, 800));
            window.pack();
            window.setVisible(true);
            
            splitPane.setDividerLocation(0.75);
        });
    }
    private static void RobotsAppearing(){
        int corner;
        int x = 0, y = 0;
        final int DELAYMIN = 1;
        final int DELAYMAX = 4;


        while(true){
            corner = (int)(Math.random()*(DELAYMAX-DELAYMIN+1)+DELAYMIN);

            switch (corner) {
                case 1 -> {
                    x = 0;
                    y = 0;
                }
                case 2 -> {
                    x = 0;
                    y = 8;
                }
                case 3 -> {
                    x = 8;
                    y = 0;
                }
                case 4 -> {
                    x = 8;
                    y = 8;
                }
            }
            setRobotXandY(x, y);
        }
    }
    private static void setRobotXandY(int x, int y){
        Map<String, XandYObject> robotsMap;
        Boolean occupied = false;
        int X, Y;
        int delay;
        final int DELAYMIN = 500;
        final int DELAYMAX = 2000;

        robotsMap = arena.getRobotsMap();
        delay = (int)(Math.random()*(DELAYMAX-DELAYMIN+1)+DELAYMIN);

        if(robotNumber == 1){

            arena.setRobotPosition(""+robotNumber, new XandYObject(x, y, delay), 0);
            CompletableFuture.runAsync(() -> TowardsTheCitadel(""+robotNumber, new XandYObject(x, y, delay)), executorService);

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            robotNumber++;
        }
        else{
            for(XandYObject xandYObject :  robotsMap.values()){
                X = xandYObject.getNewX();
                Y = xandYObject.getNewY();

                if(x == X && y == Y){
                    occupied = true;
                }
            }
            if(occupied){
                // do nothing
            }
            else{
                arena.setRobotPosition(""+robotNumber, new XandYObject(x, y, delay), 0);
                CompletableFuture.runAsync(() -> TowardsTheCitadel(""+robotNumber, new XandYObject(x, y, delay)), executorService);

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                robotNumber++;

            }
        }


    }
    private static void TowardsTheCitadel(String robotName, XandYObject xandYObject){
        int direction;
        int delay;
        int x, y =0;
        final int DELAYMAX = 2;
        final int DELAYMIN = 1;
        Boolean freeToMove;


        delay = xandYObject.getDelay();
        x = xandYObject.getNewX();
        y = xandYObject.getNewY();

        while(true){

            direction = (int) (Math.random() * (DELAYMAX - DELAYMIN + 1) + DELAYMIN);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if(x < 4 && y < 4){ // Upper left corner
                switch (direction) {
                    case 1 -> { // Moves right
                        if (x < 8) {
                            x++;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                x--;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    case 2 -> { // Moves down
                        if (y < 8) {
                            y++;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                y--;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                }
            }else if(x >= 4 && y < 4){ // Upper right corner
                switch (direction) {
                    case 1 -> { // Moves left
                        if (x > 0) {
                            x--;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                x++;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    case 2 -> { // Moves down
                        if (y < 8) {
                            y++;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                y--;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                }
            }else if(x < 4 && y >= 4) { // Bottom left corner
                switch (direction) {
                    case 1 -> { // Moves up
                        if (y > 0) {
                            y--;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                y++;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    case 2 -> { // Moves right
                        if (x < 8) {
                            x++;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                x--;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                }
            }else if(x >= 4 && y >= 4){ // Bottom right corner
                switch (direction) {
                    case 1 -> { // Moves up
                        if (y > 0) {
                            y--;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                y++;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                    case 2 -> { // Moves left
                        if (x > 0) {
                            x--;
                            freeToMove = FreeToMove(robotName, xandYObject, x, y);
                            if (!freeToMove) {
                                x++;
                                RandomMovingAttempt(robotName, xandYObject);
                            }
                        }
                    }
                }
            }
        }
    }
    private static void RandomMovingAttempt(String robotName, XandYObject xandYObject){
        int direction;
        int delay;
        int x, y;
        Boolean freeToMove;
        final int DELAYMAX = 4;
        final int DELAYMIN = 1;

        delay = xandYObject.getDelay();
        x = xandYObject.getNewX();
        y = xandYObject.getNewY();

        while(true){

            direction = (int) (Math.random() * (DELAYMAX - DELAYMIN + 1) + DELAYMIN);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


            switch (direction) {
                case 1 -> { // Moves left
                    if (x > 0) {
                        x--;
                        freeToMove = FreeToMove(robotName, xandYObject, x, y);
                        if (!freeToMove) {
                            x++;
                        }
                    }
                }
                case 2 -> { // Moves right
                    if (x < 8) {
                        x++;
                        freeToMove = FreeToMove(robotName, xandYObject, x, y);
                        if (!freeToMove) {
                            x--;
                        }
                    }
                }
                case 3 -> { // Moves up
                    if (y > 0) {
                        y--;
                        freeToMove = FreeToMove(robotName, xandYObject, x, y);
                        if (!freeToMove) {
                            y++;
                        }
                    }
                }
                case 4 -> { // Moves down
                    if (y < 8) {
                        y++;
                        freeToMove = FreeToMove(robotName, xandYObject, x, y);
                        if (!freeToMove) {
                            y--;
                        }
                    }
                }
            }

        }
    }
    private static Boolean FreeToMove(String robotName, XandYObject xandYObject, int newX, int newY){
        Map<String, XandYObject> everyRobots;
        int X, Y;
        Boolean freeToMove = true;

        everyRobots = arena.getRobotsMap();

        for(XandYObject o : everyRobots.values()){
            X = o.getNewX();
            Y = o.getNewY();

            if(newX == X && newY == Y){
                freeToMove = false;
            }
        }

        if(freeToMove){
//            if(robotName.equals("3")){
//                System.out.printf("robot %s, newX %s, newY %s%n",robotName, newX, newY);
//            }
                xandYObject.setOldX(xandYObject.getNewX());
                xandYObject.setOldY(xandYObject.getNewY());
                xandYObject.setNewX(newX);
                xandYObject.setNewY(newY);
                arena.setRobotPosition(robotName, xandYObject);
        }

        return freeToMove;
    }

}
