module jsq
{
    requires transitive javafx.controls;
    requires javafx.fxml;
    
    requires org.json;
    requires org.apache.commons.codec;
    requires org.apache.commons.io;

    requires org.lwjgl.openal;

    opens jsq to javafx.fxml;
    opens jsq.home to javafx.fxml;
    opens jsq.editor to javafx.fxml;
    opens jsq.stop_selector to javafx.fxml;
    exports jsq;
}
