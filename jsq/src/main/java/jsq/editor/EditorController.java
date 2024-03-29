package jsq.editor;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
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
import jsq.Context;
import jsq.CueTypes;
import jsq.command.BulkCommand;
import jsq.command.Command;
import jsq.command.DeleteCue;
import jsq.command.InsertCue;
import jsq.command.UpdateCueActive;
import jsq.command.UpdateCueFollows;
import jsq.command.UpdateCueName;
import jsq.command.UpdateCueNotes;
import jsq.command.UpdateSoundResource;
import jsq.command.UpdateStopped;
import jsq.cue.Cue;
import jsq.cue.PlaySound;
import jsq.cue.Stop;
import jsq.cue.StoppableCue;
import jsq.home.HomeController;
import jsq.project.Resource;
import jsq.stop_selector.StopSelector;


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
	/** Selection model for the cue list. */
	protected MultipleSelectionModel<Cue> _sm;
	/** Dirty bit indicating {@code _cueNotes} has been modified. */
	protected boolean _deltaNotes = false;
	
	/**
	 * Adds a listener to the specified list that disables a menu item when the
	 * list (stacK) is empty.
	 * @param <T> Element type of the stack.
	 * @param stack Stack to observe.
	 * @param target Menu item to enable when the stack is non-empty and disable
	 * when empty.
	 */
	protected <T>
	void CreateStackEmptyListener(ObservableList<T> stack, MenuItem target)
	{
		ListChangeListener<T> lcl = new ListChangeListener<>()
		{
			@Override public void onChanged(Change<? extends T> change)
			{
				if (change.getList().isEmpty()) target.setDisable(true);
				else target.setDisable(false);
			}	
		};
		stack.addListener(new WeakListChangeListener<>(lcl));
	}

	/** JavaFX injectable initialization of the editor's GUI. */
	public void initialize()
	{
		Context.UpdateStageTitle();
		_cueList.setItems(Context.GetCueList());
		_sm = _cueList.getSelectionModel();
		_cueList.setCellFactory(cue_lv -> new CueListCell());
		_sm.setSelectionMode(SelectionMode.MULTIPLE);
		_newCueCombo.getItems().addAll(CueTypes.class.getEnumConstants());
		_newCueCombo.setCellFactory(new_cue_lv -> new NewCueCell());
		_newCueCombo.setButtonCell(new NewCueCell());
		_newCueCombo.getSelectionModel().select(0);

		CreateStackEmptyListener(Context._undoStack, _undo);
		CreateStackEmptyListener(Context._redoStack, _redo);
		CreateStackEmptyListener(Context._clipboard, _paste);

		ListChangeListener<Cue> lcl = new ListChangeListener<>()
		{
			@Override public void onChanged(Change<? extends Cue> change)
			{ UpdateTools(); }
		};
		_sm.getSelectedItems().addListener(new WeakListChangeListener<>(lcl));

		InvalidationListener il = new InvalidationListener()
		{
			@Override public void invalidated(Observable observable)
			{ _deltaNotes = true; }			
		};
		_cueNotes.textProperty().addListener(il);

		// ToDo: Implement the below in a satisfactory way:
		// ChangeListener<Boolean> cl = new ChangeListener<>()
		// {
		// 	@Override public void changed(
		// 		ObservableValue<? extends Boolean> f, Boolean old, Boolean cur)
		// 	{
		// 		if (cur || !_deltaNotes) return;
		// 		UpdateCueNotes();
		// 		_deltaNotes = false;
		// 	}
		// };
		// _cueNotes.focusedProperty().addListener(cl);
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
		int selected_size = _sm.getSelectedIndices().size();

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
			Cue cue = _sm.getSelectedItem();
			int cue_num = _sm.getSelectedIndex() + 1;
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
		for (Cue cue : _sm.getSelectedItems())
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
			if (selected_size == 1)
			{
				// FixMe: Miscast Stop cue when undoing its creation while selected.
				PlaySound cue = (PlaySound) _sm.getSelectedItem();
				_cueSelectedSoundFile.setText(
					Context.GetResourceName(cue._resource));
			}
			else _cueSelectedSoundFile.setText("[Multiple Selected]");
		}
		else if (common.equals(Stop.class))
		{
			_cueType.setText("Stop");
			_editSubTools.getChildren().get(1).setVisible(true);
			if (selected_size == 1)
			{
				Stop cue = (Stop) _sm.getSelectedItem();
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
	// ToDo: Implement flexible cue numbering and replace the 0 here.
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for (StoppableCue cue : targets) pw.format("%d. %s\n", 0, cue._name);
		_cueSelectedStopped.setText(sw.toString());
	}

	/**
	 * Instantiates and applies commands using the provided function for
	 * operations that reflect selections in the cue list. If multiple cues are
	 * selected, {@link jsq.command.BulkCommand} is emitted; otherwise an
	 * instance of {@code C} is emitted.
	 * @param <C> Type of command being created and applied.
	 * @param factory Functional interface to instantiate the command(s).
	 */
	protected <C extends Command>
	void GenerateCommands(BiFunction<Cue, Integer, C> factory)
	{
		Command cmd = null;
		ObservableList<Integer> selected = _sm.getSelectedIndices();
		if (selected.size() == 1)
			cmd = factory.apply(_sm.getSelectedItem(), _sm.getSelectedIndex());
		else
		{
			List<C> cmds = new ArrayList<>(selected.size());
			for (int i : selected) cmds.add(
				factory.apply(_sm.getSelectedItems().get(i), i)
			);
			cmd = new BulkCommand<C>(cmds);
		}
		Context.Apply(cmd);
	}

	/**
	 * Updates the notes of all selected cues to the newly updated value in
	 * {@code _cueNotes}.
	 */
	protected void UpdateCueNotes()
	{
		String new_notes = _cueNotes.getText();
		GenerateCommands(
			(c, i) -> { return new UpdateCueNotes(c, new_notes); }
		);
	}

	/**
	 * Returns the user to the home interface. Prompts the user to verify if the
	 * current is unsaved.
	 */
	@FXML protected void OnFileOpen()
	{
		if (!ConfirmSaved("open a project")) return;
		Context.CleanupResources();
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
	// ToDo: Intercept the event fired when the close button is triggered.
	{
		if (!ConfirmSaved("exit JSQ")) return;
		Context.CleanupResources();
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
		Context.CutCopy(true, _sm.getSelectedIndices());
	}

	/**
	 * Adds the selected cues to the clipboard, leaving them in the cue list.
	 */
	@FXML protected void OnCopy()
	{
		Context.CutCopy(false, _sm.getSelectedIndices());
	}

	/**
	 * Inserts the cues in the clipboard into the cue list after last selected
	 * cue.
	 */
	@FXML protected void OnPaste()
	{
		if (_sm.isEmpty()) Context.Paste(_cueList.getItems().size() - 1);
		else
		{
			ObservableList<Integer> selected = _sm.getSelectedIndices();
			Context.Paste(selected.get(selected.size() - 1) + 1);
		}
	}

	/**
	 * Deletes the selected cues. The cues are deleted in reverse order and each
	 * deletion is performed separately.
	 */
	@FXML protected void OnDelete()
	{
		GenerateCommands(
			(c, i) -> { return new DeleteCue(i, c); }
		);
	}

	/** Selects all elements in the cue list. */
	@FXML protected void OnSelectAll()
	{
		_sm.selectAll();
	}

	/** Deselects all elements in the cue list. */
	@FXML protected void OnUnselectAll()
	{
		_sm.clearSelection();
	}

	/**
	 * Deselects all currently selected elements in the cue list and selects all
	 * other elements.
	 */
	@FXML protected void OnInvertSelection()
	{
		for (int i = 0; i < _cueList.getItems().size(); ++ i)
		{
			if (_sm.isSelected(i)) _sm.clearSelection(i);
			else _sm.select(i);
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
		int destination = (_sm.isEmpty())
			? _cueList.getItems().size()
			: _sm.getSelectedIndices().getLast() + 1;

		Cue cue = _newCueCombo.getValue().CreateCue();
		InsertCue cmd = new InsertCue(destination, cue);
		Context.Apply(cmd);
		_sm.clearAndSelect(destination);
	}

	/**
	 * Updates the names of all selected cues to the newly updated value in
	 * {@code _cueName}.
	 */
	@FXML protected void OnCueNameUpdated()
	{
		String new_name = _cueName.getText();
		GenerateCommands(
			(c, i) -> { return new UpdateCueName(c, new_name); }
		);
		_cueList.refresh();
	}

	/**
	 * Updates the active flag of all selected cues to the newly updated value
	 * in {@code _cueActive}.
	 */
	@FXML protected void OnCueActiveUpdated()
	{
		boolean new_flag = _cueActive.isSelected();
		GenerateCommands(
			(c, i) -> { return new UpdateCueActive(c, new_flag); }
		);
		_cueList.refresh();
	}

	/**
	 * Updates the follows flag of all selected cues to the newly updated value
	 * in {@code _cueFollows}.
	 */
	@FXML protected void OnCueFollowsUpdated()
	{
		boolean new_flag = _cueFollows.isSelected();
		GenerateCommands(
			(c, i) -> { return new UpdateCueFollows(c, new_flag); }
		);
		_cueList.refresh();
	}

	/**
	 * Updates the targetted cues of all selected Stop cues. The user is shown
	 * a dialog to select which cues to target.
	 */
	@FXML protected void OnSelectStopCues()
	{
		List<StoppableCue> old_targets = (_sm.getSelectedItems().size() == 1)
			? ((Stop) _sm.getSelectedItem())._targets
			: null;
		List<StoppableCue> targets = StopSelector.GetSelection(
			_sm.getSelectedIndices().getFirst(), old_targets);
		if (targets == null) return;
		SetSelectedStopText(targets);
		GenerateCommands(
			(c, i) -> { return new UpdateStopped((Stop) c, targets); }
		);
	}

	/**
	 * Updates the sound resources of all selected PlaySound cues. The user is
	 * shown a file selection dialog to select the file.
	 */
	@FXML protected void OnSelectSoundFile()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Sound File");
		chooser.getExtensionFilters().addAll(
			new ExtensionFilter("MP3 Files", "*.mp3"),
			new ExtensionFilter("All Files", "*.*")
		);
		File file = chooser.showOpenDialog(Context._stage);
		if (file == null) return;
		_cueSelectedSoundFile.setText(file.getName());
		Resource res = Context.RegisterSoundResource(file);
		
		GenerateCommands(
			(c, i) -> { return new UpdateSoundResource((PlaySound) c, res); }
		);
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
