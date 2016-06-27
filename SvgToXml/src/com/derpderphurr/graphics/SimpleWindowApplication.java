
package com.derpderphurr.graphics;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class SimpleWindowApplication extends Application {

	protected Node tux;
	
	public SimpleWindowApplication() {
		super();
		
		SvgToFXBuilder b = new SvgToFXBuilder();
		try {
			b.loadXML("testfile", new InputSource(SvgToFXBuilder.class.getResourceAsStream("/gradient.svg")));
			tux = b.createInstanceFromId("rect2");
		} catch (ParserConfigurationException e) {
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

package com.derpderphurr.graphics;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class SimpleWindowApplication extends Application {

	protected Node tux;
	
	public SimpleWindowApplication() {
		super();
		
		SvgToFXBuilder b = new SvgToFXBuilder();
		try {
			b.loadXML("testfile", new InputSource(SvgToFXBuilder.class.getResourceAsStream("/gradient.svg")));
			tux = b.createInstanceFromId("rect2");
		} catch (ParserConfigurationException e) {
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

package com.derpderphurr.graphics;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class SimpleWindowApplication extends Application {

	protected Node tux;
	
	public SimpleWindowApplication() {
		super();
		
		SvgToFXBuilder b = new SvgToFXBuilder();
		try {
			b.loadXML("testfile", new InputSource(SvgToFXBuilder.class.getResourceAsStream("/gradient.svg")));
			tux = b.createInstanceFromId("rect2");
		} catch (ParserConfigurationException e) {
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
