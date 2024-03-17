module jsq
{
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires org.json;

    opens jsq to javafx.fxml;
    opens jsq.home to javafx.fxml;
    opens jsq.editor to javafx.fxml;
    opens jsq.stop_selector to javafx.fxml;
    exports jsq;
}
