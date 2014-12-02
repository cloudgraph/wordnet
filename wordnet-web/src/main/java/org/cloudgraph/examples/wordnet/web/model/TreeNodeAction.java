package org.cloudgraph.examples.wordnet.web.model;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.AjaxBehaviorEvent;

import org.cloudgraph.examples.wordnet.web.ResourceManager;
import org.cloudgraph.examples.wordnet.web.ResourceType;

public class TreeNodeAction {

	private Action action;
	private boolean selected;
	private boolean enabled;
	private TreeSelectionModel selectionModel;
	private ActionHandler actionHandler;
	
	@SuppressWarnings("unused")
	private TreeNodeAction() {}
	
	public TreeNodeAction(Action action, ActionHandler actionHandler, TreeSelectionModel selectionModel) {
		this.action = action;
		this.actionHandler = actionHandler;
		this.selectionModel = selectionModel;
	}	

	public boolean isSelected() {
		return selected;
	}

	public String select() {
		this.selected = true;
		return null;
	}
	
	public String deselect() {
		this.selected = false;
		return null;
	}

	public String toggle() {
		if (this.selected)
			this.selected = false;
		else
			this.selected = true;
		return null;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String onAction() {
		selectionModel.clearSelection();
		selectionModel.setSelection(this);
		this.selected = true;
		return actionHandler.handleAction(this.action);
	}
	
	public void onAction(AjaxBehaviorEvent event) {
		selectionModel.clearSelection();
		selectionModel.setSelection(this);
		this.selected = true;
		actionHandler.handleAction(this.action);
	}
	
	public void onAction(ActionEvent event) {
		selectionModel.clearSelection();
		selectionModel.setSelection(this);
		this.selected = true;
		actionHandler.handleAction(this.action);
	}
	
	public ActionListener createActionListener() {
	    return new ActionListener() {
	        @Override
	        public void processAction(ActionEvent event) throws AbortProcessingException {
	        	onAction(event);
	        }
	    };
	}
	
    public String getLabel()
    {
    	return ResourceManager.instance().getString(action.toString(), ResourceType.LABEL);
    }

    public String getTooltip()
    {
    	return ResourceManager.instance().getString(action.toString(), ResourceType.TOOLTIP);
    }
    
    public String getIcon()
    {
    	return ResourceManager.instance().getString(action.toString(), 
    			ResourceType.ICON);
    }
	
}
