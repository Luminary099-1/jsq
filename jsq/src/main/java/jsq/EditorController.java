package jsq;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import jsq.command.BulkCommand;
import jsq.command.Command;
import jsq.command.DeleteCue;
import jsq.command.InsertCue;
import jsq.command.UpdateCueActive;
import jsq.command.UpdateCueFollows;
import jsq.command.UpdateCueName;
import jsq.command.UpdatedStopped;
import jsq.cue.Cue;
import jsq.cue.PlaySound;
import jsq.cue.Stop;
import jsq.cue.StoppableCue;


/** JavaFX controller for the project editor interface. */
public class EditorController
{
	/** Undo menu button. */
	@FXML protected MenuItem _undo;
	/** Redo menu button. */
	@FXML protected MenuItem _redo;
	/** Redo menu button. */
	@FXML protected MenuItem _paste;
	/** Displays the cue list. */
	@FXML protected ListView<Cue> _cueList;
	/** Selector to determine which cue to create. */
	@FXML protected ComboBox<CueTypes> _newCueCombo;
	/** Root node of the cue editing controls. */
	@FXML protected VBox _editTools;
	/** Root node of the cue type dependant editing controls. */
	@FXML protected StackPane _editSubTools;
	/** Displays the most common type of the selected cues. */
	@FXML protected Label _cueType;
	/** Displays the selected cue's number. */
	@FXML protected Label _cueNumber;
	/** Field to modify the names of the selected cues. */
	@FXML protected TextField _cueName;
	/** Field to modify the active flag os the selected cues. */
	@FXML protected CheckBox _cueActive;
	/** Field to modify the follows flag of the selected cues. */
	@FXML protected CheckBox _cueFollows;
	/** Field to modify the notes of the selected cues. */
	@FXML protected TextArea _cueNotes;
	/** Button to enable modification the sound files of selected sound cues. */
	@FXML protected Button _cueSoundFile;
	/** Displays the sound file of the selected sound cues. */
	@FXML protected TextField _cueSelectedSoundFile;
	/** Button to enable modification of targets for selected stop cues. */
	@FXML protected Button _cueSelectStopped;
	/** Displays the targets of the selected stop cues. */
	@FXML protected TextArea _cueSelectedStopped;
	
	/** JavaFX injectable initialization of the editor's GUI. */
	public void initialize()
	{
		Context._stage.setTitle("JSQ: New Project");
		new StackEmptyListener<>(Context._undoStack, _undo);
		new StackEmptyListener<>(Context._redoStack, _redo);
		new StackEmptyListener<>(Context._clipboard, _paste);
		_cueList.setItems(Context.GetCueList());
		_cueList.setCellFactory(cue_lv -> new CueListCell());
		_cueList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		_newCueCombo.getItems().addAll(CueTypes.class.getEnumConstants());
		_newCueCombo.setCellFactory(new_cue_lv -> new NewCueCell());
		_newCueCombo.setButtonCell(new NewCueCell());
		_newCueCombo.getSelectionModel().select(0);

		_cueList.getSelectionModel().getSelectedItems().addListener(
			new ListChangeListener<Cue>()
			{
				@Override public void onChanged(Change<? extends Cue> arg0)
				{ UpdateTools(); }
			}
		);
	}

