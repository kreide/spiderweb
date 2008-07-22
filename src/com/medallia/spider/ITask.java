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

import java.util.Collection;

import com.medallia.spider.api.StRenderable;

/**
 * Interface that all tasks must implement; on top of {@link StRenderable}
 * it adds the concept of an embedded task; see {@link EmbeddedRenderTask}
 * for doc.
 */
public interface ITask extends StRenderable {

	/** list of embedded tasks this task depends on */
	Collection<EmbeddedRenderTask> dependsOn();
	
}
