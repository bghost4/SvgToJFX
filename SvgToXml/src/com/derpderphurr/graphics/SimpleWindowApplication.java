package com.derpderphurr.graphics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

public class SimpleWindowApplication extends Application {

	protected Node tux;
	
	public SimpleWindowApplication() {
		super();
		
		SvgToFXBuilder b = new SvgToFXBuilder();
		try {
			b.setDefaultEnteredHandler(meh -> { 
				if(meh.getSource() instanceof Shape) {
					Shape shape = (Shape)meh.getSource();
					shape.setFill(Color.AQUA);
					System.out.println("Mouse Entered: "+shape.getId());
				} else {
					System.err.println("Bad Class: "+meh.getSource().getClass().getName());
				}
			});
			
			b.setDefaultClickHandler(meh -> { 
				if(meh.getSource() instanceof Shape) {
					Shape shape = (Shape)meh.getSource();
					//shape.setFill(Color.AQUA);
					System.out.println("Mouse Entered: "+shape.getId());
					if(meh.getButton() == MouseButton.SECONDARY) {
						//shape.setVisible(false);
						shape.getStrokeDashArray().setAll(25.0,20.0,5.0,20.0);
						shape.setOnMouseEntered(null);
					} else if (meh.getButton() == MouseButton.PRIMARY) {
						System.out.println("Shape Clicked: "+shape.getId());
					}
				} else {
					System.err.println("Bad Class: "+meh.getSource().getClass().getName());
				}
			});
			
			b.setDefaultExitHandler(meh -> { 
				if(meh.getSource() instanceof Shape) {
					Shape shape = (Shape)meh.getSource();
					shape.setFill(Color.TRANSPARENT);
				}
			});
			
			b.loadXML("testfile", new InputSource(new FileReader(new File("src/stl_out_ungroup.svg"))));
			tux = b.createInstanceFromId("svg2");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage arg0) throws Exception {
		HBox container = new HBox();
		//container.setStyle("-fx-background-color: #000000");
		container.getChildren().addAll(tux);
		Scene s = new Scene(container);
		arg0.setTitle("Test SVG");
		arg0.setScene(s);
		arg0.show();
	}

}