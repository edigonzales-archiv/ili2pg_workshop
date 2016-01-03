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
package ch.ehi.basics.jdbc;

/**
 * @author ce
 * @version $Revision: 1.0 $ $Date: 26.05.2006 $
 */
public class DriverInfo {
	// system property jdbc.drivers=foo.bah.Driver
	// jdbc.drivers=sun.jdbc.odbc.JdbcOdbcDriver
	private String driverClassName=null;
	public String getDriverClassName() {
		return driverClassName;
	}
	public void setDriverClassName(String string) {
		driverClassName = string;
	}

}
