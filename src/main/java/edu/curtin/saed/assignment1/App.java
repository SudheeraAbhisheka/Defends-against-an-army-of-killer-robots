package edu.curtin.saed.assignment1;

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

    public static void main(String[] args) 
    {
        executorService = Executors.newFixedThreadPool(81);

        // Note: SwingUtilities.invokeLater() is equivalent to JavaFX's Platform.runLater().
        SwingUtilities.invokeLater(() ->
        {
            JFrame window = new JFrame("Example App (Swing)");
            arena = new SwingArena();

            CompletableFuture.runAsync(App::RobotsAppearing, executorService);
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
        int robotNumber = 1;


        while(true){
            corner = (int)(Math.random()*(4-1+1)+1);


            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            switch (corner){
                case 1: {
                    x = 0;
                    y = 0;
                }break;

                case 2:{
                    x = 0;
                    y = 8;
                }break;

                case 3:{
                    x = 8;
                    y = 0;
                }break;

                case 4:{
                    x = 8;
                    y = 8;
                }break;
            }
            setRobotXandY(x, y);
        }
    }
    private static void setRobotXandY(int x, int y){
        Map<String, XandYObject> robotsMap;
        Boolean occupied = false;
        int X, Y;
        int delay;

        robotsMap = arena.getRobotsMap();

        if(robotsMap.isEmpty()){

            delay = (int)(Math.random()*(2000-500+1)+500);
            arena.setRobotPosition(""+robotNumber, new XandYObject(x, y, delay));
            CompletableFuture.runAsync(() -> MovingAttemptThread(""+robotNumber, new XandYObject(x, y, delay)), executorService);
            System.out.println((executorService));
            robotNumber++;

        }
        else{
            for(XandYObject xandYObject :  robotsMap.values()){
                X = xandYObject.getX();
                Y = xandYObject.getY();

                if(x == X && y == Y){
                    occupied = true;
                }
            }
            if(occupied){
                // do nothing
            }
            else{
                delay = (int)(Math.random()*(2000-500+1)+500);
                arena.setRobotPosition(""+robotNumber, new XandYObject(x, y, delay));
                CompletableFuture.runAsync(() -> MovingAttemptThread(""+robotNumber, new XandYObject(x, y, delay)), executorService);
                robotNumber++;
                System.out.println((executorService));
            }
        }


    }
    private static void MovingAttemptThread(String robotName, XandYObject xandYObject){
        int direction;
        int delay = 0;
        int x = 0, y = 0;

        delay = xandYObject.getDelay();
        x = xandYObject.getX();
        y = xandYObject.getY();

        while(true){
            direction = (int) (Math.random() * (4 - 1 + 1) + 1);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            switch (direction) {
                case 1: { // Moves left
                    if(x > 0){
                        x--;
                        FreeToMove(robotName, xandYObject, x, y);
                    }
                }
                break;

                case 2: { // Moves right
                    if(x < 8){
                        x++;
                        FreeToMove(robotName, xandYObject, x, y);
                    }
                }
                break;

                case 3: { // Moves up
                    if(y < 8){
                        y++;
                        FreeToMove(robotName, xandYObject, x, y);
                    }
                }
                break;

                case 4: { // Moves down
                    if(y > 0){
                        y--;
                        FreeToMove(robotName, xandYObject, x, y);
                    }
                }
                break;
            }

        }
    }
    private static void FreeToMove(String robotName, XandYObject xandYObjectOld, int newX, int newY){
        Map<String, XandYObject> everyRobots;
        int X, Y;
        Boolean freeToMove = true;

        everyRobots = arena.getRobotsMap();

        for(XandYObject xandYObject : everyRobots.values()){
            X = xandYObject.getX();
            Y = xandYObject.getY();

            if(newX == X && newY == Y){
                freeToMove = false;
            }
        }

        if(freeToMove){
            xandYObjectOld.setX(newX);
            xandYObjectOld.setY(newY);
            arena.setRobotPosition(robotName, xandYObjectOld);
        }

    }

}
