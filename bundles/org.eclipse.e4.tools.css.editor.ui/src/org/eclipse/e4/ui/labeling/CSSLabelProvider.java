/*
* generated by Xtext
*/
package org.eclipse.e4.ui.labeling;

import java.util.Iterator;

import org.eclipse.e4.cSS.Rules;
import org.eclipse.e4.cSS.expr;
import org.eclipse.e4.cSS.selector;
import org.eclipse.e4.cSS.stylesheet;
import org.eclipse.e4.cSS.term;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider;

import com.google.inject.Inject;

/**
 * Provides labels for a EObjects.
 * 
 * see http://www.eclipse.org/Xtext/documentation/latest/xtext.html#labelProvider
 */
public class CSSLabelProvider extends DefaultEObjectLabelProvider {

	@Inject
	public CSSLabelProvider(AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}

	public String text(stylesheet ss) {
		StringBuilder builder = new StringBuilder();
//		builder.append(notNull(ss.getName()));
		builder.append("StyleSheet : ");
		builder.append(ss.getLocation());
		return builder.toString();
	}
	
	public String text(Rules r) {
		StringBuilder builder = new StringBuilder();
		builder.append("Rules : ");
		EList<selector> sels = r.getSelectors();
		Iterator<selector> iter = sels.iterator();
		while (iter.hasNext()) {
			selector s = (selector) iter.next();
			builder.append(s.getSimpleselectors().getElement().getName());
//		st<simple_selector>simple = s.getSimp	ELileselectors();
//			Iterator<simple_selector> simple_iter = simple.iterator();
//			while (simple_iter.hasNext()) {				
//				builder.append(((simple_selector) simple_iter).getElement().getName());
//			}
		}
		return builder.toString();
	}
	
	public String text(expr r) {
		StringBuilder builder = new StringBuilder();
		builder.append("expr : ");
		return builder.toString();
	}
	
	public String text(selector s) {
		StringBuilder builder = new StringBuilder();
		builder.append("selector : ");
		return builder.toString();
	}
	
//	public String text(simple_selector s) {
//		StringBuilder builder = new StringBuilder();
//		builder.append("simple selector : ");
//		return builder.toString();
//	}
	
	public String text(term s) {
		StringBuilder builder = new StringBuilder();
		builder.append("term: ");
		builder.append(s.getName());
		return builder.toString();
	}
}
