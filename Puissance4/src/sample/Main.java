package sample;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main extends Application {

    // Initialisation des variables
    private static final int TILE_SIZE = 80;
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private int RED;
    private int YELLOW;
    private String REDString = Integer.toString(RED);
    private String YELLOWString = Integer.toString(YELLOW);

    private Label jaune = new Label("Yellow's score : ");
    private Label scorejaune = new Label(GetYellowScore());
    private Label rouge = new Label("Red's Score : ");
    private Label scorerouge  = new Label(GetRedScore());
    private Button restartButton = new Button();

    private boolean redMove = true;
    private Disc[][] grid = new Disc[COLUMNS][ROWS];

    private Pane discRoot = new Pane();

    // Création du plateau et de la grille
    private Parent createContent() {
        Pane root = new Pane();
        root.getChildren().add(discRoot);

        Shape gridShape = makeGrid();
        root.getChildren().add(gridShape);
        root.getChildren().addAll(makeColumns());

        return root;
    }

    // Définition des des formes dans le plateau
    private Shape makeGrid()
    {
        Shape shape = new Rectangle((COLUMNS + 1) * TILE_SIZE, (ROWS + 1) * TILE_SIZE);

        for( int y = 0; y < ROWS ; y++)
        {
            for (int x = 0 ; x<COLUMNS ; x++)
            {
                Circle circle = new Circle(TILE_SIZE / 2);
                circle.setCenterX(TILE_SIZE/2);
                circle.setCenterY(TILE_SIZE/2);
                circle.setTranslateX(x * (TILE_SIZE+5) + TILE_SIZE / 4);
                circle.setTranslateY(y * (TILE_SIZE+5) + TILE_SIZE / 4);

                shape = Shape.subtract(shape, circle);
            }
        }

        Light.Distant light = new Light.Distant();
        light.setAzimuth(45.0);
        light.setElevation(30.0);

        Lighting lighting = new Lighting();
        lighting.setLight(light);
        lighting.setSurfaceScale(5.0);

        shape.setFill(Color.GREEN);
        shape.setEffect(lighting);

        return shape;
    }

    // Définition des colonnes dans le Pane + surbrillance lors du déplacement et du clic
    private List<Rectangle> makeColumns()
    {
        List<Rectangle> list = new ArrayList<>();

        for(int x = 0; x < COLUMNS; x++)
        {
            Rectangle rect = new Rectangle(TILE_SIZE, (ROWS+1) * TILE_SIZE);
            rect.setTranslateX(x * (TILE_SIZE+5) + TILE_SIZE / 4);
            rect.setFill(Color.TRANSPARENT);

            rect.setOnMouseEntered(event -> rect.setFill(Color.rgb(200,200,50,0.3)));
            rect.setOnMouseExited(event -> rect.setFill(Color.TRANSPARENT));

            final int column = x;
            rect.setOnMouseClicked(event -> placeDisc(new Disc(redMove), column));

            list.add(rect);
        }
        return list;
    }

    // Fonction permettant de placer un pion + définition de l'animation
    private void placeDisc(Disc disc, int column)
    {
        int row = ROWS - 1;
        do
        {
            if(!getDisc(column,row).isPresent())
                break;

            row--;
        }while(row>=0);

        if(row<0)
            return;

        grid[column][row] = disc;
        discRoot.getChildren().add(disc);
        disc.setTranslateX(column * (TILE_SIZE + 5) + TILE_SIZE/4);

        final int currentRow=row;

        TranslateTransition animation = new TranslateTransition(Duration.seconds(0.3), disc);
        animation.setToY(row * (TILE_SIZE + 5) + TILE_SIZE/4);
        animation.setOnFinished(event -> {
            if(gameEnded(column, currentRow)) {
                gameOver();
            }
            redMove = !redMove;
        } );
        animation.play();
    }

    // Définition des 4 sens dans lesquels le joueur peut aligner les pions
    private boolean gameEnded(int column, int row)
    {
        List<Point2D> vertical = IntStream.rangeClosed(row - 3, row+3).mapToObj(r -> new Point2D(column, r)).collect(Collectors.toList());

        List<Point2D> horizontal = IntStream.rangeClosed(column - 3, column+3).mapToObj(c -> new Point2D(c, row)).collect(Collectors.toList());

        Point2D topLeft = new Point2D(column - 3, row - 3);
        List<Point2D> diagonalA = IntStream.rangeClosed(0,6).mapToObj(i -> topLeft.add(i,i)).collect(Collectors.toList());

        Point2D BottomLeft = new Point2D(column - 3, row + 3);
        List<Point2D> diagonalB = IntStream.rangeClosed(0,6).mapToObj(i -> BottomLeft.add(i,-i)).collect(Collectors.toList());

        return checkRange(vertical) || checkRange(horizontal) ||checkRange(diagonalA) || checkRange(diagonalB);
    }

    // Vérification de l'alignement de 4 pions
    private boolean checkRange(List<Point2D> points)
    {
        int chain = 0;

        for(Point2D p : points)
        {
            int column = (int) p .getX();
            int row = (int) p.getY();

            Disc disc = getDisc(column, row).orElse(new Disc(!redMove));
            if(disc.red == redMove)
            {
                chain++;
                if (chain == 4)
                {
                    return true;
                }
            }
            else
            {
                chain=0;
            }
        }
        return false;
    }

    // Action réalisée quand la fin du jeu est atteinte
    private void gameOver()
    {
        if (redMove) {
            System.out.println("Red won");

        }
        else if(!redMove){
            System.out.println("Yellow won");
        }
        /*if (redMove){
            RED++;
            System.out.println("Red's score : " + RED);

        }
        else{
            YELLOW++;
            System.out.println("Yellow's Score : " + YELLOW);
        }

        if(RED == 3){
            System.out.println("RED PLAYER WON !");
        }
        else if(YELLOW == 3)
        {
            System.out.println("YELLOW PLAYER WON !");
        }*/
    }

    // Fonction permettant de récupérer le score du joueur Jaune
    private String GetYellowScore(){
        return YELLOWString;
    }

    // Fonction permettant de récupérer le score du jour Rouge
    private String GetRedScore(){
        return REDString;
    }

    // Vérification du nombre de pions contenus dans une colonne
    private Optional<Disc> getDisc(int column, int row)
    {
        if (column < 0 || column >= COLUMNS || row<0 || row>= ROWS)
        {
            return Optional.empty();
        }
        return Optional.ofNullable(grid[column][row]);
    }

    // Démarrage de l'application
    @Override
    public void start(Stage stage) throws Exception
    {
        stage.setScene(new Scene(createContent()));
        stage.show();

        Stage theStage = new Stage();

        CreateSecondStage(theStage);

        /*restartButton.setOnAction( __ ->
        {
            System.out.println( "Restarting app!" );
            stage.close();
            Platform.runLater( () -> new ().start( new Stage() ) );
        } );*/
    }

    // Procédure permettant de créer la deuxième fenêtre
    private void CreateSecondStage(Stage theStage)
    {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 380,150, Color.WHITE);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5));
        gridPane.setHgap(5);
        gridPane.setVgap(5);

        restartButton.setText("Clear the grid");

        ColumnConstraints column1 = new ColumnConstraints(100);
        ColumnConstraints column2 = new ColumnConstraints(50,150,300);

        column2.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(column1, column2);

        GridPane.setHalignment(jaune, HPos.RIGHT);
        gridPane.add(jaune, 0,0);

        GridPane.setHalignment(rouge, HPos.RIGHT);
        gridPane.add(rouge,0,1);

        GridPane.setHalignment(scorejaune, HPos.LEFT);
        gridPane.add(scorejaune, 1,0);

        GridPane.setHalignment(scorerouge, HPos.LEFT);
        gridPane.add(scorerouge,1,1);

        GridPane.setHalignment(restartButton, HPos.CENTER);
        gridPane.add(restartButton, 0,3);

        root.setCenter(gridPane);
        theStage.setScene(scene);
        theStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}