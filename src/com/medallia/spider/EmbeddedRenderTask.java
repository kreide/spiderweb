/*
 * This file is part of the Spider Web Framework.
 * 
 * The Spider Web Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Spider Web Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Spider Web Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.medallia.spider;

/**
 * Interface for tasks that render content which is embedded
 * in the content of a {@link IRenderTask}.
 * <p>
 * 
 * An embedded task differs from a {@link IRenderTask} in that it
 * cannot be rendered alone; it thus it does not have a page title.
 * <p>
 * 
 * The rendered content of this task will be placed in a StringTemplate
 * attribute which can then be included in the template for the
 * {@link IRenderTask}.
 * <p>
 * 
 * This feature can be used for including a component on several pages,
 * e.g. a heading, menu and footer.
 */
public interface EmbeddedRenderTask extends ITask {
	
	/** @return the name of the StringTemplate attribute in which the rendered content will be placed */ 
	String getStAttribute();

}
