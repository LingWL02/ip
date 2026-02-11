import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

import components.DialogBox;


public class Main extends Application {

    private ScrollPane scrollPane;
    private VBox dialogContainer;
    private TextField userInput;
    private Button sendButton;
    private Scene scene;

    private Image userImage = new Image(this.getClass().getResourceAsStream("/images/pdidd.jpg"));
    private Image dukeImage = new Image(this.getClass().getResourceAsStream("/images/jefep.jpg"));
    private Duke duke = new Duke();
    @Override
    public void start(Stage stage) {
        //Setting up required components

        this.scrollPane = new ScrollPane();
        this.dialogContainer = new VBox();
        scrollPane.setContent(dialogContainer);

        this.userInput = new TextField();
        this.sendButton = new Button("Send");

        //Handling user input

        this.sendButton.setOnMouseClicked((event) -> {
            this.handleUserInput();
        });
        this.userInput.setOnAction((event) -> {
            this.handleUserInput();
        });

        AnchorPane mainLayout = new AnchorPane();
        mainLayout.getChildren().addAll(scrollPane, userInput, sendButton);

        this.scene = new Scene(mainLayout);

        stage.setScene(this.scene);
        stage.show();

        //Formatting the window to look as expected

        stage.setTitle("Duke");
        stage.setResizable(false);
        stage.setMinHeight(600.0);
        stage.setMinWidth(400.0);

        mainLayout.setPrefSize(400.0, 600.0);

        this.scrollPane.setPrefSize(385, 535);
        this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        this.scrollPane.setVvalue(1.0);
        this.scrollPane.setFitToWidth(true);

        this.dialogContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);

        this.userInput.setPrefWidth(325.0);

        this.sendButton.setPrefWidth(55.0);

        AnchorPane.setTopAnchor(this.scrollPane, 1.0);

        AnchorPane.setBottomAnchor(this.sendButton, 1.0);
        AnchorPane.setRightAnchor(this.sendButton, 1.0);

        AnchorPane.setLeftAnchor(this.userInput, 1.0);
        AnchorPane.setBottomAnchor(this.userInput, 1.0);

        //More code to be added here later
        //Scroll down to the end every time dialogContainer's height changes.
        this.dialogContainer.heightProperty().addListener(
            (observable) -> this.scrollPane.setVvalue(1.0)
        );
    }

    private void handleUserInput() {
        String userText = this.userInput.getText();
        String dukeText = this.duke.getResponse(userText);
        this.dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(userText, this.userImage),
                DialogBox.getDukeDialog(dukeText, this.dukeImage)
        );
        userInput.clear();
    }
}
