/**
 * This file belongs to the BPELUnit utility and Eclipse plugin set. See enclosed
 * license file for more information.
 * 
 */
package net.bpelunit.toolsupport.editors.sections;

import net.bpelunit.framework.client.eclipse.ExtensionControl;
import net.bpelunit.framework.client.eclipse.dialog.field.FileSelector;
import net.bpelunit.framework.xml.suite.XMLPUTDeploymentInformation;
import net.bpelunit.toolsupport.editors.TestSuitePage;
import net.bpelunit.toolsupport.editors.formwidgets.ComboEntry;
import net.bpelunit.toolsupport.editors.formwidgets.ContextPart;
import net.bpelunit.toolsupport.editors.formwidgets.EntryAdapter;
import net.bpelunit.toolsupport.editors.formwidgets.FormEntry;
import net.bpelunit.toolsupport.editors.formwidgets.FormEntryAdapter;
import net.bpelunit.toolsupport.editors.formwidgets.TextEntry;
import net.bpelunit.toolsupport.editors.wizards.DeploymentOptionWizard;
import net.bpelunit.toolsupport.util.WSDLFileFilter;
import net.bpelunit.toolsupport.util.WSDLFileValidator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * The PUT section allows the user to edit the PUT name, deployment type, and WSDL file, and also
 * offers a link to the deployment options editor.
 * 
 * @version $Id$
 * @author Philip Mayer
 * 
 */
public class PUTSection extends BPELUnitSection implements ContextPart, IHyperlinkListener {

	private TextEntry fNameEntry;
	private ComboEntry fTypeEntry;
	private FormEntry fWSDLEntry;

	public PUTSection(Composite parent, TestSuitePage page, FormToolkit toolkit, int style) {
		super(page, parent, toolkit, style);
		createClient(getSection(), toolkit);
	}

	protected void createClient(Section section, FormToolkit toolkit) {

		section.setText("Process Under Test");

		section.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		section.setDescription("Enter a name, type, and WSDL file for the PUT.");

		Composite content= toolkit.createComposite(section);
		TableWrapLayout layout= new TableWrapLayout();
		layout.leftMargin= layout.rightMargin= toolkit.getBorderStyle() != SWT.NULL ? 0 : 2;
		layout.numColumns= 3;
		content.setLayout(layout);

		createNameEntry(content, toolkit);
		createTypeEntry(content, toolkit);
		createWSDLEntry(content, toolkit);

		createText(content, "<form><p><a href=\"deploymentOptions\">Configure Deployment Options...</a></p></form>", toolkit, this);

		section.setClient(content);
		toolkit.paintBordersFor(content);
	}

	private void createWSDLEntry(Composite client, FormToolkit toolkit) {

		fWSDLEntry= new FormEntry(client, toolkit, "WSDL", "Browse...", true);

		fWSDLEntry.setFormEntryListener(new FormEntryAdapter(this) {
			@Override
			public void textValueChanged(FormEntry entry) {
				getPUT().setWsdl(entry.getValue());
			}

			@Override
			public void linkActivated(HyperlinkEvent e) {
				handleChooseWSDLSelected();
			}

			@Override
			public void browseButtonSelected(FormEntry entry) {
				handleChooseWSDLSelected();
			}
		});

		fWSDLEntry.setEditable(true);
	}

	protected void handleChooseWSDLSelected() {

		String currentFile= fWSDLEntry.getValue();

		IProject currentProject= getEditor().getCurrentProject();
		IContainer currentDirectory= getEditor().getCurrentDirectory();

		String path= FileSelector.getFile(getShell(), currentFile, new WSDLFileValidator(getEditor()), new WSDLFileFilter(), currentProject,
				currentDirectory);

		if (path != null) {
			fWSDLEntry.setValue(path);
			getPUT().setWsdl(path);
			manageTargetNamespace(path);
			markDirty();
		}
	}

	private void createNameEntry(Composite content, FormToolkit toolkit) {

		fNameEntry= new TextEntry(content, toolkit, "PUT Name", SWT.SINGLE);
		fNameEntry.setFormEntryListener(new EntryAdapter(this) {

			@Override
			public void textValueChanged(TextEntry entry) {
				getPUT().setName(entry.getValue());
			}
		});

	}

	private void createTypeEntry(Composite content, FormToolkit toolkit) {

		fTypeEntry= new ComboEntry(content, toolkit, "PUT Type");
		fTypeEntry.setItems(ExtensionControl.getDeployerMetaInformation());
		fTypeEntry.setFormEntryListener(new EntryAdapter(this) {

			@Override
			public void textValueChanged(TextEntry entry) {
				getPUT().setType(entry.getValue());
			}
		});

	}

	@Override
	public void refresh() {
		fNameEntry.setValue(getPUT().getName(), true);
		fTypeEntry.setValue(getPUT().getType(), true);
		fWSDLEntry.setValue(getPUT().getWsdl(), true);

		super.refresh();
	}

	public void fireSaveNeeded() {
		markDirty();
	}

	private XMLPUTDeploymentInformation getPUT() {
		return getTestSuite().getDeployment().getPut();
	}

	public void linkActivated(HyperlinkEvent e) {

		WizardDialog dialog= new WizardDialog(getShell(), new DeploymentOptionWizard(getPUT()));
		if (dialog.open() == Window.OK) {
			markDirty();
		}
		// TODO reset if cancel
	}

	public void linkEntered(HyperlinkEvent e) {
		// do nothing
	}

	public void linkExited(HyperlinkEvent e) {
		// do nothing
	}

}
