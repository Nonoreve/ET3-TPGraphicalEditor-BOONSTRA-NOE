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
    private Shape iconShape;
    private Shape drawingShape;
    private Shape selectedShape;
    private Dimension2D origin;

    @FXML
    public void initialize() {
        shapes = new ArrayList<Shape>();
        iconShape = null;
        selectedShape = null;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setHeight(600);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this);
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, this);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
        canvas.addEventHandler(MouseEvent.MOUSE_ENTERED, this);

        selectRB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) iconShape = null;
        });
        ellipseRB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                iconShape = new Ellipse(colorP.getValue());
                origin = null;
            }
        });
        rectangleRB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                iconShape = new Rectangle(colorP.getValue());
                origin = null;
            }
        });
        lineRB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                iconShape = new Line(colorP.getValue());
                origin = null;
            }
        });
    }

    // too much accustomed to swing :)
    public void paintComponents(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setLineWidth(5);
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
        if (iconShape != null) {
            iconShape.position = new Dimension2D(mx + 10, my + 10);
            iconShape.draw(gc);
            // freezing the drawn shape
            if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
                System.out.println("press " + mx + ' ' + my);
                shapes.add(drawingShape);
            }
            // summoning new shape the same type as drawingShape
            if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
                System.out.println("press " + mx + ' ' + my);
                drawingShape = iconShape.summon(mx, my);
                origin = new Dimension2D(mx, my);
            }
            // resizing the drawn shape
            if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
                System.out.println("drag " + mx + ' ' + my);
                drawingShape.size = new Dimension2D((mx - origin.getWidth()), (my - origin.getHeight()));
                drawingShape.draw(gc);
            }
            // reset color
            if (event.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
                System.out.println("enter " + mx + ' ' + my);
                iconShape.color = colorP.getValue();
            }
        } else { // selection mode
            if (selectedShape != null) {
                if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
                    System.out.println("drag " + mx + ' ' + my);
                    selectedShape.position = new Dimension2D(mx, my);
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
        Color color;
        boolean selected = false;

        Shape(Color color) { // for the drawing shape
            position = null;
            this.color = color;
            size = new Dimension2D(20, 20);
        }

        Shape(Color color, double posX, double posY) { // for summoning
            position = new Dimension2D(posX, posY);
            this.color = color;
            size = new Dimension2D(1, 1);
        }

        abstract public void draw(GraphicsContext gc);

        abstract public Shape summon(double posX, double posY);
    }

    class Line extends Shape {

        public Line(Color color) {
            super(color);
        }

        public Line(Color color, double posX, double posY) {
            super(color, posX, posY);
        }

        public void draw(GraphicsContext gc) {
            gc.setFill(color);
            gc.setLineWidth(5);
            if (selected)
                gc.fillOval(position.getWidth(), position.getHeight(), 10, 10);
            gc.strokeLine(position.getWidth(), position.getHeight(), position.getWidth() + size.getWidth(), position.getHeight() + size.getHeight());
        }

        @Override
        public Shape summon(double posX, double posY) {
            return new Line(color, posX, posY);
        }

    }

    class Ellipse extends Shape {

        public Ellipse(Color color) {
            super(color);
        }

        public Ellipse(Color color, double posX, double posY) {
            super(color, posX, posY);
        }

        public void draw(GraphicsContext gc) {
            gc.setFill(color);
            if (selected)
                gc.strokeOval(position.getWidth(), position.getHeight(), size.getWidth(), size.getHeight());
            gc.fillOval(position.getWidth(), position.getHeight(), size.getWidth(), size.getHeight());
        }

        @Override
        public Shape summon(double posX, double posY) {
            return new Ellipse(color, posX, posY);
        }
    }

    class Rectangle extends Shape {

        public Rectangle(Color color) {
            super(color);
        }

        public Rectangle(Color color, double posX, double posY) {
            super(color, posX, posY);
        }

        public void draw(GraphicsContext gc) {
            gc.setFill(color);
            if (selected)
                gc.strokeRect(position.getWidth(), position.getHeight(), size.getWidth(), size.getHeight());
            gc.fillRect(position.getWidth(), position.getHeight(), size.getWidth(), size.getHeight());
        }

        @Override
        public Shape summon(double posX, double posY) {
            return new Rectangle(color, posX, posY);
        }
    }
}
