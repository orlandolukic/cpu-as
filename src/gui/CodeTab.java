package gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretListener;
import javax.swing.text.*;

import checkings.KeyListener;
import controller.MainScreenController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.w3c.dom.Text;
import start.AppJTextPane;
import utils.LinePainter;
import utils.RXTextUtilities;
import utils.Regex;
import utils.Styles;

import static controller.MainScreenController.CurrentTab;
import static java.lang.Thread.sleep;

public class CodeTab {

	private static LinkedList<CodeTab> tabs;

	static {
		tabs = new LinkedList<>();
	}

	public static CodeTab getCodeTabByIndex(int index) {
		Iterator<CodeTab> it = tabs.iterator();
		CodeTab t;
		while( it.hasNext() )
		{
			t = it.next();
			if ( t.TabIndex == index )
				return t;
		};
		return null;
	}

	static public int Counter = 0;
	public int TabID;
	public int TabIndex;
	public Tab NewTab;
	public JTextPane TextPane;
	public boolean FileSaved;
	public JScrollPane ScrollPane;
	public File TextFile;
	public TextLineNumber TextLineNumber;
	public AppDefaultStyledDocument StyleDocument;
	public LinePainter LinePainter;
	public int hash;
	public String text;
	public String outputText;
	public CaretListener[] CaretListeners;
	public int CheckedTimes;

	
	public CodeTab(String TabName){
		TabID = Counter++;
		FileSaved = false;
		TextFile = null;
		outputText = null;
		CheckedTimes = 0;
		
		// VBox inside the Tab
		VBox TextLayout = new VBox();
		
		this.StyleDocument = new AppDefaultStyledDocument();
		TextPane = new AppJTextPane(StyleDocument);
		LinePainter = new LinePainter(TextPane, Styles.colorLineBackground);
		LinePainter.setColor(Styles.colorLineBackground);
		StyleDocument.setTextPane(TextPane);
		setTabs(TextPane, 8);
		
		TextLineNumber tln = new TextLineNumber(TextPane);	
		TextLineNumber = tln;

		
		ScrollPane = new JScrollPane(TextPane);
		ScrollPane.setRowHeaderView(tln);     
		ScrollPane.setPreferredSize(new Dimension(200,400));
		//ScrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		ScrollPane.repaint();
		MainScreenController.getInstance().OutputTextArea.clear();
		
		SwingNode sn = new SwingNode();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				sn.setContent(ScrollPane);
			}
		});
		
		TextLayout.setPadding(new Insets(5));
		TextLayout.getChildren().addAll(sn);
		TextLayout.setAlignment(Pos.CENTER);
		
		NewTab = new Tab(TabName, TextLayout);
		NewTab.setStyle("-fx-focus-traversable: false;");
		NewTab.setOnClosed(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				if ( NewTab.isSelected() )
					OnClosed();
			}
		});
		
		// Set font of the editor.
		Font f = new Font("Courier New", Font.PLAIN, 16);
		TextPane.setFont(f);

		NewTab.setOnSelectionChanged(new EventHandler<Event>() {
			
			@Override
			public void handle(Event event) {

				if ( DebugPopUpWindow.IS_DEBUGGING ) {
					event.consume();

					return;
				}

				if ( CurrentTab != null && CurrentTab.TabID == TabID )
				{
					OnBeforeTabChanged();
				}

				if(CurrentTab != null && CurrentTab.TabID != TabID) {
					OnTabChanged();
				}
			}
		});

		TextPane.addKeyListener( new KeyListener() );
		TextLineNumber.setToRunOnRowClick( MainScreenController.getInstance().DebugSection.ConsumerOnLineClick );

	}

	public void setTabIndex( int index )
	{
		TabIndex = index;
		tabs.add(this);
	}
	
	public void OnClosed() {
		
		ObservableList<Tab> o = MainScreenController.getInstance().CodeTabPane.getTabs();
		if ( o.size() == 0 )
		{
			CurrentTab = null;
			MainScreenController.getInstance().setDisableActionButtons(true);
			MainScreenController.getInstance().OutputTextArea.clear();
		} else
		{
			try {
				CurrentTab.NewTab.getTabPane().getSelectionModel().select(TabIndex - 1);
				CurrentTab = getCodeTabByIndex(TabIndex - 1);
			} catch( Exception e ) {}
		};
	}

	public void OnTabChanged() {
		CurrentTab = this;
		MainScreenController.RefreshScrollPane(50);
		this.TextPane.setEnabled(true);
		TextArea ta = MainScreenController.getInstance().OutputTextArea;
		ta.setText(this.outputText);
		Thread t = new Thread(() -> {
			try {
				sleep(100);
			} catch( InterruptedException e ) {}
			Platform.runLater(() -> {
				ta.selectPositionCaret(ta.getLength());
				ta.deselect();
			});
		});
		t.start();
	}

	public void OnBeforeTabChanged()
	{
		CurrentTab.outputText = MainScreenController.getInstance().OutputTextArea.getText();
	}
	
	// CTRL + S
	public void OnControlS() {
		MainScreenController.SaveBtn.getOnMouseClicked().handle(null);
	}
	
	public static void setTabs( final JTextPane textPane, int charactersPerTab)
    {
        FontMetrics fm = textPane.getFontMetrics( textPane.getFont() );
//          int charWidth = fm.charWidth( 'w' );
        int charWidth = fm.charWidth( ' ' );
        int tabWidth = charWidth * charactersPerTab;
//      int tabWidth = 100;

        TabStop[] tabs = new TabStop[5];

        for (int j = 0; j < tabs.length; j++)
        {
            int tab = j + 1;
            tabs[j] = new TabStop( tab * tabWidth );
        }

        TabSet tabSet = new TabSet(tabs);
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setTabSet(attributes, tabSet);
        int length = textPane.getDocument().getLength();
        textPane.getStyledDocument().setParagraphAttributes(0, length, attributes, false);
    }

    public void setCaretListeners()
	{
		CaretListeners = TextPane.getCaretListeners();
	}

    public void removeAllCaretListeners()
	{
		setCaretListeners();
		for (int i=0; i<CaretListeners.length; i++)
			TextPane.removeCaretListener(CaretListeners[i]);
	}

	public void restoreAllCaretListeners()
	{
		for (int i=0; i<CaretListeners.length; i++)
			TextPane.addCaretListener(CaretListeners[i]);
	}
	
}
