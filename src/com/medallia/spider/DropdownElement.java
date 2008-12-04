package com.medallia.spider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.medallia.tiny.Func;
import com.medallia.tiny.Funcs;
import com.medallia.tiny.Implement;

/** Abstraction for an element in an HTML dropdown (option element) */
public class DropdownElement {
	
	private final String value, text;
	private final boolean selected;
	
	private DropdownElement(String value, String text, boolean selected) {
		this.value = value;
		this.text = text;
		this.selected = selected;
	}

	/** @return the value attribute */
	public String getValue() { return value; }
	/** @return the text to display for the option */
	public String getText() { return text; }
	/** @return true if this option is currently selected */
	public boolean isSelected() { return selected; }
	

	/** @return {@link DropdownElement} objects for the given list; the functions are used to obtain the value and text for each element */
	public static <X> List<DropdownElement> fromList(List<X> l, final String selectedVar, final Func<X, String> valueFunc, final Func<X, String> textFunc) {
		return Funcs.map(l, new Func<X, DropdownElement>() {
			@Implement public DropdownElement call(X x) {
				String value = valueFunc.call(x);
				return new DropdownElement(value, textFunc.call(x), selectedVar != null && selectedVar.equals(value));
			}
		});
	}

	/** @return {@link DropdownElement} objects for the given enums */
	public static <X extends Enum> List<DropdownElement> fromEnum(Class<X> type, final X selected) {
		return Funcs.map(Arrays.asList(type.getEnumConstants()), new Func<X, DropdownElement>() {
			@Implement public DropdownElement call(X x) {
				return new DropdownElement(String.valueOf(x.name()), x.toString(), selected == x);
			}
		});
	}
	
	/** Abstraction for the HTML optgroup element */
	public interface DropdownOptGroup {
		/** @return the text label for the group */
		String getText();
		/** @return the dropdown elements in this group */
		List<DropdownElement> getOptions();
	}
	
	/** @return {@link DropdownOptGroup} objects for the given map */
	public static <X> List<DropdownOptGroup> fromMap(Map<?, List<X>> m, final X selectedItem, final Func<X, String> valueFunc, final Func<X, String> textFunc) {
		return Funcs.map(m.entrySet(), new Func<Map.Entry<?, List<X>>, DropdownOptGroup>() {
			@Implement public DropdownOptGroup call(final Map.Entry<?, List<X>> me) {
				return new DropdownOptGroup() {
					@Implement public String getText() { return String.valueOf(me.getKey()); }
					@Implement public List<DropdownElement> getOptions() {
						return Funcs.map(me.getValue(), new Func<X, DropdownElement>() {
							@Implement public DropdownElement call(X x) {
								String value = valueFunc.call(x);
								return new DropdownElement(value, textFunc.call(x), selectedItem != null && valueFunc.call(selectedItem).equals(value));
							}
						});
					}
				};
			}
		});
	}
	
}
