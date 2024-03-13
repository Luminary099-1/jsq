module jsq {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;

    opens jsq to javafx.fxml;
    exports jsq;
}
