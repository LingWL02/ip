package ui;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;

import bot.Bot;
import ui.components.DialogBox;

/**
 * Controller for the main GUI.
 */

public class MainWindow {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Bot bot;

    private Image userImage = new Image(this.getClass().getResourceAsStream("/images/pdidd.jpg"));
    private Image dukeImage = new Image(this.getClass().getResourceAsStream("/images/jefep.jpg"));
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /** Injects the Duke instance */
    public void setBot(Bot bot) {
        this.bot = bot;

        // Display greeting message when bot is set
        String greeting = bot.getGreeting();
        dialogContainer.getChildren().add(
            DialogBox.getBotDialog(greeting, dukeImage)
        );
    }

    /**
     * Creates two dialog boxes, one echoing user input and the other containing Duke's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        String response = bot.getResponse(input);
        dialogContainer.getChildren().addAll(
            DialogBox.getUserDialog(input, userImage),
            DialogBox.getBotDialog(response, dukeImage)
        );
        userInput.clear();

        // Check if bot should terminate and close the window
        if (!bot.isAlive()) {
            // Use Platform.runLater to ensure the dialog is added before closing
            Platform.runLater(() -> {
                Stage stage = (Stage) userInput.getScene().getWindow();
                stage.close();
            });
        }
    }
}
