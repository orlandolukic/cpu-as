package gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextPane;

import controller.MainScreenController;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import utils.Regex;

import static java.lang.Thread.sleep;

public class KeyCombinationRunnables {
	
	public static Runnable CTRLS = new Runnable() {
		@Override
		public void run() {
			if ( DebugPopUpWindow.IS_DEBUGGING )
				return;

			if ( MainScreenController.getInstance().CurrentTab != null ) {
				MainScreenController.getInstance().OutputTextArea.appendText("Saving file...\n");
				boolean saved = MainScreenController.SaveFunction(MainScreenController.getInstance().CurrentTab.TextFile);
				if ( saved )
					MainScreenController.getInstance().OutputTextArea.appendText("File successfully saved\n");
			};
		}
	};

	public static Runnable CTRLV = new Runnable() {
		@Override
		public void run() {
			if ( DebugPopUpWindow.IS_DEBUGGING )
				return;

			if ( MainScreenController.getInstance().CurrentTab != null ) {
				Thread t = new Thread( () -> {
					try {
						sleep(120);
					} catch( InterruptedException e ) {}
					Platform.runLater(() -> {
						MainScreenController.CurrentTab.StyleDocument.highlightAll();
					});
				} );
				t.start();
			};
		}
	};

	public static Runnable CTRLO = new Runnable() {
		@Override
		public void run() {
			if ( DebugPopUpWindow.IS_DEBUGGING )
				return;
			MainScreenController.OpenFileBtn.getOnAction().handle(null);
		}
	};

	public static Runnable CTRLQ = new Runnable() {
		@Override
		public void run() {
			if ( DebugPopUpWindow.IS_DEBUGGING )
				return;
			if ( !MainScreenController.AssembleBtn.isDisabled() )
				MainScreenController.AssembleBtn.getOnAction().handle(null);
		}
	};

	public static Runnable CTRLN = new Runnable() {
		@Override
		public void run() {
			if ( DebugPopUpWindow.IS_DEBUGGING )
				return;
			MainScreenController.NewFileBtn.getOnAction().handle(null);
		}
	};

	public static Runnable CTRLD = new Runnable() {
		@Override
		public void run() {

			if ( DebugPopUpWindow.IS_DEBUGGING )
				return;

			if ( !MainScreenController.DebugBtn.isDisabled() )
				MainScreenController.DebugBtn.getOnAction().handle(null);
		}
	};

	public static Runnable F6 = new Runnable() {
		@Override
		public void run() {
			Button b = MainScreenController.getInstance().DebugSection.StepBtn;
			if ( DebugPopUpWindow.IS_DEBUGGING && !b.isDisabled() ) {
				b.getOnAction().handle(null);
			}
		}
	};

	public static Runnable F7 = new Runnable() {
		@Override
		public void run() {
			Button b = MainScreenController.getInstance().DebugSection.ContinueBtn;
			if ( DebugPopUpWindow.IS_DEBUGGING && !b.isDisabled() ) {
				b.getOnAction().handle(null);
			}
		}
	};

	public static Runnable F8 = new Runnable() {
		@Override
		public void run() {
			if ( DebugPopUpWindow.IS_DEBUGGING ) {
				MainScreenController.getInstance().DebugSection.ResetBtn.getOnAction().handle(null);
			}
		}
	};

	public static Runnable F9 = new Runnable() {
		@Override
		public void run() {
			if ( DebugPopUpWindow.IS_DEBUGGING ) {
				MainScreenController.getInstance().DebugSection.StopBtn.getOnAction().handle(null);
			}
		}
	};
}