	/**
	 * Configures the cue editing controls to reflect the selection in the cue
	 * list. Controls will be populated with the values of the selected cue.
	 * Controls will be put into an indeterminate state if multiple cues are
	 * selected. If cues of different types are selected, only the common
	 * controls will be enabled.
	 */
	protected void UpdateTools()
	// ToDo: Refactor this mess.
	{
		int selected_size
			= _cueList.getSelectionModel().getSelectedIndices().size();

		_editSubTools.getChildren().forEach(c -> c.setVisible(false));
		_cueActive.setIndeterminate(false);
		_cueFollows.setIndeterminate(false);
		if (selected_size == 0)
		{
			_editTools.setDisable(true);
			_cueType.setText(null);
			_cueNumber.setText(null);
			_cueName.setText(null);
			_cueActive.setSelected(false);
			_cueFollows.setSelected(false);
			_cueNotes.setText(null);
			return;
		}
		_editTools.setDisable(false);
		if (selected_size == 1)
		{
			Cue cue = _cueList.getSelectionModel().getSelectedItem();
			int cue_num = _cueList.getSelectionModel().getSelectedIndex() + 1;
			_cueNumber.setText(Integer.toString(cue_num));
			_cueName.setText(cue._name);
			_cueActive.setSelected(cue._active);
			_cueFollows.setSelected(cue._follows);
			_cueNotes.setText(cue._notes);
		}
		else
		{
			_cueNumber.setText("###");
			_cueName.setText("[Multiple Selected]");
			_cueActive.setIndeterminate(true);
			_cueFollows.setIndeterminate(true);
			_cueNotes.setText("[Multiple Selected]");
		}

		Class common = null;
		for (Cue cue : _cueList.getSelectionModel().getSelectedItems())
		{
			if (common == null || common.equals(cue.getClass()))
			{
				common = cue.getClass();
				continue;
			}
			common = Cue.class;
			break;
		}

		if (common.equals(PlaySound.class))
		{
			_cueType.setText("Play Sound");
			_editSubTools.getChildren().get(0).setVisible(true);
			// ToDo: Populate these controls.
		}
		else if (common.equals(Stop.class))
		{
			_cueType.setText("Stop");
			_editSubTools.getChildren().get(1).setVisible(true);
			if (selected_size == 1)
			{
				Stop cue = (Stop) _cueList.getSelectionModel().getSelectedItem();
				SetSelectedStopText(cue._targets);
			}
			else _cueSelectedStopped.setText("[Multiple Selected]");
		}
		else _cueType.setText("Cue");
	}

	/**
	 * Shows the user a dialog indicating an error occurred and displays the
	 * associated stacktrace.
	 * @param title Title for the dialog.
	 * @param message Explains what operation failed/what went wrong.
	 * @param e The offending exception.
	 */
	protected void ExceptionDialog(String title, String message, Exception e)
	{
		e.printStackTrace();
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(message);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String e_text = sw.toString();
		
		TextArea ta = new TextArea(e_text);
		ta.setEditable(false);
		ta.setWrapText(true);

		alert.getDialogPane().setExpandableContent(ta);
		alert.showAndWait();
	}

	/**
	 * Returns {@code true} if the current project is saved or the user verifies
	 * they are willing to lose unsaved changes.
	 * @param action Description of the action being undertaken that would cause
	 * the project to be reset.
	 * @return {@code true} if the potentially harmful action can proceed;
	 * {@code false} otherwise.
	 */
	protected boolean ConfirmSaved(String action)
	{
		// ToDo: Add the option to save the current file.
		if (Context.IsSaved()) return true;

		// ToDo: Ideally the cancel button should have focus by default.
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Warning: There are unsaved changes.");
		alert.setHeaderText(null);
		alert.setContentText(String.format(
			"Are you sure you want to %s without saving?", action));

		if (alert.showAndWait().get() == ButtonType.OK) return true;
		else return false;
	}

