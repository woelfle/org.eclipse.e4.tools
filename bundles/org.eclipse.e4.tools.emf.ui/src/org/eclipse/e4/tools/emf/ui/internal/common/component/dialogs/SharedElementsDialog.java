package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.e4.tools.emf.ui.common.component.AbstractComponentEditor;

import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;

import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;

import org.eclipse.emf.edit.command.SetCommand;

import org.eclipse.emf.common.command.Command;

import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.e4.tools.emf.ui.common.IModelResource;

import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;

import org.eclipse.e4.tools.emf.ui.internal.PatternFilter;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

import org.eclipse.jface.viewers.StyledString;

import org.eclipse.e4.ui.model.application.ui.MUILabel;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.ViewerCell;

import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;

import org.eclipse.e4.ui.model.fragment.MStringModelFragment;

import org.eclipse.e4.ui.model.fragment.MModelFragment;

import org.eclipse.e4.ui.model.fragment.MModelFragments;

import org.eclipse.e4.ui.model.application.ui.MUIElement;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

import java.util.ArrayList;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.e4.ui.model.application.MApplicationElement;

import org.eclipse.e4.ui.model.application.MApplication;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;

import org.eclipse.jface.viewers.ArrayContentProvider;

import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.swt.layout.GridData;

import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Label;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.TitleAreaDialog;

public class SharedElementsDialog extends TitleAreaDialog {
	private TableViewer viewer;
	private MPlaceholder placeholder;
	private IModelResource resource;
	private ModelEditor editor;
	
	public SharedElementsDialog(Shell parentShell, ModelEditor editor, MPlaceholder placeholder, IModelResource resource) {
		super(parentShell);
		this.editor = editor;
		this.placeholder = placeholder;
		this.resource = resource;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);

		setTitle("Find Shared Elements");
		setMessage("Find Shared Elements of an Window");

		
		Composite container = new Composite(comp, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label l = new Label(container, SWT.NONE);
		l.setText("Name");
		
		Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		l = new Label(container, SWT.NONE);
		
		viewer = new TableViewer(container);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProviderImpl());
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		
		if( resource.getRoot().get(0) instanceof MApplication ) {
			List<MUIElement> list = new ArrayList<MUIElement>();
			for( MWindow m : ((MApplication)resource.getRoot().get(0)).getChildren() ) {
				list.addAll(filter(m.getSharedElements()));
			}
			viewer.setInput(list);
		} else if( resource.getRoot().get(0) instanceof MModelFragments ) {
			List<MApplicationElement> list = new ArrayList<MApplicationElement>();
			for( MModelFragment f : ((MModelFragments)resource.getRoot().get(0)).getFragments() ) {
				if( f instanceof MStringModelFragment ) {
					if( ((MStringModelFragment)f).getFeaturename().equals("sharedElements") ) { //$NON-NLS-1$
						list.addAll(filter(f.getElements()));
					}
				}
			}
			viewer.setInput(list);
		}
		
		final PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isParentMatch(Viewer viewer, Object element) {
				return viewer instanceof AbstractTreeViewer && super.isParentMatch(viewer, element);
			}
		};
		viewer.addFilter(filter);

		searchText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				filter.setPattern(((Text) e.widget).getText());
				viewer.refresh();
			}
		});
		
		return comp;
	}
	
	@Override
	protected void okPressed() {
		if( ! viewer.getSelection().isEmpty() ) {
			IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
			Command cmd = SetCommand.create(resource.getEditingDomain(), placeholder, AdvancedPackageImpl.Literals.PLACEHOLDER__REF, s.getFirstElement());
			if( cmd.canExecute() ) {
				resource.getEditingDomain().getCommandStack().execute(cmd);
				super.okPressed();
			}
		}
	}
	
	private static <T> List<T> filter(List<T> o) {
		List<T> rv = new ArrayList<T>();
		for( T i : o ) {
			if( i instanceof MPart || i instanceof MPartSashContainer ) {
				rv.add(i);
			}
		}
		return rv;
	}
	
	private class LabelProviderImpl extends StyledCellLabelProvider implements ILabelProvider {
		public void update(final ViewerCell cell) {
			EObject o = (EObject) cell.getElement();
			
			StyledString string = new StyledString(getTypename(o));
			
			if( o instanceof MUILabel ) {
				string.append(" - " + ((MUILabel)o).getLabel(), StyledString.DECORATIONS_STYLER);
			}
			
			MApplicationElement el = (MApplicationElement) o;
			string.append(" - " + el.getElementId(), StyledString.DECORATIONS_STYLER);
			
			cell.setText(string.getString());
			cell.setStyleRanges(string.getStyleRanges());
			cell.setImage(getImage(o));
		}
		
		public String getText(Object element) {
			EObject o = (EObject) element;
			MApplicationElement el = (MApplicationElement) o;
			
			if( el instanceof MUILabel ) {
				MUILabel label = (MUILabel) el;
				return getTypename(o) + " - " + el.getElementId() + " - " + label.getLabel();
			} else {
				return getTypename(o) + " - " + el.getElementId() + " - ";	
			}
		}
		
		private String getTypename(EObject o) {
			AbstractComponentEditor editor = SharedElementsDialog.this.editor.getEditor((o).eClass());
			if( editor != null ) {
				return editor.getLabel(o);
			} else {
				return o.eClass().getName();
			}
		}
		
		public Image getImage(Object element) {
			AbstractComponentEditor editor = SharedElementsDialog.this.editor.getEditor(((EObject)element).eClass());
			if( editor != null ) {
				return editor.getImage(element, getShell().getDisplay());
			}
			return null;
		}
	}
}