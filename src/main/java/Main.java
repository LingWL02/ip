import bot.Bot;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ui.MainWindow;

/**
 * A GUI for Duke using FXML.
 */
public class Main extends Application {

    private Bot bot = new Bot("Lil Bro", "_".repeat(50));

    @Override
    public void start(Stage stage) {
        try {
            bot.initialize();
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane ap = fxmlLoader.load();
            Scene scene = new Scene(ap);
            stage.setScene(scene);
            fxmlLoader.<MainWindow>getController().setBot(bot); // inject the Duke instance
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
