import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import POJO.BallisticTableData;
import POJO.Dot;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class GUI extends Application {

    public static final int SCREEN_W = Toolkit.getDefaultToolkit().getScreenSize().width;
    public static final int SCREEN_H = Toolkit.getDefaultToolkit().getScreenSize().height;
    public static units Units = units.EU;

    //A collection of stages, that closes itself when primary stage's onClose event occures

    private static ArrayList<Stage> stages = new ArrayList<>();
    private static Bullet bullet = new Bullet();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        bullet.setName("Custom bullet");
        System.out.println(bullet.toString());

        Label caliber_label = new Label("Caliber");
        Label mass_label = new Label("Bullet weight");
        Label muzzle_vel_label = new Label("Muzzle velocity");

        Label measure_caliber = new Label("mm");
        Label measure_mass = new Label("mg");
        Label measure_velocity = new Label("m/s");

        Label bullet_props_header = new Label("Bullet properties");

        double default_header_font_size = Font.getDefault().getSize() * 1.2;

        bullet_props_header.setFont(Font.font("verdana", FontWeight.SEMI_BOLD, FontPosture.REGULAR, default_header_font_size));

        //a pattern that takes only double values
        Pattern validEditingState = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");

        //this goes to force textfields to get only doubles
        UnaryOperator<TextFormatter.Change> doubleFilter = c -> {
            String text = c.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return c ;
            } else {
                return null ;
            }
        };

        //converts double values to strings and versus vista
        StringConverter<Double> doubleStringConverter = new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return object.toString();
            }

            @Override
            public Double fromString(String string) {
                if (string.isEmpty() || string == null || "-".equals(string) || ".".equals(string) || "-.".equals(string)) {
                    return 0.0;
                } else return Double.valueOf(string);
            }
        };

        TextField caliber_tf = new TextField();
        TextField mass_tf = new TextField();
        TextField muzzle_vel_tf = new TextField();

        caliber_tf.setTextFormatter(new TextFormatter<Double>(doubleStringConverter, 0.0, doubleFilter));
        mass_tf.setTextFormatter(new TextFormatter<Double>(doubleStringConverter, 0.0, doubleFilter));
        muzzle_vel_tf.setTextFormatter(new TextFormatter<Double>(doubleStringConverter, 0.0, doubleFilter));

        caliber_tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    Double cal = Double.valueOf(newValue);
                    if (cal < 1) {
                        bullet.setCaliber_inch(cal);
                    } else {
                        bullet.setCaliber_mm(cal);
                    }
                } catch (Exception ex) { }
            }
        });
        mass_tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    Double mass = Double.valueOf(newValue);
                    switch (Units) {
                        case EU:
                            bullet.setMass_kg(mass * 1000000);
                            break;
                        case US:
                            bullet.setMass_pound(mass);
                            break;
                    }
                } catch (Exception ex) { }
            }
        });

        muzzle_vel_tf.textProperty().addListener((observable, oldValue, newValue) -> {

            try {
                Double vel = Double.valueOf(muzzle_vel_tf.getText());
                switch (Units) {
                    case US:
                        bullet.setMuzzleVelocity_ft_per_s(vel);
                        break;
                    case EU:
                        bullet.setMuzzleVelocity_m_per_s(vel);
                        break;
                }
            } catch (Exception ex) { }
        });

        ChoiceBox<String> ballistics_chbox = new ChoiceBox<>();
        ballistics_chbox.getItems().addAll(
                "Enter G7 form-factor",
                "Build G7 drag from table",
                "Calculate from bullet properties"
        );
        ballistics_chbox.setValue(ballistics_chbox.getItems().get(0));

        TextField g7_ff_tf0 = new TextField(); //to enter G7 form=factor manually
        Button g7_fromTable_bt1 = new Button("..."); //to create G7 chart from table;
        Button g7_fromBulletProps_bt2 = new Button("..."); //to calculate form-factor from bullet properties;


        g7_ff_tf0.setTextFormatter(new TextFormatter<Double>(doubleStringConverter, 1.0, doubleFilter));
        g7_ff_tf0.setText("1.0"); //According to Bryan Litz, G7 FF approximately equals 1
        g7_ff_tf0.textProperty().addListener((observable, oldValue, newValue) -> {
            bullet.setG7_form_factor(Double.valueOf(newValue));
        });

        g7_fromTable_bt1.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Stage localstage = new Stage();
                stages.add(localstage);
                localstage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        stages.remove(localstage);
                    }
                });
                Label head = new Label("Enter bullet properties to the table below");
                head.setFont(Font.font("verdana", FontWeight.SEMI_BOLD, FontPosture.REGULAR, default_header_font_size));

                TableView<Dot> tb = new TableView();
                ObservableList<Dot> observableList = FXCollections.observableArrayList();

                TableColumn<Dot,Double> drag_column = new TableColumn<>("Drag coefficient");
                drag_column.setCellValueFactory(new PropertyValueFactory<Dot,Double>("D"));
                drag_column.prefWidthProperty().bind(tb.widthProperty().multiply(0.5));

                TableColumn<Dot,Double> vel_column = new TableColumn<>("Velocity");
                vel_column.setCellValueFactory(new PropertyValueFactory<Dot,Double>("V"));
                vel_column.prefWidthProperty().bind(tb.widthProperty().multiply(0.5));

                tb.setItems(observableList);
                tb.setEditable(true);
                tb.getColumns().addAll(drag_column,vel_column);

                TextField drag_tf = new TextField();
                TextField vel_tf = new TextField();

                drag_tf.setTextFormatter(new TextFormatter<Double>(doubleStringConverter,0.5,doubleFilter));
                vel_tf.setTextFormatter(new TextFormatter<Double>(doubleStringConverter, 100.0, doubleFilter));

                Button add_btn = new Button("Add to table");
                add_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        Double vel = Double.valueOf(vel_tf.getText());
                        Double drg = Double.valueOf(drag_tf.getText());

                        AtomicBoolean isAlreadyInTable = new AtomicBoolean(false);
                        observableList.forEach((dot) -> {
                            if (dot.getD() == drg && dot.getV() == vel) {
                                isAlreadyInTable.set(true); return;
                            }
                        });
                        if (!(0 < vel && 0 < drg && drg < 1)) {

                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Wooops!");
                            alert.setHeaderText("Incorrect input!");
                            alert.setContentText("Velocity must be bigger thn 0\nDrag coefficient must be in (0,1)");
                            alert.showAndWait();

                        }
                        else if(isAlreadyInTable.get()) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Wooops!");
                            alert.setHeaderText("Incorrect input!");
                            alert.setContentText("This value already exists");
                            alert.showAndWait();
                        }
                        else {
                            observableList.add(new Dot(
                                    new SimpleDoubleProperty(drg),
                                    new SimpleDoubleProperty(vel)
                            ));
                            drag_tf.setText("0.5");
                            vel_tf.setText("100.0");
                        }
                    }
                });
                Button submit_btn = new Button("Submit");
                submit_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (observableList.size() < 1) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Wooops!");
                            alert.setHeaderText("List is empty!");
                            alert.setContentText("Please, enter at least one row");
                            alert.showAndWait();
                        } else {
                            double d[] = new double[observableList.size()];
                            double v[] = new double[observableList.size()];

                            for (int i = 0; i < observableList.size(); i++) {
                                d[i] = observableList.get(i).getD();
                                v[i] = observableList.get(i).getV();
                            }
                            switch (Units) {
                                case EU:
                                    bullet.setG7_velocity_m_per_s(v,d); break;
                                case US:
                                    bullet.setG7_velocity_ft_per_s(v,d); break;
                            }
                        }

                    }
                });
                GridPane controls_pane = new GridPane();
                controls_pane.setHgap(2); controls_pane.setVgap(2);
                controls_pane.add(drag_tf,0,0);
                controls_pane.add(vel_tf,1,0);
                controls_pane.add(add_btn,2,0);
                controls_pane.add(submit_btn,0,1);
                VBox vBox = new VBox();
                vBox.setSpacing(5);
                vBox.setPadding(new Insets(10));
                vBox.getChildren().addAll(head, tb, controls_pane);
                Scene g7_builder_scene = new Scene(vBox,360,240);
                localstage.setFullScreen(false);
                localstage.setScene(g7_builder_scene); localstage.showAndWait();
            }
        });

        g7_fromBulletProps_bt2.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                GridPane pane = new GridPane();
                Label[] labels = new Label[] {
                        new Label("Nose length"),
                        new Label("Rt/R"),
                        new Label("Mepl"),
                        new Label("Tale length"),
                        new Label("Tale angle")
                };
                TextField[] textFields = new TextField[5];
                for (int i = 0; i < textFields.length; i++) {
                    textFields[i] = new TextField();
                    textFields[i].setTextFormatter(new TextFormatter<Double>(doubleStringConverter,1.0,doubleFilter));
                    pane.add(labels[i],0,i);
                    pane.add(textFields[i],1,i);
                }
                Button btn = new Button("Calculate");
                btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        bullet.setG7_form_factor(
                                Double.valueOf(textFields[0].getText()),
                                Double.valueOf(textFields[1].getText()),
                                Double.valueOf(textFields[2].getText()),
                                Double.valueOf(textFields[3].getText()),
                                Double.valueOf(textFields[4].getText())
                        );
                    }
                });
                pane.add(btn, 0,5);
                pane.setVgap(4); pane.setHgap(7);
                pane.setPadding(new Insets(10));
                pane.setAlignment(Pos.TOP_LEFT);

                Scene localScene = new Scene(pane, 280,200);



                Stage localStage = new Stage();
                stages.add(localStage);
                localStage.setScene(localScene);
                localStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        stages.remove(localStage);
                    }
                });

                localStage.showAndWait();
            }
        });

        Consumer<Control> Disable = (c) -> {
            c.setVisible(false);
            c.setDisable(true);
        };

        Consumer<Control> Enable = (c) -> {
            c.setVisible(true);
            c.setDisable(false);
        };
        Disable.accept(g7_fromTable_bt1);
        Disable.accept(g7_fromBulletProps_bt2);


        ballistics_chbox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int i = ballistics_chbox.getItems().indexOf(ballistics_chbox.getValue());
                switch (i) {
                    case 0:
                        Enable.accept(g7_ff_tf0);
                        Disable.accept(g7_fromTable_bt1);
                        Disable.accept(g7_fromBulletProps_bt2);

                        break;
                    case 1:
                        Disable.accept(g7_ff_tf0);
                        Enable.accept(g7_fromTable_bt1);
                        Disable.accept(g7_fromBulletProps_bt2);

                        break;
                    case 2:
                        Disable.accept(g7_ff_tf0);
                        Disable.accept(g7_fromTable_bt1);
                        Enable.accept(g7_fromBulletProps_bt2);

                        break;
                    case 3:
                        Disable.accept(g7_ff_tf0);
                        Disable.accept(g7_fromTable_bt1);
                        Disable.accept(g7_fromBulletProps_bt2);

                        break;

                }
            }
        });

        AnchorPane bullet_prop_Apane = new AnchorPane();
        GridPane bullet_prop_Gpane = new GridPane();
        bullet_prop_Gpane.add(caliber_label,0,0);
        bullet_prop_Gpane.add(mass_label,0,1);
        bullet_prop_Gpane.add(muzzle_vel_label,0,2);

        bullet_prop_Gpane.add(caliber_tf,1,0);
        bullet_prop_Gpane.add(mass_tf,1,1);
        bullet_prop_Gpane.add(muzzle_vel_tf,1,2);

        bullet_prop_Gpane.add(measure_caliber,2,0);
        bullet_prop_Gpane.add(measure_mass,2,1);
        bullet_prop_Gpane.add(measure_velocity, 2,2);

        bullet_prop_Gpane.add(ballistics_chbox,0,3);
        bullet_prop_Gpane.add(g7_ff_tf0,1,3);
        bullet_prop_Gpane.add(g7_fromTable_bt1,1,3);
        bullet_prop_Gpane.add(g7_fromBulletProps_bt2,1,3);



        bullet_prop_Gpane.setVgap(10); bullet_prop_Gpane.setHgap(5);

        Label enviroment_header = new Label("Enivroment");
        enviroment_header.setFont(Font.font("verdana", FontWeight.SEMI_BOLD, FontPosture.REGULAR, default_header_font_size));

        Label air_den_label = new Label("Air density");
        Label temp_label = new Label("Temperature");
        Label rng_label = new Label("Range");

        Label measure_temp = new Label("C");
        Label measure_air = new Label("kg/m^3");
        Label measure_range = new Label("meters");

        TextField air_den_tf = new TextField();
        TextField temp_tf = new TextField();
        TextField rng_tf = new TextField();

        air_den_tf.setTextFormatter(new TextFormatter<Double>(doubleStringConverter, 10.0,doubleFilter));
        temp_tf.setTextFormatter(new TextFormatter<Double>(doubleStringConverter,20.0,doubleFilter));
        rng_tf.setTextFormatter(new TextFormatter<Double>(doubleStringConverter,1000.0,doubleFilter));


        air_den_tf.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                Double den = Double.valueOf(newValue);
                switch (Units) {
                    case EU:
                        Enviroment.setAir_density_kg_to_cubeM(den); break;
                    case US:
                        Enviroment.setAir_density_lb_to_cubeFt(den); break;
                }
            } catch (Exception ex) { }
        });
        temp_tf.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                Double t = Double.valueOf(newValue);
                switch (Units) {
                    case EU:
                        Enviroment.setTempretaure_C(t); break;
                    case US:
                        Enviroment.setTempretaure_F(t); break;
                }
            }  catch (Exception ex) { }
        });
        rng_tf.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                Double r = Double.valueOf(newValue);
                switch (Units) {
                    case EU:
                        Enviroment.setRange_m(r); break;
                    case US:
                        Enviroment.setRange_ft(r); break;
                }
            } catch (Exception ex) { }
        });

        GridPane env_prop_Gpane = new GridPane();
        env_prop_Gpane.add(temp_label,0,0);
        env_prop_Gpane.add(air_den_label,0,1);
        env_prop_Gpane.add(rng_label,0,2);

        env_prop_Gpane.add(temp_tf,1,0);
        env_prop_Gpane.add(air_den_tf,1,1);
        env_prop_Gpane.add(rng_tf,1,2);

        env_prop_Gpane.add(measure_temp,2,0);
        env_prop_Gpane.add(measure_air,2,1);
        env_prop_Gpane.add(measure_range,2,2);

        env_prop_Gpane.setVgap(bullet_prop_Gpane.getVgap());
        env_prop_Gpane.setHgap(bullet_prop_Gpane.getHgap());

        ChoiceBox<units> Units_cb = new ChoiceBox<>();
        Units_cb.getItems().addAll(units.EU,units.US);
        Units_cb.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Units = Units_cb.getValue();

                switch (Units) {
                    case EU:
                        measure_caliber.setText("mm");
                        measure_mass.setText("mg");
                        measure_velocity.setText("m/s");

                        measure_temp.setText("C");
                        measure_air.setText("kg/m^3");
                        measure_range.setText("meters");

                        break;
                    case US:
                        measure_caliber.setText("inch");
                        measure_mass.setText("pounds");
                        measure_velocity.setText("ft/s");

                        measure_temp.setText("F");
                        measure_air.setText("lb/ft^3");
                        measure_range.setText("feet");

                        break;
                }
            }
        });
        Units_cb.setValue(units.EU);

        bullet_prop_Apane.setPadding(new Insets(10));

        AnchorPane.setTopAnchor(bullet_prop_Gpane, 50.0);
        AnchorPane.setLeftAnchor(bullet_prop_Gpane, 10.0);

        AnchorPane.setTopAnchor(bullet_props_header,20.0);
        AnchorPane.setLeftAnchor(bullet_props_header, 10.0);

        AnchorPane.setTopAnchor(Units_cb, 20.0);
        AnchorPane.setRightAnchor(Units_cb, 10.0);


        bullet_prop_Apane.getChildren().addAll(bullet_prop_Gpane, bullet_props_header, Units_cb);

        AnchorPane env_prop_Apane = new AnchorPane();

        AnchorPane.setTopAnchor(enviroment_header, 20.0);
        AnchorPane.setLeftAnchor(enviroment_header, 10.0);

        AnchorPane.setTopAnchor(env_prop_Gpane,50.0);
        AnchorPane.setLeftAnchor(env_prop_Gpane, 10.0);

        env_prop_Apane.getChildren().addAll(enviroment_header, env_prop_Gpane);


        AnchorPane out_Apane = new AnchorPane();

        Label deg = new Label("Degrees = ");
        Label deg_val = new Label("-");

        Button exe_btn = new Button("On this Range");
        exe_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                try {
                    Double angle = BallisticsHandler.getAngle(Enviroment.getRange(), bullet);
                    deg_val.setText(angle.toString());
                }
                catch (Exception ex){
                    deg.setText("-");
                }
            }
        });
        Button tbl_btn = new Button("Build table");
        tbl_btn.setOnMouseClicked(event -> {
            TableView<BallisticTableData> tb = new TableView<>();
            ObservableList<BallisticTableData> list = FXCollections.observableArrayList();
            tb.setItems(list);

            TableColumn<BallisticTableData, Double> range = new TableColumn<>("Range");
            TableColumn<BallisticTableData, Double> moa = new TableColumn<>("MOA");
            TableColumn<BallisticTableData, Double> angle = new TableColumn<>("Angle");

            range.setCellValueFactory(new PropertyValueFactory<>("range"));
            range.prefWidthProperty().bind(tb.widthProperty().divide(3));

            moa.setCellValueFactory(new PropertyValueFactory<BallisticTableData,Double>("moa"));
            moa.prefWidthProperty().bind(tb.widthProperty().divide(3));

            angle.setCellValueFactory(new PropertyValueFactory<BallisticTableData,Double>("angle"));
            angle.prefWidthProperty().bind(tb.widthProperty().divide(3));

            tb.getColumns().addAll(range,moa,angle);
            for (Double i = 300.; i < 1000; i+=50) {
                Double ang = BallisticsHandler.getAngle(i, bullet);
                BallisticTableData btd = new BallisticTableData();
                btd.setAngle(ang);
                btd.setRange(i);
                list.add(btd);
            }
            Scene scene = new Scene(tb);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.showAndWait();

        });

        GridPane out_Gpane = new GridPane();
        out_Gpane.add(deg,0,0); out_Gpane.add(deg_val,1,0);
        out_Gpane.add(exe_btn,0,1); out_Gpane.add(tbl_btn,1,1);

        AnchorPane.setLeftAnchor(out_Gpane,70.);
        AnchorPane.setTopAnchor(out_Gpane,10.);

        out_Apane.getChildren().addAll(out_Gpane);

        HBox hBox = new HBox();
        hBox.getChildren().addAll(env_prop_Apane, out_Apane);
        hBox.setSpacing(15);

        VBox vBox = new VBox();
        vBox.setMinSize(640,480);
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(15);

        vBox.getChildren().addAll(bullet_prop_Apane,hBox);

        Scene mainScene = new Scene(vBox,640,480);


        primaryStage.setScene(mainScene);
        primaryStage.setResizable(true);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                stages.forEach((stage -> stage.close()));
            }
        });
    }

    public enum units {
        EU,
        US
    }
}
