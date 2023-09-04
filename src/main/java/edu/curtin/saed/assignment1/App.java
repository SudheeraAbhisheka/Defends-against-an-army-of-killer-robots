package edu.curtin.saed.assignment1;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;
import javax.swing.*;

public class App 
{
    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static BlockingQueue<Map<String, XandYObject>> blockingQueue = new LinkedBlockingQueue<>();
    private static ExecutorService executorService;

    public static void main(String[] args) 
    {
        executorService = Executors.newFixedThreadPool(10);

        // Note: SwingUtilities.invokeLater() is equivalent to JavaFX's Platform.runLater().
        SwingUtilities.invokeLater(() ->
        {
            JFrame window = new JFrame("Example App (Swing)");
            SwingArena arena = new SwingArena();

            CompletableFuture.runAsync(() -> methodOne(arena), executorService);

            CompletableFuture.runAsync(() -> methodTwo(arena), executorService);
            CompletableFuture.runAsync(() -> methodThree(arena), executorService);
            CompletableFuture.runAsync(() -> methodFour(arena), executorService);
            CompletableFuture.runAsync(() -> methodFive(arena), executorService);


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
    public static void methodOne(SwingArena arena){
        Map<String, XandYObject> mapOne = new HashMap<>();

        for(int i = 0; i < 9; i++){
            mapOne.put(""+i, new XandYObject(i, i));
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        blockingQueue.add(mapOne);

//        arena.setRobotPosition(blockingQueue);
    }

    private static void methodTwo(SwingArena arena){
        Map<String, XandYObject> mapTwo = new HashMap<>();

        for(int i = 0; i < 9; i++){
            mapTwo.put(""+i, new XandYObject(8-i, i));
        }


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        blockingQueue.add(mapTwo);

//        arena.setRobotPosition(blockingQueue);
    }
    private static void methodThree(SwingArena arena){
        Map<String, XandYObject> mapThree = new HashMap<>();

        for(int i = 0; i < 9; i++){
            mapThree.put(""+i, new XandYObject(8-i, i));
        }

        mapThree.put("11", new XandYObject(0, 0));
        mapThree.put("12", new XandYObject(0, 8));
        mapThree.put("13", new XandYObject(8, 0));
        mapThree.put("14", new XandYObject(8, 8));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        blockingQueue.add(mapThree);

//        arena.setRobotPosition(blockingQueue);
    }
    private static void methodFour(SwingArena arena){
        Map<String, XandYObject> mapFour = new HashMap<>();

        for(int i = 0; i < 9; i++){
            mapFour.put(""+i, new XandYObject(8-i, i));
        }

        mapFour.put("11", new XandYObject(2, 0));
        mapFour.put("12", new XandYObject(0, 2));
        mapFour.put("13", new XandYObject(2, 2));

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        blockingQueue.add(mapFour);

//        arena.setRobotPosition(blockingQueue);
    }
    private static void methodFive(SwingArena arena){
        while(true){
            arena.setRobotPosition(blockingQueue);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
