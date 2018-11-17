/**
 * 
 */
package comp3111.webscraper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.stream.Collectors;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * 
 * @author kevinw
 *
 *
 * Controller class that manage GUI interaction. Please see document about JavaFX for details.
 * 
 */
public class Controller {

    @FXML 
    private Label labelCount; 

    @FXML 
    private Label labelPrice; 

    @FXML 
    private Hyperlink labelMin; 

    @FXML 
    private Hyperlink labelLatest; 

    @FXML
    private TextField textFieldKeyword;
    
    @FXML
    private TextArea textAreaConsole;
    
    private WebScraper scraper;
    
    @FXML 
    private TableView<Item> itemTable;
    
    @FXML 
    private TableColumn<Item, String> itemTitle;
    
    @FXML 
    private TableColumn<Item, Double> itemPrice;
    
    @FXML 
    private TableColumn<Item, Hyperlink> itemUrl;

    @FXML 
    private TableColumn<Item, Date> itemPosted;
    
    @FXML
    private Button refineButton;
    
    @FXML
    private MenuItem lastSearchMenuItem;

    @FXML
    private MenuItem quitMenuItem;
    
    @FXML
    private MenuItem closeMenuItem;
    
    private List<Item> searchRecordCache;
    private List<Item> searchRecord;

    /**
     * Default controller
     */
    public Controller() {
    	scraper = new WebScraper();
    	searchRecordCache = new Vector<Item>();
    	searchRecord = new Vector<Item>();
    }

    /**
     * Default initializer. It is empty.
     */
    @FXML
    private void initialize() {
    	itemTitle.setCellValueFactory(new PropertyValueFactory<Item, String>("title"));
    	itemPrice.setCellValueFactory(new PropertyValueFactory<Item, Double>("price"));
    	itemUrl.setCellValueFactory(new PropertyValueFactory<Item, Hyperlink>("url"));
    	itemPosted.setCellValueFactory(new PropertyValueFactory<Item, Date>("itemDate"));
    }
    
    /**
     * Called when the search button is pressed.
     */
    @FXML
    private void actionSearch() {
    	
    	System.out.println("actionSearch: " + textFieldKeyword.getText());
    	
    	searchRecordCache.clear();
    	searchRecordCache = searchRecord.stream().collect(Collectors.toList());
    	searchRecord = scraper.scrape(textFieldKeyword.getText());
    	
    	updateTabs(searchRecord);
    	
    	refineButton.setDisable(false);
    	lastSearchMenuItem.setDisable(false);
    }
    
    /**
     * 
     * 
     * @param items 
     */
    private void updateTabs(List<Item> items) {
    	
    	// Update the console tab
    	String output = "";
    	for (Item item : items) {
    		output += item.getTitle() + "\t" + item.getPrice() + "\t" + item.getUrl() + "\t" + item.getItemDate() + "\t" + item.getSite() + "\n";
    	}
    	textAreaConsole.setText(output);
    	
    	
    	double a = 0.0;
    	int no_of_nonzero_items = 0;
    	double lowest_price = 0.0;
    	
    	//Calculates total sum of prices and no of items.
    	for (Item item : items) {
    		if (item.getPrice() != 0.0 && item.getPrice() > 0.0) {
    			a = a + item.getPrice();
    			no_of_nonzero_items++;
    		}
    	}
    	
    	//calculates hyperlink smallest non zero price
    	List<Item> SortedResult = items.stream().sorted(Comparator.comparing(Item::getPrice)).collect(Collectors.toList());
    	for (Item item : SortedResult) {
    		if(item.getPrice() > 0.0) {
    			labelMin.setText(String.valueOf(item.getUrl()));
    			lowest_price = item.getPrice();

    			break;
    		} else {
    			labelMin.setText("-");
    		}
    	}
    	labelMin.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent e) {
    	        System.out.println("This link is clicked");
    	    }
    	});
    	//Hyperlink for latest Item
    	for (Item item : items) {
    		labelLatest.setText(String.valueOf(item.getUrl()));
    		break;
    		
    	}
       	labelLatest.setOnAction(new EventHandler<ActionEvent>() {
    	    @Override
    	    public void handle(ActionEvent e) {
    	        System.out.println("This link is clicked");
    	    }
    	});
       	
    	labelCount.setText(String.valueOf(items.size()));

    	//calculates the average price
    	double average_price = a/no_of_nonzero_items;
    	System.out.println("Average Price:");
    	System.out.println(average_price);
    	
    	labelPrice.setText(String.valueOf(average_price));
    	
    	// Update the Table tab
    	ObservableList<Item> ObservableItems = FXCollections.observableList(items);
    	itemTable.setItems(ObservableItems);
    }
    
//    /**
//     * Changing to Hyperlink
//     */
//    private void addLink(final String url) {
//        final Hyperlink link = new Hyperlink(url);
//        link.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent t) {
//                getHostServices().showDocument(link.getText());
//                //openBrowser(link.getText());
//            }
//
//        });
//        itemTable.getItems().addAll(link);
//    }
    
    
    
    /**
     * Called when the refine button is pressed.  Filters the searched data and keeps the 
     * items with their titles containing the keyword typed in the text area.
     */
    @FXML
    private void actionRefine() {
    	
    	String keyword = textFieldKeyword.getText().toLowerCase();
    	List<Item> refinedSearchRecord = new Vector<Item>();
    	
    	for(Item item : searchRecord) {
    		if(item.getTitle().toLowerCase().contains(keyword)) {
    			refinedSearchRecord.add(item);
    		}
    	}
    	
    	updateTabs(refinedSearchRecord);
    	refineButton.setDisable(true);
    }
    
    /**
     * Called when the about your team button is pressed. Makes a dialog 
     * displaying information about the team members appear.
     */
    @FXML
    private void actionAbout() {
    	final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("Names: Ruben Wijkmark, Kaushal Kalyanasundaram, Kenny Li"));
        dialogVbox.getChildren().add(new Text("ITSC: rwijkmark, kkac, klian"));
        dialogVbox.getChildren().add(new Text("Github: RubenWijkmark, kaushalkalyan, kli283"));
        dialog.setTitle("About");
        dialog.setResizable(false);
        Scene dialogScene = new Scene(dialogVbox, 330, 100);
        dialog.setScene(dialogScene);
        dialog.show();
    }
    
    /**
     * Called when the last search button is pressed. Reverts the search results
     * to the previous search.
     */
    @FXML
    private void actionLast() {
    	lastSearchMenuItem.setDisable(true);
    	updateTabs(searchRecordCache);
    }
    
    /**
     * Called when the quit button is pressed. Exits the program and 
     * closes all connections.
     */
    @FXML
    private void actionQuit() {
    	// todo
    }
    
    /**
     * Called when the close button is pressed. Clears the current search record and 
     * initialize all tabs to their initial state.
     */
    @FXML
    private void actionClose() {
    	// todo
    }
    
}

