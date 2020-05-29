package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Controller implements EventHandler<MouseEvent> {
    @FXML
    private RadioButton selectRB;

    @FXML
    private RadioButton ellipseRB;

    @FXML
    private RadioButton rectangleRB;

    @FXML
    private RadioButton lineRB;

    @FXML
    private ColorPicker colorP;

    @FXML
    private Button deleteB;

    @FXML
    private Button cloneB;

    @FXML
    private Canvas canvas;

    private List<Shape> shapes;
    private Shape drawingShape;
    private Shape selectedShape;
    private Dimension2D offset;

    @FXML
    public void initialize() {
        shapes = new ArrayList<Shape>();
        drawingShape = null;
        selectedShape = null;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, this);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this);

        selectRB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) drawingShape = null;
        });
        ellipseRB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                drawingShape = new Ellipse();
                offset = null;
            }
        });
        rectangleRB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                drawingShape = new Rectangle();
                offset = null;
            }
        });
        lineRB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                drawingShape = new Line();
                offset = null;
            }
        });
    }

    // too much accustomed to swing :)
    public void paintComponents(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setLineWidth(5);
        gc.setFill(colorP.getValue());
        for (Shape shape : shapes) {
            shape.draw(gc);
        }
    }

    @Override
    public void handle(MouseEvent event) {
        double mx = event.getX(), my = event.getY();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        paintComponents(gc);
        // drawing mode
        if (drawingShape != null) {
            drawingShape.position = new Dimension2D(mx + 10, my + 10);
            drawingShape.draw(gc);
            // summoning new shape the same type as drawingShape
            if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
                System.out.println("press " + mx + ' ' + my);
                shapes.add(drawingShape.summon(mx, my));
            }
        } else { // selection mode
            if (selectedShape != null) {
                if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
                    System.out.println("press " + mx + ' ' + my);
                    if (offset == null)
                        offset = new Dimension2D(mx, my);
                }
                if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
                    System.out.println("drag " + mx + ' ' + my);
                    selectedShape.size = new Dimension2D(selectedShape.size.getWidth() + (mx - offset.getWidth()), selectedShape.size.getHeight() + (my - offset.getHeight()));
                }
            }
            if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
                for (Shape shape : shapes) { // search for the clicked shape
                    double sx1 = shape.position.getWidth(), sx2 = shape.position.getWidth() + shape.size.getWidth();
                    double sy1 = shape.position.getHeight(), sy2 = shape.position.getHeight() + shape.size.getHeight();
                    if (mx > sx1 && mx < sx2 && my > sy1 && my < sy2) {
                        if (selectedShape != null)
                            selectedShape.selected = false;
                        shape.selected = true;
                        selectedShape = shape;
                    }
                }
            }

        }
    }

    abstract class Shape { // created my own because the javafx version don't match my needs
        Dimension2D size;
        Dimension2D position;
        boolean selected = false;

        Shape() { // for the drawing shape
            position = null;
            size = new Dimension2D(20, 20);
        }

        Shape(double posX, double posY) { // for summoning
            position = new Dimension2D(posX, posY);
            size = new Dimension2D(50, 50);
        }

        abstract public void draw(GraphicsContext gc);

        abstract public Shape summon(double posX, double posY);
    }

    class Line extends Shape {

        public Line() {
            super();
        }

        public Line(double posX, double posY) {
            super(posX, posY);
        }

        public void draw(GraphicsContext gc) {
            gc.setLineWidth(5);
            if (selected)
                gc.fillOval(position.getWidth(), position.getHeight(), 10, 10);
            gc.strokeLine(position.getWidth(), position.getHeight(), position.getWidth() + size.getWidth(), position.getHeight() + size.getHeight());
        }

        @Override
        public Shape summon(double posX, double posY) {
            return new Line(posX, posY);
        }

    }

    class Ellipse extends Shape {

        public Ellipse() {
            super();
        }

        public Ellipse(double posX, double posY) {
            super(posX, posY);
        }

        public void draw(GraphicsContext gc) {
            if (selected)
                gc.strokeOval(position.getWidth(), position.getHeight(), size.getWidth(), size.getHeight());
            gc.fillOval(position.getWidth(), position.getHeight(), size.getWidth(), size.getHeight());
        }

        @Override
        public Shape summon(double posX, double posY) {
            return new Ellipse(posX, posY);
        }
    }

    class Rectangle extends Shape {

        public Rectangle() {
            super();
        }

        public Rectangle(double posX, double posY) {
            super(posX, posY);
        }

        public void draw(GraphicsContext gc) {
            if (selected)
                gc.strokeRect(position.getWidth(), position.getHeight(), size.getWidth(), size.getHeight());
            gc.fillRect(position.getWidth(), position.getHeight(), size.getWidth(), size.getHeight());
        }

        @Override
        public Shape summon(double posX, double posY) {
            return new Rectangle(posX, posY);
        }
    }
}
