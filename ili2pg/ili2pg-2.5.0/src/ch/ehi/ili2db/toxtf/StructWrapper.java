/* This file is part of the ili2ora project.
 * For more information, please see <http://www.interlis.ch>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package ch.ehi.ili2db.toxtf;

import ch.interlis.iom.IomObject;
import ch.interlis.ili2c.metamodel.AttributeDef;

/**
 * @author ce
 * @version $Revision: 1.0 $ $Date: 05.04.2005 $
 */
public class StructWrapper {
	private int parentSqlId;
	private IomObject parent;
	private AttributeDef parentAttr;
	/** creates a StructWrapper.
	 * @param parentSqlId1 sqlid of the parent object/structele.
	 * @param parentAttr1 Structure attribute in the parent CLASS/STRUCTURE.
	 * @param parent1 parent object/structele.
	 */
	public StructWrapper(int parentSqlId1,AttributeDef parentAttr1,IomObject parent1){
		parentSqlId=parentSqlId1;
		parentAttr=parentAttr1;
		parent=parent1;
	}
	public int getParentSqlId() {
		return parentSqlId;
	}
	public AttributeDef getParentAttr() {
		return parentAttr;
	}
	public IomObject getParent() {
		return parent;
	}
}
