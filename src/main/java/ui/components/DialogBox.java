package ui.components;
import java.io.IOException;
import java.util.Collections;

import bot.response.Response;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

/**
 * Represents a dialog box consisting of an ImageView to represent the speaker's face
 * and a label containing text from the speaker.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    private DialogBox(String text, Image img) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.setText(text);
        displayPicture.setImage(img);

        // "Cover" crop: take a centered square from the source image so the
        // circle is always filled with pixels (no letterbox / whitespace).
        double size = displayPicture.getFitWidth(); // e.g. 50 px
        displayPicture.setPreserveRatio(false); // we handle proportions ourselves
        displayPicture.setFitWidth(size);
        displayPicture.setFitHeight(size);

        double imgW = img.getWidth();
        double imgH = img.getHeight();
        if (imgW > 0 && imgH > 0) {
            double crop = Math.min(imgW, imgH); // largest square that fits
            double x = (imgW - crop) / 2.0; // centre horizontally
            double y = (imgH - crop) / 2.0; // centre vertically
            displayPicture.setViewport(new Rectangle2D(x, y, crop, crop));
        }

        // Clip rendered square to a circle
        double radius = size / 2.0;
        Circle clip = new Circle(radius, radius, radius);
        displayPicture.setClip(clip);
    }

    /**
     * Flips the dialog box such that the ImageView is on the left and text on the right.
     */
    private void flip() {
        ObservableList<Node> tmp = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(tmp);
        getChildren().setAll(tmp);
        setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
    }

    public static DialogBox getUserDialog(String text, Image img) {
        return new DialogBox(text, img);
    }

    /**
     * Creates a bot dialog box styled according to the response type.
     *
     * @param response The bot's response, carrying both the text and its semantic type.
     * @param img      The bot's avatar image.
     * @return A flipped, styled DialogBox for the bot.
     */
    public static DialogBox getBotDialog(Response response, Image img) {
        var db = new DialogBox(response.getMessage(), img);
        db.flip();
        db.applyResponseStyle(response.getType());
        return db;
    }

    /**
     * Applies a CSS style class to the dialog label based on the response type,
     * so the UI can visually distinguish errors, successes, info, etc.
     */
    private void applyResponseStyle(Response.Type type) {
        String cssClass = switch (type) {
        case ERROR -> "error-label";
        case UNKNOWN -> "unknown-label";
        case SUCCESS -> "success-label";
        case INFO -> "info-label";
        case CHEER -> "cheer-label";
        case GREETING -> "greeting-label";
        case FAREWELL -> "farewell-label";
        };
        dialog.getStyleClass().add(cssClass);
    }
}
