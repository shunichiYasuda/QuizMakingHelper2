module com.genSci.tools.QuizMakingHelper {
    requires javafx.controls;
    requires javafx.fxml;
	requires javafx.graphics;

    opens com.genSci.tools.QuizMakingHelper to javafx.fxml;
    exports com.genSci.tools.QuizMakingHelper;
}