	/**
	 * Sets the text of {@code _cueSelectedStopped} to display the cues stored
	 * in the past list.
	 * @param targets List of cues to display. 
	 */
	protected void SetSelectedStopText(Iterable<StoppableCue> targets)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for (StoppableCue cue : targets)
			pw.format("%d. %s\n", 0, cue._name);
		_cueSelectedStopped.setText(sw.toString());
	}

	/**
	 * Returns the user to the home interface. Prompts the user to verify if the
	 * current is unsaved.
	 */
	@FXML protected void OnFileOpen()
	{
		if (!ConfirmSaved("open a project")) return;
		Context.SwitchScene(HomeController.class.getResource("home.fxml"));		
	}

	/** Saves the current project to its workspace folder. */
	@FXML protected void OnFileSave()
	{
		try { Context.Save(); }
		catch (Exception e)
		{
			ExceptionDialog("Error", 
				"An error prevented the project from being saved.", e);
		}
	}

	/** */
	@FXML protected void OnFileRevert()
	{
		// ToDo:
	}

	/** */
	@FXML protected void OnFilePreferences()
	{
		// ToDo:
	}

	/**
	 * Exits the application. Prompts the user to verify if the current project
	 * is project is unsaved.
	 */
	@FXML protected void OnFileQuit() throws Exception
	{
		if (!ConfirmSaved("exit JSQ")) return;
		Platform.exit();
	}

	/** Undoes the last applied operation. */
	@FXML protected void OnUndo()
	{
		Context.Undo();
	}

	/** Redoes the last undone operation. */
	@FXML protected void OnRedo()
	{
		Context.Redo();
	}

	/**
	 * Adds the selected cues to the clipboard and delete them from the cue
	 * list.
	 */
	@FXML protected void OnCut()
	{
		Context.CutCopy(
			true, _cueList.getSelectionModel().getSelectedIndices());
	}

	/**
	 * Adds the selected cues to the clipboard, leaving them in the cue list.
	 */
	@FXML protected void OnCopy()
	{
		Context.CutCopy(
			false, _cueList.getSelectionModel().getSelectedIndices());
	}

	/**
	 * Inserts the cues in the clipboard into the cue list after last selected
	 * cue.
	 */
	@FXML protected void OnPaste()
	{
		MultipleSelectionModel<Cue> sm = _cueList.getSelectionModel();
		if (sm.isEmpty()) Context.Paste(_cueList.getItems().size() - 1);
		else
		{
			ObservableList<Integer> selected = sm.getSelectedIndices();
			Context.Paste(selected.get(selected.size() - 1));
		}
	}

	/**
	 * Deletes the selected cues. The cues are deleted in reverse order and each
	 * deletion is performed separately.
	 */
	@FXML protected void OnDelete()
	{
		MultipleSelectionModel<Cue> sm = _cueList.getSelectionModel();
		ObservableList<Integer> selected = sm.getSelectedIndices();
		
		for (int i = selected.size() - 1; i >= 0; -- i)
		{
			DeleteCue command = new DeleteCue(selected.get(i));
			Context.Apply(command);
		}

		sm.clearSelection();
	}

	/** Selects all elements in the cue list. */
	@FXML protected void OnSelectAll()
	{
		_cueList.getSelectionModel().selectAll();
	}

	/** Deselects all elements in the cue list. */
	@FXML protected void OnUnselectAll()
	{
		_cueList.getSelectionModel().clearSelection();
	}

	/**
	 * Deselects all currently selected elements in the cue list and selects all
	 * other elements.
	 */
	@FXML protected void OnInvertSelection()
	{
		MultipleSelectionModel<Cue> sm = _cueList.getSelectionModel();
		for (int i = 0; i < _cueList.getItems().size(); ++ i)
		{
			if (sm.isSelected(i)) sm.clearSelection(i);
			else sm.select(i);
		}
	}

	/** */
	@FXML protected void OnHelpAbout()
	{
		// ToDo:
	}

	/**
	 * Creates a new cue of the type specified by {@code _newCueCombo}. The new
	 * cue is inserted after the last currently selected cue. If no cues are
	 * selected, or there are no cues in the list, the new cue is inserted at
	 * the end of the list.
	 * After it's inserted, all other cues are deselected and the new cue is
	 * selected.
	 */
	@FXML protected void OnCreate()
	{
		MultipleSelectionModel<Cue> sm = _cueList.getSelectionModel();
		int num_cues = _cueList.getItems().size();
		int destination = 0;

		if (num_cues != 0)
		{
			if (sm.isEmpty()) destination = num_cues - 1;
			else
			{
				ObservableList<Integer> selected = sm.getSelectedIndices();
				destination = selected.get(selected.size() - 1) + 1;
			}
		}

		Cue cue = _newCueCombo.getValue().CreateCue();
		InsertCue command = new InsertCue(destination, cue);
		Context.Apply(command);
		sm.clearAndSelect(destination);
	}

	// ToDo: Modularize the following update methods?

	/**
	 * Updates the names of all selected cues to the newly updated value in
	 * {@code _cueName}.
	 */
	@FXML protected void OnCueNameUpdated()
	{
		String new_name = _cueName.getText();
		Command command = null;
		ObservableList<Cue> selected
			= _cueList.getSelectionModel().getSelectedItems();

		if (selected.size() == 1)
			command = new UpdateCueName(selected.get(0), new_name);
		else
		{
			UpdateCueName commands[] = new UpdateCueName[selected.size()];
			for (int i = 0; i < selected.size(); ++ i)
				commands[i] = new UpdateCueName(selected.get(i), new_name);
			command = new BulkCommand<UpdateCueName>(commands);
		}
		Context.Apply(command);
		_cueList.refresh();
	}

	/**
	 * Updates the active flag of all selected cues to the newly updated value
	 * in {@code _cueActive}.
	 */
	@FXML protected void OnCueActiveUpdated()
	{
		boolean new_flag = _cueActive.isSelected();
		Command command = null;
		ObservableList<Cue> selected
			= _cueList.getSelectionModel().getSelectedItems();

		if (selected.size() == 1)
			command = new UpdateCueActive(selected.get(0), new_flag);
		else
		{
			UpdateCueActive commands[] = new UpdateCueActive[selected.size()];
			for (int i = 0; i < selected.size(); ++ i)
				commands[i] = new UpdateCueActive(selected.get(i), new_flag);
			command = new BulkCommand<UpdateCueActive>(commands);
		}
		Context.Apply(command);
		_cueList.refresh();
	}

	/**
	 * Updates the follows flag of all selected cues to the newly updated value
	 * in {@code _cueFollows}.
	 */
	@FXML protected void OnCueFollowsUpdated()
	{
		boolean new_flag = _cueFollows.isSelected();
		Command command = null;
		ObservableList<Cue> selected
			= _cueList.getSelectionModel().getSelectedItems();

		if (selected.size() == 1)
			command = new UpdateCueFollows(selected.get(0), new_flag);
		else
		{
			UpdateCueFollows commands[] = new UpdateCueFollows[selected.size()];
			for (int i = 0; i < selected.size(); ++ i)
				commands[i] = new UpdateCueFollows(selected.get(i), new_flag);
			command = new BulkCommand<UpdateCueFollows>(commands);
		}
		Context.Apply(command);
		_cueList.refresh();
	}

	// ToDo: Implement handler to update cue notes.

	/**
	 * Updates the targetted cues of all selected Stop cues. The user is shown
	 * a dialog to select which cues to target.
	 */
	@FXML protected void OnSelectStopCues()
	{
		MultipleSelectionModel<Cue> sm = _cueList.getSelectionModel();
		ObservableList<StoppableCue> targets
			= StopSelector.GetSelection(sm.getSelectedIndices().get(0));
		if (targets == null) return;
		SetSelectedStopText(targets);

		Command command = null;
		int selected_size = sm.getSelectedItems().size();
		if (selected_size == 1)
			command = new UpdatedStopped((Stop) sm.getSelectedItem(), targets);
		else
		{
			ObservableList<Cue> selected = sm.getSelectedItems();
			UpdatedStopped commands[] = new UpdatedStopped[selected_size];
			for (int i = 0; i < selected_size; ++ i) commands[i]
				= new UpdatedStopped((Stop) selected.get(i), targets);
			command = new BulkCommand<UpdatedStopped>(commands);
		}
		Context.Apply(command);
	}

	/**  */
	@FXML protected void OnSelectSoundFile()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Sound File");
		chooser.getExtensionFilters().addAll(
			new ExtensionFilter("MP3 Files", "*.mp3"),
			new ExtensionFilter("All Files", "*.*")
		);
		File file = chooser.showOpenDialog(Context._stage);

		// ToDo: Complete this.
	}

	/** */
	@FXML protected void OnBack()
	{
		// ToDo:
	}

	/** */
	@FXML protected void OnGo()
	{
		// ToDo:
	}
}


/** Listener to disable {@code MenuItem}s when observed lists are empty. */
class StackEmptyListener<T> implements ListChangeListener<T>
{
	/** The menu item to be disabled when the stack is empty. */
	protected MenuItem _target;

	/**
	 * Creates a listener to disable a menu item when a list is empty.
	 * @param list The list to listen to.
	 * @param target The item to be disabled when the list is empty.
	 */
	public StackEmptyListener(ObservableList<T> list, MenuItem target)
	{
		_target = target;
		list.addListener(this);
	}

	/** When the list is updated, disables _target if the list is empty. */
	@Override public void onChanged(Change<? extends T> change)
	{
		if (change.getList().isEmpty()) _target.setDisable(true);
		else _target.setDisable(false);
	}
}
