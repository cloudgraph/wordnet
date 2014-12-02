package org.cloudgraph.examples.wordnet.web.model;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@ManagedBean(name="NavigationBean")
@SessionScoped
public class NavigationBean extends ModelBean {
    
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(NavigationBean.class);
    
    private TreeNodeAction welcomeAction;
    private TreeNodeAction searchAction;
    private TreeNodeAction profileAction;
    private TreeNodeAction contactAction;
    private TreeNodeAction administrationAction;
    private TreeNodeAction logonAction;
    private TreeNodeAction logoffAction;
    private TreeNodeAction registerAction;
    private TreeNodeAction docsAction;
    private TreeNodeAction aboutAction;
    
    private TreeNodeAction selectedTopAction;
    
    private TreeSelectionModel topSelectionModel;
    	
	public NavigationBean()
	{
		ActionHandler topActionHandler = new ActionHandler() {
			@Override
			public String handleAction(Action action) {
				// TODO Auto-generated method stub
				return action.toString();
			}
		};
		
		topSelectionModel = new TreeSelectionModel() {
			@Override
			public void clearSelection() {
				welcomeAction.setSelected(false);
				searchAction.setSelected(false);
				profileAction.setSelected(false);
				contactAction.setSelected(false);
				administrationAction.setSelected(false);
				logonAction.setSelected(false);
				logoffAction.setSelected(false);
				registerAction.setSelected(false);
				aboutAction.setSelected(false);
				docsAction.setSelected(false);
			}

			@Override
			public void setSelection(TreeNodeAction selection) {
				clearSelection();
				selectedTopAction = selection;
				selectedTopAction.setSelected(true);				
			}			
		};
		
		welcomeAction = new TreeNodeAction(Action.topnav_welcome, 
	    		topActionHandler, topSelectionModel);
		searchAction = new TreeNodeAction(Action.topnav_search, 
	    		topActionHandler, topSelectionModel);
		profileAction = new TreeNodeAction(Action.topnav_profile, 
	    		topActionHandler, topSelectionModel);
		contactAction = new TreeNodeAction(Action.topnav_contact, 
	    		topActionHandler, topSelectionModel);
		administrationAction = new TreeNodeAction(Action.topnav_administration, 
	    		topActionHandler, topSelectionModel);
		logonAction = new TreeNodeAction(Action.topnav_logon, 
	    		topActionHandler, topSelectionModel);
		logoffAction = new TreeNodeAction(Action.topnav_logoff, 
	    		topActionHandler, topSelectionModel);
		registerAction = new TreeNodeAction(Action.topnav_register, 
	    		topActionHandler, topSelectionModel);
		aboutAction = new TreeNodeAction(Action.topnav_about, 
	    		topActionHandler, topSelectionModel);
		docsAction = new TreeNodeAction(Action.topnav_docs, 
	    		topActionHandler, topSelectionModel);
		
	    selectedTopAction = welcomeAction;
		
	    ActionHandler leftActionHandler = new ActionHandler() {

			@Override
			public String handleAction(Action action) {
				return selectedTopAction.getAction().toString();
			}
			
		};
	    
		// default selection
		setWelcomeSelected(new Boolean(true));
	}

	public void setWelcomeSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.welcomeAction);
		else
			this.welcomeAction.setSelected(false);			
	}
	
	public void handleWelcomeSelected(AjaxBehaviorEvent event) {
		this.topSelectionModel.setSelection(this.welcomeAction);
	}
	
	public TreeNodeAction getWelcomeAction() {
		return this.welcomeAction;
	}
	

	public void setSearchSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.searchAction);
		else
			this.searchAction.setSelected(false);			
	}
	
	public void handleSearchSelected(AjaxBehaviorEvent event) {
		this.topSelectionModel.setSelection(this.searchAction);
	}
	
	public TreeNodeAction getSearchAction() {
		return this.searchAction;
	}

	
	public void setProfileSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.profileAction);
		else
			this.profileAction.setSelected(false);			
	}
	
	public void handleProfileSelected(AjaxBehaviorEvent event) {
		this.topSelectionModel.setSelection(this.profileAction);
	}
	
	public TreeNodeAction getProfileAction() {
		return this.profileAction;
	}
	
	public void setContactSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.contactAction);
		else
			this.contactAction.setSelected(false);			
	}
	
	public void handleContactSelected(AjaxBehaviorEvent event) {
		this.topSelectionModel.setSelection(this.contactAction);
	}
	
	public TreeNodeAction getContactAction() {
		return this.contactAction;
	}	
	
	public TreeNodeAction getAdministrationAction() {
		return this.administrationAction;
	}
	
	public void setAdministrationSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.administrationAction);
		else
			this.administrationAction.setSelected(false);			
	}
	
	public void handleAdministrationSelected(AjaxBehaviorEvent event) {
		this.topSelectionModel.setSelection(this.administrationAction);
	}

	public TreeNodeAction getLogonAction() {
		return this.logonAction;
	}
	
	public void setLogonSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.logonAction);
		else
			this.logonAction.setSelected(false);			
	}
	
	public void handleLogonSelected(AjaxBehaviorEvent event) {
		this.topSelectionModel.setSelection(this.logonAction);
	}
		
	public TreeNodeAction getLogoffAction() {
		return this.logoffAction;
	}
	
	public void setLogoffSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.logoffAction);
		else
			this.logoffAction.setSelected(false);			
	}
	
	public void handleLogoffSelected(AjaxBehaviorEvent event) {
		this.topSelectionModel.setSelection(this.logoffAction);
	}

	public TreeNodeAction getRegisterAction() {
		return this.registerAction;
	}
	
	public void setRegisterSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.registerAction);
		else
			this.registerAction.setSelected(false);			
	}
	
	public TreeNodeAction getAboutAction() {
		return this.aboutAction;
	}
	
	public void setAboutSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.aboutAction);
		else
			this.aboutAction.setSelected(false);			
	}
	
	public TreeNodeAction getDocsAction() {
		return this.docsAction;
	}
	
	public void seDocsSelected(Object selected) {
		Boolean b = new Boolean(String.valueOf(selected));
		if (b.booleanValue())
		    this.topSelectionModel.setSelection(this.docsAction);
		else
			this.docsAction.setSelected(false);			
	}
	
	public void handleDocsSelected(AjaxBehaviorEvent event) {
		this.topSelectionModel.setSelection(this.docsAction);
	}
	
	public TreeNodeAction getSelectedTopAction() {
		return selectedTopAction;
	}

	public void setSelectedTopAction(TreeNodeAction selectedTopAction) {
		this.selectedTopAction = selectedTopAction;
	}	
    
}
