module jsq {
    requires javafx.controls;
    requires javafx.fxml;

    opens jsq to javafx.fxml;
    exports jsq;
}
