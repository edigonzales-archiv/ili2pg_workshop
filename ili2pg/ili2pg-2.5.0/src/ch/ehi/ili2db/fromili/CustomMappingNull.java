package ch.ehi.ili2db.fromili;

import ch.ehi.sqlgen.repository.DbColumn;
import ch.ehi.sqlgen.repository.DbTable;
import ch.ehi.sqlgen.repository.DbTableName;
import ch.interlis.ili2c.metamodel.AssociationDef;
import ch.interlis.ili2c.metamodel.AttributeDef;
import ch.interlis.ili2c.metamodel.RoleDef;
import ch.interlis.ili2c.metamodel.Viewable;

public class CustomMappingNull implements CustomMapping {
	@Override
	public void init(ch.ehi.ili2db.gui.Config config)
	{
		
	}
	
	@Override
	public void end(ch.ehi.ili2db.gui.Config config)
	{
		
	}

	@Override
	public void fixupViewable(DbTable sqlTableDef, Viewable iliClassDef) {
	}

	@Override
	public void fixupAttribute(DbTable sqlTableDef, DbColumn sqlColDef, AttributeDef iliAttrDef) {
	}
	@Override
	public void fixupEmbeddedLink(DbTable dbTable, DbColumn dbColId,
			AssociationDef roleOwner, RoleDef role, DbTableName targetTable,
			String targetPk) {
	}

}
