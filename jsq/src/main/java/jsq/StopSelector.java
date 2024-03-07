package jsq;

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
import jsq.cue.Cue;
import jsq.cue.StoppableCue;


/** Modal to select cues to set the target of a {@link jsq.cue.Stop}. */
public class StopSelector extends Stage
// ToDo: Refactor out the need for _accepted.
{	
	/** Cue list for the selection of stop targets. */
	@FXML protected ListView<Cue> _cueList;
	/** Cue list index of the earliest cue to be assigned these targets. */
	protected int _stoppingIndex;
	/** Indicates if the user accepted their selection. */
	protected boolean _accepted = false;
	/** To store the selection for reterival by the calling code. */
	protected ObservableList<StoppableCue> _selection;

	/** JavaFX injectable initialization of the editor GUI. */
	public void initialize()
	{
		_cueList.setItems(Context.GetCueList());
		_cueList.setCellFactory(
			cue_lv -> new SelectStopListCell(_stoppingIndex));
		_cueList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	/**
	 * Creates a new modal to select stop cue targets.
	 * @param stopping_index Cue list index of the first stop cue whose targets
	 * are to be selected.
	 */
	public StopSelector(int stopping_index)
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
	 * @return {@code true} if the user accepted their selection; {@code false}
	 * otherwise.
	 */
	public boolean Accepted()
	{
		return _accepted;
	}

	/**
	 * @return The selection of cues to stop made by the user.
	 */
	public ObservableList<StoppableCue> Selection()
	{
		return _selection;
	}

	/** Mark the dialog as accepted and store the user's selection. */
	@FXML protected void OnAccpet()
	{
		_accepted = true;
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
