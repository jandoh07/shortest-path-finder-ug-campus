package org.example;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.painter.Painter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;


public class App extends Application {
    private JXMapViewer mapViewer;
    List<GeoPosition> geoPositions = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        try {
            final SwingNode swingNode = new SwingNode();
            createMap(swingNode);

            GraphBuilder graphBuilder = new GraphBuilder();

            // Load nodes and edges from CSV files
            graphBuilder.loadNodes("src/main/resources/nodes.csv");
            graphBuilder.loadEdges("src/main/resources/edges.csv");

            Graph graph = new Graph(graphBuilder.getNodes(), graphBuilder.getEdges());

            DjikstrasAlgorithm djikstrasAlgorithm = new DjikstrasAlgorithm();
            AStarAlgorithm aStarAlgorithm = new AStarAlgorithm();


            Button zoomInButton = new Button("Zoom In");
            zoomInButton.setOnAction(e -> mapViewer.setZoom(mapViewer.getZoom() - 1));

            Button zoomOutButton = new Button("Zoom Out");
            zoomOutButton.setOnAction(e -> mapViewer.setZoom(mapViewer.getZoom() + 1));

            HBox zoomControls = new HBox(10, zoomInButton, zoomOutButton);

            Label algorithmLabel = new Label("Select Algorithm:");
            ChoiceBox<String> choiceBox = new ChoiceBox<>();
            choiceBox.getItems().addAll("Djikstra's Algorithm", "A* Algorithm");
            choiceBox.setValue("Djikstra's Algorithm");

            Label startLabel = new Label("Start Location:");
            TextField startField = new TextField();

            Label endLabel = new Label("End Location:");
            TextField endField = new TextField();

            Label landmarkLabel = new Label("Select Landmark/Location:");
            TextField landmarkField = new TextField();

            Button submitButton = new Button("Get Shortest Path");

            Text distanceText = new Text(" ");
            distanceText.setWrappingWidth(195);

            Text timeText = new Text(" ");
            timeText.setWrappingWidth(195);

            VBox startSuggestions = new VBox(4);
            VBox endSuggestions = new VBox(4);
            VBox landmarkSuggestions = new VBox(4);

            VBox test = new VBox(10);
            test.setStyle("-fx-padding: 10;");
            test.setPrefWidth(200);
            test.getChildren().addAll(zoomControls,algorithmLabel,choiceBox,startLabel, startField,startSuggestions,endLabel,endField,endSuggestions,landmarkLabel,landmarkField,landmarkSuggestions,submitButton, distanceText, timeText);


            startField.textProperty().addListener((observable, oldValue, newValue) -> {

                startSuggestions.getChildren().clear();

                List<String> locations = graphBuilder.searchLocations(newValue);
                int itemsToPrint = Math.min(locations.size(), 4);

                for (int i = 0; i < itemsToPrint; i++) {
                    Hyperlink hyperlink = new Hyperlink(locations.get(i));
                    hyperlink.wrapTextProperty().setValue(true);

                    hyperlink.setOnAction(e -> {
                        startField.setText(hyperlink.getText());
                        startSuggestions.getChildren().clear();
                    });

                    startSuggestions.getChildren().add(hyperlink);
                }
            });

            endField.textProperty().addListener((observable, oldValue, newValue) -> {

                endSuggestions.getChildren().clear();

                List<String> locations = graphBuilder.searchLocations(newValue);
                int itemsToPrint = Math.min(locations.size(), 4);

                for (int i = 0; i < itemsToPrint; i++) {
                    Hyperlink hyperlink = new Hyperlink(locations.get(i));
                    hyperlink.wrapTextProperty().setValue(true);

                    hyperlink.setOnAction(e -> {
                        endField.setText(hyperlink.getText());
                        endSuggestions.getChildren().clear();
                    });

                    endSuggestions.getChildren().add(hyperlink);
//                    System.out.println(locations.get(i));
                }
            });

            landmarkField.textProperty().addListener((observable, oldValue, newValue) -> {

                landmarkSuggestions.getChildren().clear();

                List<String> locations = graphBuilder.searchLocations(newValue);
                int itemsToPrint = Math.min(locations.size(), 4);

                for (int i = 0; i < itemsToPrint; i++) {
                    Hyperlink hyperlink = new Hyperlink(locations.get(i));
                    hyperlink.wrapTextProperty().setValue(true);

                    hyperlink.setOnAction(e -> {
                        landmarkField.setText(hyperlink.getText());
                        landmarkSuggestions.getChildren().clear();
                    });

                    landmarkSuggestions.getChildren().add(hyperlink);
//                    System.out.println(locations.get(i));
                }
            });

            BorderPane pane = new BorderPane();
            pane.setCenter(swingNode);
            pane.setLeft(test);

            submitButton.setOnAction(e -> {
                String startNode = graphBuilder.getLocationNodeId(startField.getText());
                String endNode = graphBuilder.getLocationNodeId(endField.getText());
                double distance = 0.0;

                if (Objects.equals(choiceBox.getValue(), "Djikstra's Algorithm")) {
                    if (landmarkField.getText().isEmpty()) {
                        distance = djikstrasAlgorithm.findShortestPaths(graph, startNode).get(endNode);
                        geoPositions = djikstrasAlgorithm.printPath(startNode, endNode, graph);
                    } else {
                        String landmarkNode = graphBuilder.getLocationNodeId(landmarkField.getText());
                        double distance1 = djikstrasAlgorithm.findShortestPaths(graph, startNode).get(landmarkNode);
                        geoPositions = djikstrasAlgorithm.printPath(startNode, landmarkNode, graph);

                        double distance2 = djikstrasAlgorithm.findShortestPaths(graph, landmarkNode).get(endNode);
                        geoPositions.addAll(djikstrasAlgorithm.printPath(landmarkNode, endNode, graph));

                        distance = distance1 + distance2;
                    }
                } else if (Objects.equals(choiceBox.getValue(), "A* Algorithm")) {
                    if (landmarkField.getText().isEmpty()) {
                        PathResult result = aStarAlgorithm.findShortestPath(graph, startNode, endNode);
                        distance = result.getDistance();
                    geoPositions = result.getPath();
                    } else {
                        String landmarkNode =graphBuilder.getLocationNodeId(landmarkField.getText());
                        PathResult result = aStarAlgorithm.findShortestPath(graph,startNode, landmarkNode);
                        PathResult result1 = aStarAlgorithm.findShortestPath(graph, landmarkNode, endNode);
                        distance = result.getDistance() + result1.getDistance();
                        geoPositions = result.getPath();
                        geoPositions.addAll(result1.getPath());
                    }
                }

                DecimalFormat df = new DecimalFormat("#.##");

                distanceText.setText("Distance to target: " + df.format(distance) + "m");
                timeText.setText("Time to target(30km/h): " + df.format((distance /(30.00 * 1000 / 3600))/60 ) + "min");

                Painter<JXMapViewer> lineOverlay = (g, map, w, h) -> {
                    g = (Graphics2D) g.create();

                    g.setColor(Color.RED);
                    g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    // Get the current viewport position
                    Rectangle viewportBounds = map.getViewportBounds();

                    for (int i = 0; i < geoPositions.size() - 1; i++) {
                        Point2D pt1 = map.convertGeoPositionToPoint(geoPositions.get(i));
                        Point2D pt2 = map.convertGeoPositionToPoint(geoPositions.get(i + 1));

                        g.draw(new Line2D.Double(pt1, pt2));
                    }

                    g.dispose();
                };

                mapViewer.setCenterPosition(geoPositions.get(0));
                mapViewer.setOverlayPainter(lineOverlay);
                mapViewer.repaint();
            });

            Scene scene = new Scene(pane, 1000, 700);
            primaryStage.setTitle("University of Ghana Campus Map");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();


        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void createMap(final SwingNode swingNode) {
        SwingUtilities.invokeLater(() -> {
            mapViewer = new JXMapViewer();

            TileFactoryInfo info = new OSMTileFactoryInfo();
            DefaultTileFactory tileFactory = new DefaultTileFactory(info);
            mapViewer.setTileFactory(tileFactory);

            // Set the focus on UG campus with zoom level
            GeoPosition geo = new GeoPosition(5.65055, -0.18497);
            mapViewer.setZoom(3); // Adjust zoom level as necessary
            mapViewer.setAddressLocation(geo);

            // Add mouse wheel listener for zooming
            mapViewer.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (e.getWheelRotation() < 0) {
                        mapViewer.setZoom(mapViewer.getZoom() - 1);
                    } else {
                        mapViewer.setZoom(mapViewer.getZoom() + 1);
                    }
                }
            });

            PanMouseInputListener panner = new PanMouseInputListener(mapViewer);
            mapViewer.addMouseListener(panner);
            mapViewer.addMouseMotionListener(panner);
            mapViewer.addMouseWheelListener(panner);

            swingNode.setContent(mapViewer);


            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> mapViewer.repaint());
                }
            }, 1000);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}