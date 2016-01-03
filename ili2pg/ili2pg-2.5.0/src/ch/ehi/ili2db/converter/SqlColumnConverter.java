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
package ch.ehi.ili2db.converter;

import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Connection;

import ch.interlis.iom.IomObject;

/**
 * @author ce
 * @version $Revision: 1.0 $ $Date: 12.02.2007 $
 */
public interface SqlColumnConverter {
	public void setup(Connection conn,ch.ehi.ili2db.gui.Config config);
	public abstract int getSrsid(String crsAuthority,String crsCode,Connection conn) throws ConverterException;
	/*
	public abstract String getCoordSqlUDT();
	public abstract String getPolylineSqlUDT();
	public abstract String getSurfaceSqlUDT();
	public abstract String getAreaSqlUDT();
	*/
	public abstract String getInsertValueWrapperCoord(String wkfValue,int srid);
	public abstract String getInsertValueWrapperPolyline(String wkfValue,int srid);
	public abstract String getInsertValueWrapperSurface(String wkfValue,int srid);
	public abstract String getInsertValueWrapperMultiSurface(String wkfValue,int srid);
	public abstract String getSelectValueWrapperCoord(String dbNativeValue);
	public abstract String getSelectValueWrapperPolyline(String dbNativeValue);
	public abstract String getSelectValueWrapperSurface(String dbNativeValue);
	public abstract String getSelectValueWrapperMultiSurface(String dbColName);
	public abstract void setCoordNull(java.sql.PreparedStatement stmt,int parameterIndex) throws java.sql.SQLException;
	public abstract void setPolylineNull(java.sql.PreparedStatement stmt,int parameterIndex)throws java.sql.SQLException;
	public abstract void setSurfaceNull(java.sql.PreparedStatement stmt,int parameterIndex)throws java.sql.SQLException;
	public abstract void setDecimalNull(java.sql.PreparedStatement stmt,int parameterIndex)throws java.sql.SQLException;
	public abstract void setBoolean(java.sql.PreparedStatement stmt,int parameterIndex,boolean value)throws java.sql.SQLException;
	public abstract Object fromIomUuid(String uuid)
			throws java.sql.SQLException, ConverterException;
	public abstract java.lang.Object fromIomSurface(
		IomObject obj,
		int srid,
		boolean hasLineAttr,
		boolean is3D,double p)
		throws java.sql.SQLException, ConverterException;
	public abstract java.lang.Object fromIomMultiSurface(
			IomObject obj,
			int srid,
			boolean hasLineAttr,
			boolean is3D,double p)
			throws java.sql.SQLException, ConverterException;
	public abstract java.lang.Object fromIomCoord(IomObject value,int srid, boolean is3D)
		throws java.sql.SQLException, ConverterException;
	public abstract java.lang.Object fromIomPolyline(
		IomObject obj,
		int srid,
		boolean is3D,double p)
		throws java.sql.SQLException, ConverterException;
	public abstract IomObject toIomCoord(
		Object geomobj,
		String sqlAttrName,
		boolean is3D)
		throws java.sql.SQLException, ConverterException;
	public abstract IomObject toIomSurface(
		Object geomobj,
		String sqlAttrName,
		boolean is3D)
		throws java.sql.SQLException, ConverterException;
	public abstract IomObject toIomMultiSurface(
			Object geomobj,
			String sqlAttrName,
			boolean is3D)
			throws java.sql.SQLException, ConverterException;
	public abstract IomObject toIomPolyline(
		Object geomobj,
		String sqlAttrName,
		boolean is3D)
		throws java.sql.SQLException, ConverterException;
}