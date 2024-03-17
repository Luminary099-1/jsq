package jsq.stop_selector;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jsq.Context;
import jsq.cue.Cue;
import jsq.cue.StoppableCue;


/** Modal to select cues to set the target of a {@link jsq.cue.Stop}. */
public class StopSelector extends Stage
// ToDo: Preselect the stop cue's already selected cues (for a single stop cue selection).
{	
	/** Cue list for the selection of stop targets. */
	@FXML protected ListView<Cue> _cueList;
	/** Cue list index of the earliest cue to be assigned these targets. */
	protected int _stoppingIndex;
	/** To store the selection for reterival by the calling code. */
	protected ObservableList<StoppableCue> _selection = null;

	/** JavaFX injectable initialization of the modal's GUI. */
	public void initialize()
	{
		_cueList.setItems(Context.GetCueList());
		_cueList.setCellFactory(
			cue_lv -> new StopSelectListCell(_stoppingIndex));
		_cueList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	/**
	 * Creates a new modal to select stop cue targets.
	 * @param stopping_index The index of the first stop cue to target.
	 */
	private StopSelector(int stopping_index)
	{
		super();
		_stoppingIndex = stopping_index;
		initModality(Modality.APPLICATION_MODAL);
		FXMLLoader loader
			= new FXMLLoader(getClass().getResource("stopSelector.fxml"));
		loader.setController(this);
		try
		{
			Parent root_node = loader.load();
			setScene(new Scene(root_node, 1280, 720));
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	/**
	 * Prompt the user to select the targets for stop cues.
	 * @param stopping_index The index of the first stop cue to target.
	 * @return The cues the user selected to stop. If null, the user did not
	 * make a selection.
	 */
	public static ObservableList<StoppableCue> GetSelection(int stopping_index)
	{
		StopSelector selector = new StopSelector(stopping_index);
		selector.showAndWait();
		return selector._selection;
	}

	/** Store the user's selection. */
	@FXML protected void OnAccpet()
	{
		_selection = FXCollections.observableArrayList();
		for (Cue cue : _cueList.getSelectionModel().getSelectedItems())
			_selection.add((StoppableCue) cue);
		close();
	}

	/** Close the dialog without making a selection. */
	@FXML protected void OnCancel()
	{
		close();
	}
}
