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
package ch.ehi.ili2db.base;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.logging.LogEvent;
import ch.ehi.basics.logging.StdLogEvent;
import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.metamodel.AttributeDef;
import ch.interlis.ili2c.metamodel.Element;
import ch.interlis.ili2c.metamodel.SurfaceOrAreaType;
import ch.interlis.ili2c.metamodel.Topic;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Type;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.ilirepository.IliFiles;
import ch.ehi.sqlgen.repository.DbSchema;
import ch.ehi.sqlgen.repository.DbTableName;
import ch.ehi.sqlgen.generator.*;
//import ch.ehi.sqlgen.generator_impl.oracle.GeneratorOracle;
import ch.ehi.sqlgen.generator_impl.jdbc.*;
import ch.ehi.sqlgen.generator.Generator;
import ch.ehi.ili2db.converter.*;
import ch.ehi.ili2db.fromili.*;
import ch.ehi.ili2db.fromxtf.*;
import ch.ehi.ili2db.mapping.*;
import ch.ehi.ili2db.toxtf.*;
import ch.interlis.iom_j.iligml.IligmlWriter;
//import ch.interlis.iom.swig.iom_javaConstants;
import ch.interlis.iom_j.itf.ItfReader;
import ch.interlis.iom_j.itf.ItfReader2;
import ch.interlis.iom_j.itf.ItfWriter;
import ch.interlis.iom_j.itf.ItfWriter2;
import ch.interlis.iom_j.xtf.XtfWriter;
import ch.interlis.iom_j.xtf.XtfReader;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox.IoxWriter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import ch.ehi.ili2db.gui.Config;

/**
 * @author ce
 * @version $Revision: 1.0 $ $Date: 07.02.2005 $
 */
public class Ili2db {
	public static final String XTF="%XTF";
	public static final String XTF_DIR="%XTF_DIR";
	public static final String JAR_DIR="%JAR_DIR";
	public static final String ILI_FROM_DB="%ILI_FROM_DB";
	private final static String SETTINGS_FILE = System.getProperty("user.home") + "/.ili2db";
	public static final String SETTING_DIRUSED="ch.ehi.ili2db.dirused";
	public static void readAppSettings(Settings settings)
	{
		java.io.File file=new java.io.File(SETTINGS_FILE);
		try{
			if(file.exists()){
				settings.load(file);
			}
		}catch(java.io.IOException ex){
			EhiLogger.logError("failed to load settings from file "+SETTINGS_FILE,ex);
		}
	}
	public static void writeAppSettings(Settings settings)
	{
		java.io.File file=new java.io.File(SETTINGS_FILE);
		try{
			settings.store(file,"ili2db settings");
		}catch(java.io.IOException ex){
			EhiLogger.logError("failed to settings settings to file "+SETTINGS_FILE,ex);
		}
	}
	public static void readSettingsFromDb(Config config)
	throws Ili2dbException
	{
		String dburl=config.getDburl();
		String dbusr=config.getDbusr();
		String dbpwd=config.getDbpwd();
		if(dburl==null){
			EhiLogger.logError("no dburl given");
			return;
		}
		if(dbusr==null){
			//EhiLogger.logError("no dbusr given");
			//return;
			dbusr="";
		}
		if(dbpwd==null){
			//EhiLogger.logError("no dbpwd given");
			//return;
			dbpwd="";
		}
		String jdbcDriver=config.getJdbcDriver();
		if(jdbcDriver==null){
			EhiLogger.logError("no JDBC driver given");
			return;
		}
		if(jdbcDriver.equals("ch.ehi.ili2geodb.jdbc.GeodbDriver")){
			return;
		}
		// open db connection
		try{
			Class.forName(jdbcDriver);
		}catch(Exception ex){
			EhiLogger.logError("failed to load JDBC driver",ex);
			return;
		}
		Connection conn=null;
		String url = dburl;
		try {
			conn = DriverManager.getConnection(url, dbusr, dbpwd);
			TransferFromIli.readSettings(conn,config,config.getDbschema());
		} catch (SQLException e) {
			EhiLogger.logError(e);
		}finally{
			if(conn!=null){
				try{
					conn.close();
				}catch(java.sql.SQLException ex){
					EhiLogger.logError(ex);
				}finally{
					conn=null;
				}
			}
		}
		
	}
	public static void run(Config config,String appHome)
	throws Ili2dbException
	{
		if(config.getFunction()==Config.FC_IMPORT){
			runImport(config,appHome);
		}else if(config.getFunction()==Config.FC_UPDATE){
			runUpdate(config,appHome,false);
		}else if(config.getFunction()==Config.FC_EXPORT){
			runExport(config,appHome);
		}else if(config.getFunction()==Config.FC_SCHEMAIMPORT){
			runSchemaImport(config,appHome);
		}else{
			throw new Ili2dbException("function not supported");
		}

		
	}
	public static void runImport(Config config,String appHome) 
	throws Ili2dbException
	{
		runUpdate(config,appHome,true);
	}
	public static void runUpdate(Config config,String appHome,boolean importOnly) 
	throws Ili2dbException
		{
		ch.ehi.basics.logging.FileListener logfile=null;
		if(config.getLogfile()!=null){
			logfile=new Ili2dbLogger(new java.io.File(config.getLogfile()),true);
			EhiLogger.getInstance().addListener(logfile);
		}
		try{
			logGeneralInfo(config);
			
			//String zipfilename=null;
			java.util.zip.ZipEntry zipXtfEntry=null;
			java.util.zip.ZipFile zipFile=null;
			String inputFilename=config.getXtffile();
				if(inputFilename==null){
					throw new Ili2dbException("no xtf-file given");
				}
				if(ch.ehi.basics.view.GenericFileFilter.getFileExtension(inputFilename).toLowerCase().equals("zip")){
					try {
						zipFile=new java.util.zip.ZipFile(inputFilename);
					} catch (IOException ex) {
						throw new Ili2dbException(ex);
					}
					java.util.Enumeration filei=zipFile.entries();
					while(filei.hasMoreElements()){
						java.util.zip.ZipEntry zipEntry=(java.util.zip.ZipEntry)filei.nextElement();
						String ext=ch.ehi.basics.view.GenericFileFilter.getFileExtension(zipEntry.getName()).toLowerCase();
						if(ext!=null && (ext.equals("xml") || ext.equals("xtf") || ext.equals("itf"))){
							zipXtfEntry=zipEntry;
							break;
						}
					}
					if(zipXtfEntry==null){
						throw new Ili2dbException("no xtf/itf-file in zip-archive "+zipFile.getName());
					}
				}
				String modeldir=config.getModeldir();
				if(modeldir==null){
					throw new Ili2dbException("no modeldir given");
				}
				EhiLogger.traceState("modeldir <"+modeldir+">");
			
			String models=config.getModels();
			if(models==null){
				throw new Ili2dbException("no models given");
			}
			EhiLogger.traceState("models <"+models+">");
			ch.interlis.ili2c.config.Configuration modelv=new ch.interlis.ili2c.config.Configuration();
			String modelnames[]=models.split(ch.interlis.ili2c.Main.MODELS_SEPARATOR);
			for(int modeli=0;modeli<modelnames.length;modeli++){
				String m=modelnames[modeli];
				if(m.equals(XTF)){
					// read modelname from xtf-file
					if(zipXtfEntry!=null){
						try{
							java.io.InputStream in=zipFile.getInputStream(zipXtfEntry);
							m=getModelFromXtf(in,zipXtfEntry.getName());
						}catch(java.io.IOException ex){
							throw new Ili2dbException(ex);
						}
					}else{
						m=getModelFromXtf(inputFilename);
					}
				}
				if(m!=null){
					modelv.addFileEntry(new ch.interlis.ili2c.config.FileEntry(m,ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));				
				}
			}
			if(modelv.getSizeFileEntry()==0){
				throw new Ili2dbException("no models given");
			}

			String dburl=config.getDburl();
			String dbusr=config.getDbusr();
			String dbpwd=config.getDbpwd();
			if(dburl==null){
				throw new Ili2dbException("no dburl given");
			}
			if(dbusr==null){
				//EhiLogger.logError("no dbusr given");
				//return;
				dbusr="";
			}
			if(dbpwd==null){
				//EhiLogger.logError("no dbpwd given");
				//return;
				dbpwd="";
			}
			String dbschema=config.getDbschema();
			if(dbschema!=null){
				EhiLogger.logState("dbschema <"+dbschema+">");
			}
			String geometryConverter=config.getGeometryConverter();
			if(geometryConverter==null){
				throw new Ili2dbException("no geoemtry converter given");
			}
			String ddlGenerator=config.getDdlGenerator();
			if(ddlGenerator==null){
				throw new Ili2dbException("no DDL generator given");
			}
			String idGenerator=config.getIdGenerator();
			if(idGenerator==null){
				throw new Ili2dbException("no ID generator given");
			}
			String jdbcDriver=config.getJdbcDriver();
			if(jdbcDriver==null){
				throw new Ili2dbException("no JDBC driver given");
			}
			
			// open db connection
			try{
				Class.forName(jdbcDriver);
			}catch(Exception ex){
				throw new Ili2dbException("failed to load JDBC driver",ex);
			}
			Connection conn=null;
			String url = dburl;
			ch.ehi.basics.logging.ErrorTracker errs=null;
			try{
				EhiLogger.logState("dburl <"+url+">");
				EhiLogger.logState("dbusr <"+dbusr+">");
			  //DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			  try {
				conn = DriverManager.getConnection(url, dbusr, dbpwd);
			} catch (SQLException ex) {
				throw new Ili2dbException("failed to get db connection",ex);
			}
			  logDBVersion(conn);
			  
			  // switch off auto-commit
			  try {
				conn.setAutoCommit(false);
			} catch (SQLException ex) {
				throw new Ili2dbException("failed to switch off auto-commit",ex);
			}
			  
			  // create db schema
				if(importOnly){
				  	if(config.getDbschema()!=null){
				  		if(!DbUtility.schemaExists(conn, config.getDbschema())){
					  		DbUtility.createSchema(conn, config.getDbschema());
				  		}
				  	}
				}
			  	
				// compile required ili files
				setupIli2cPathmap(config, appHome, inputFilename,conn);
				EhiLogger.logState("compile models...");
				modelv.setAutoCompleteModelList(true);
				modelv.setGenerateWarnings(false);
				TransferDescription td = ch.interlis.ili2c.Main.runCompiler(modelv,
						config);
				if (td == null) {
					throw new Ili2dbException("compiler failed");
				}
			  	
				// read mapping file
				String mappingConfig=config.getMappingConfigFilename();
				NameMapping mapping=new NameMapping(config);
				  if(DbUtility.tableExists(conn,new DbTableName(config.getDbschema(),DbNames.CLASSNAME_TAB))){
					  // read mapping from db
					  mapping.readTableMappingTable(conn,config.getDbschema());
				  }
				  if(DbUtility.tableExists(conn,new DbTableName(config.getDbschema(),DbNames.ATTRNAME_TAB))){
					  // read mapping from db
					  mapping.readAttrMappingTable(conn,config.getDbschema());
				  }
				if(mappingConfig!=null){
					mapping.readDeprecatedConfig(mappingConfig);
				}
				  TrafoConfig trafoConfig=new TrafoConfig();
				  trafoConfig.readTrafoConfig(conn, config.getDbschema());

				ModelElementSelector ms=new ModelElementSelector();
				ArrayList<String> modelNames=new ArrayList<String>();
				for(int modeli=0;modeli<modelv.getSizeFileEntry();modeli++){
					if(modelv.getFileEntry(modeli).getKind()==ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE){
						String m=modelv.getFileEntry(modeli).getFilename();
						modelNames.add(m);				
					}
				}
				java.util.List<Element> eles=ms.getModelElements(modelNames,td, td.getIli1Format()!=null && config.getDoItfLineTables(),Config.CREATE_ENUM_DEFS_MULTI.equals(config.getCreateEnumDefs()));
				optimizeSqlTableNames(config,mapping,eles);
				Viewable2TableMapping class2wrapper=Viewable2TableMapper.getClass2TableMapping(config,trafoConfig,eles);

				Generator gen=null;
				try{
					gen=(Generator)Class.forName(ddlGenerator).newInstance();
				}catch(Exception ex){
					throw new Ili2dbException("failed to load/create DDL generator",ex);
				}
				DbIdGen idGen=null;
				try{
					idGen=(DbIdGen)Class.forName(idGenerator).newInstance();
				}catch(Exception ex){
					throw new Ili2dbException("failed to load/create ID generator",ex);
				}
				idGen.init(config.getDbschema());
				SqlColumnConverter geomConverter=null;
				try{
					geomConverter=(SqlColumnConverter)Class.forName(geometryConverter).newInstance();
				}catch(Exception ex){
					throw new Ili2dbException("failed to load/create geometry converter",ex);
				}
				geomConverter.setup(conn, config);

				config.setJdbcConnection(conn);
				idGen.initDb(conn,dbusr);
				idGen.initDbDefs();
				
				// create table structure
				if(importOnly){
					EhiLogger.logState("create table structure...");
					try{
						TransferFromIli trsfFromIli=new TransferFromIli();
						
						// map ili-classes to sql-tables
						DbSchema schema;
						try {
							schema = trsfFromIli.doit(td,eles,mapping,config,idGen,trafoConfig,class2wrapper);
						} catch (Ili2dbException e) {
							throw new Ili2dbException("mapping of ili-classes to sql-tables failed",e);
						}
						if(schema==null){
							return;
						}

						trsfFromIli.addBasketsTable(schema);
						trsfFromIli.addImportsTable(schema);

						TransferFromIli.addInheritanceTable(schema,Integer.parseInt(config.getMaxSqlNameLength()));
						TransferFromIli.addSettingsTable(schema);
						TransferFromIli.addTrafoConfigTable(schema);
						TransferFromIli.addModelsTable(schema);
						trsfFromIli.addEnumTable(schema);
						TransferFromIli.addTableMappingTable(schema);
						TransferFromIli.addAttrMappingTable(schema);
						idGen.addMappingTable(schema);
						
						GeneratorDriver drv=new GeneratorDriver(gen);
						drv.visitSchema(config,schema);
						// create script requested by user?
						String createscript=config.getCreatescript();
						if(createscript!=null && (gen instanceof GeneratorJdbc)){
							writeScript(createscript,((GeneratorJdbc)gen).iteratorCreateLines());
						}
						// drop script requested by user?
						String dropscript=config.getDropscript();
						if(dropscript!=null && (gen instanceof GeneratorJdbc)){
							writeScript(dropscript,((GeneratorJdbc)gen).iteratorDropLines());
						}
							// update mapping table
							mapping.updateTableMappingTable(conn,config.getDbschema());
							mapping.updateAttrMappingTable(conn,config.getDbschema());
							trafoConfig.updateTrafoConfig(conn, config.getDbschema());
							// update inheritance table
							trsfFromIli.updateInheritanceTable(conn,config.getDbschema());
							// update enumerations table
							trsfFromIli.updateEnumTable(conn);
							TransferFromIli.addModels(conn,td,config.getDbschema());
							if(!config.isConfigReadFromDb()){
								TransferFromIli.updateSettings(conn,config,config.getDbschema());
							}
					}catch(java.io.IOException ex){
						throw new Ili2dbException(ex);
					}
				}
				
				// process xtf files
				EhiLogger.logState("process data file...");
				HashSet<BasketStat> stat=new HashSet<BasketStat>();
				errs=new ch.ehi.basics.logging.ErrorTracker();
				EhiLogger.getInstance().addListener(errs);
				if(zipXtfEntry!=null){
					IoxReader ioxReader=null;
					java.io.InputStream in = null;
					try {
						  EhiLogger.logState("data <"+inputFilename+":"+zipXtfEntry.getName()+">");
						in = zipFile.getInputStream(zipXtfEntry);
						if(isItfFilename(zipXtfEntry.getName())){
							if(config.getDoItfLineTables()){
								ioxReader=new ItfReader(in);
								((ItfReader)ioxReader).setModel(td);		
							}else{
								ioxReader=new ItfReader2(in);
								((ItfReader2)ioxReader).setModel(td);		
							}
						}else{
							ioxReader=new XtfReader(in);
						}
						transferFromXtf(conn,ioxReader,importOnly,mapping,td,dbusr,geomConverter,idGen,config,stat,trafoConfig,class2wrapper);
					} catch (IOException ex) {
						throw new Ili2dbException(ex);
					} catch (IoxException ex) {
						throw new Ili2dbException(ex);
					}finally{
						if(ioxReader!=null){
							try {
								ioxReader.close();
							} catch (IoxException e) {
								throw new Ili2dbException(e);
							}
							ioxReader=null;
						}
						if(in!=null){
							try {
								in.close();
							} catch (IOException e) {
								throw new Ili2dbException(e);
							}
							in=null;
						}
					}
					// save attachments
					String attachmentKey=config.getAttachmentKey();
					String attachmentsBase=config.getAttachmentsPath();
					if(attachmentsBase!=null){
						java.io.File basePath=new java.io.File(attachmentsBase,attachmentKey);
						java.util.Enumeration filei=zipFile.entries();
						while(filei.hasMoreElements()){
							java.util.zip.ZipEntry zipEntry=(java.util.zip.ZipEntry)filei.nextElement();
							if(!zipXtfEntry.getName().equals(zipEntry.getName())){
								// save file
								java.io.File destFile=new java.io.File(basePath,zipEntry.getName());
								java.io.File parent=destFile.getAbsoluteFile().getParentFile();
								if(!parent.exists()){
									if(!parent.mkdirs()){
										throw new Ili2dbException("failed to create "+parent.getAbsolutePath());
									}
								}
								try {
									copyStream(destFile,zipFile.getInputStream(zipEntry));
								} catch (IOException ex) {
									throw new Ili2dbException("failed to save attachment "+zipEntry.getName(),ex);
								}
							}
						}
						
					}
				}else{
					  EhiLogger.logState("data <"+inputFilename+">");
					IoxReader ioxReader=null;
					try {
						if(isItfFilename(inputFilename)){
							if(config.getDoItfLineTables()){
								ioxReader=new ItfReader(new java.io.File(inputFilename));
								((ItfReader)ioxReader).setModel(td);		
							}else{
								ioxReader=new ItfReader2(new java.io.File(inputFilename));
								((ItfReader2)ioxReader).setModel(td);		
							}
						}else{
							ioxReader=new XtfReader(new java.io.File(inputFilename));
						}
						transferFromXtf(conn,ioxReader,importOnly,mapping,td,dbusr,geomConverter,idGen,config,stat,trafoConfig,class2wrapper);
					} catch (IoxException e) {
						throw new Ili2dbException(e);
					}finally{
						if(ioxReader!=null){
							try {
								ioxReader.close();
							} catch (IoxException e) {
								throw new Ili2dbException(e);
							}
							ioxReader=null;
						}
					}
				}
				
				if(errs.hasSeenErrors()){
					try {
						conn.rollback();
					} catch (SQLException e) {
						EhiLogger.logError("rollback failed",e);
					}
					throw new Ili2dbException("...import failed");
				}else{
					try {
						conn.commit();
					} catch (SQLException e) {
						EhiLogger.logError("commit failed",e);
						throw new Ili2dbException("...import failed");
					}
					logStatistics(td.getIli1Format()!=null,stat);
					EhiLogger.logState("...import done");
				}
			}finally{
				if(conn!=null){
					try{
						conn.close();
					}catch(java.sql.SQLException ex){
						EhiLogger.logError(ex);
					}finally{
						conn=null;
					}
				}
				if(errs!=null){
					EhiLogger.getInstance().removeListener(errs);
					errs=null;
				}
			}
		}catch(Ili2dbException ex){
			if(logfile!=null){
				logfile.logEvent(new StdLogEvent(LogEvent.ERROR,null,ex,null));
			}
			throw ex;
		}catch(java.lang.RuntimeException ex){
			if(logfile!=null){
				logfile.logEvent(new StdLogEvent(LogEvent.ERROR,null,ex,null));
			}
			throw ex;
		}finally{
			if(logfile!=null){
				EhiLogger.getInstance().removeListener(logfile);
				logfile.close();
				logfile=null;
			}
		}
		

	}
	private static void logStatistics(boolean isIli1,HashSet<BasketStat> stat)
	{
		ArrayList<BasketStat> statv=new ArrayList<BasketStat>(stat);
		java.util.Collections.sort(statv,new java.util.Comparator<BasketStat>(){
			@Override
			public int compare(BasketStat b0, BasketStat b1) {
				int ret=b0.getFile().compareTo(b1.getFile());
				if(ret==0){
					ret=b0.getTopic().compareTo(b1.getTopic());
					if(ret==0){
						ret=b0.getBasketId().compareTo(b1.getBasketId());
					}
				}
				return ret;
			}
			
		});
		for(BasketStat basketStat:statv){
			if(isIli1){
				EhiLogger.logState(basketStat.getFile()+": "+basketStat.getTopic());
			}else{
				EhiLogger.logState(basketStat.getFile()+": "+basketStat.getTopic()+" BID="+basketStat.getBasketId());
			}
			HashMap<String, ClassStat> objStat=basketStat.getObjStat();
			ArrayList<String> classv=new ArrayList<String>(objStat.keySet());
			java.util.Collections.sort(classv,new java.util.Comparator<String>(){
				@Override
				public int compare(String b0, String b1) {
					int ret=b0.compareTo(b1);
					return ret;
				}
			});
			String nbsp=Character.toString('\u00A0');
			for(String className : classv){
				ClassStat classStat=objStat.get(className);
				String objCount=Integer.toString(classStat.getObjcount());
				if(objCount.length()<6){
					objCount=ch.ehi.basics.tools.StringUtility.STRING(6-objCount.length(), ' ')+objCount;
				}
				EhiLogger.logState(nbsp+objCount+" objects in CLASS "+className);
			}
		}
	}
	private static void copyStream(java.io.File outFile, java.io.InputStream in) throws IOException {
		java.io.BufferedWriter out=new java.io.BufferedWriter(new java.io.FileWriter(outFile));
		byte[] bt = new byte[1024];
		int i;
		while((i=in.read(bt)) != -1)
					{
						out.write(new String(bt,0,i));
					}
		out.close();
	}
	
	public static Ili2dbInit getInitStrategy(Config config)
	throws Ili2dbException
	{
		String initClassName=config.getInitStrategy();
		if(initClassName==null){
			return new Ili2dbInitNull();
		}
		Ili2dbInit init=null;
		try{
			init=(Ili2dbInit)Class.forName(initClassName).newInstance();
		}catch(Exception ex){
			throw new Ili2dbException("failed to load/create init strategy",ex);
		}
		return init;
	}
	public static void runSchemaImport(Config config,String appHome) 
	throws Ili2dbException
	{
		ch.ehi.basics.logging.FileListener logfile=null;
		if(config.getLogfile()!=null){
			logfile=new ch.ehi.basics.logging.FileListener(new java.io.File(config.getLogfile()),true);
			EhiLogger.getInstance().addListener(logfile);
		}
		try{
			logGeneralInfo(config);
			
			Ili2dbInit ao=null;
			try{
				ao=getInitStrategy(config); 
				ao.init();
				
			ch.interlis.ili2c.config.Configuration modelv=new ch.interlis.ili2c.config.Configuration();
			String xtffile=config.getXtffile();
			String ilifile=null;
			if(xtffile!=null && xtffile.endsWith(".ili")){
				ilifile=xtffile;
				modelv.addFileEntry(new ch.interlis.ili2c.config.FileEntry(ilifile,ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));				
			}
			
			String models=config.getModels();
			if(models!=null){
				String modelnames[]=models.split(";");
				for(int modeli=0;modeli<modelnames.length;modeli++){
					String m=modelnames[modeli];
					if(m!=null){
						if(m.equals(XTF)){
							// ignore it
						}else{
							modelv.addFileEntry(new ch.interlis.ili2c.config.FileEntry(m,ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));				
						}
					}
				}
			}
			if(modelv.getSizeFileEntry()==0){
				throw new Ili2dbException("no models given");
			}

			String dburl=config.getDburl();
			String dbusr=config.getDbusr();
			String dbpwd=config.getDbpwd();
			if(dburl==null){
				throw new Ili2dbException("no dburl given");
			}
			if(dbusr==null){
				//EhiLogger.logError("no dbusr given");
				//return;
				dbusr="";
			}
			if(dbpwd==null){
				//EhiLogger.logError("no dbpwd given");
				//return;
				dbpwd="";
			}
			String dbschema=config.getDbschema();
			if(dbschema!=null){
				EhiLogger.logState("dbschema <"+dbschema+">");
			}
			String geometryConverter=config.getGeometryConverter();
			if(geometryConverter==null){
				throw new Ili2dbException("no geoemtry converter given");
			}
			String ddlGenerator=config.getDdlGenerator();
			if(ddlGenerator==null){
				throw new Ili2dbException("no DDL generator given");
			}
			String idGenerator=config.getIdGenerator();
			if(idGenerator==null){
				throw new Ili2dbException("no ID generator given");
			}
			String jdbcDriver=config.getJdbcDriver();
			if(jdbcDriver==null){
				throw new Ili2dbException("no JDBC driver given");
			}

			// open db connection
			try{
				Class.forName(jdbcDriver);
			}catch(Exception ex){
				throw new Ili2dbException("failed to load JDBC driver",ex);
			}
			Connection conn=null;
			String url = dburl;
			try{
				EhiLogger.logState("dburl <"+url+">");
				EhiLogger.logState("dbusr <"+dbusr+">");
				conn = DriverManager.getConnection(url, dbusr, dbpwd);
			  logDBVersion(conn);
			  
			  // switch off auto-commit
			  conn.setAutoCommit(false);
			  
			}catch(SQLException ex){
				throw new Ili2dbException(ex);
			}
			
			
			// setup ilidirs+pathmap for ili2c
			setupIli2cPathmap(config, appHome, ilifile,conn);
			
			// compile required ili files
			EhiLogger.logState("compile models...");
			TransferDescription td;
			modelv.setAutoCompleteModelList(true);
			modelv.setGenerateWarnings(false);
			td = ch.interlis.ili2c.Main.runCompiler(modelv,
					config);
			if (td == null) {
				throw new Ili2dbException("compiler failed");
			}
			
			// an INTERLIS 1 model?
			if(td.getIli1Format()!=null){
				config.setItfTranferfile(true);
			}
			Generator gen=null;
			try{
				gen=(Generator)Class.forName(ddlGenerator).newInstance();
			}catch(Exception ex){
				throw new Ili2dbException("failed to load/create DDL generator",ex);
			}
			  // create db schema
		  	if(config.getDbschema()!=null){
		  		if(!DbUtility.schemaExists(conn, config.getDbschema())){
			  		DbUtility.createSchema(conn, config.getDbschema());
		  		}
		  	}
			DbIdGen idGen=null;
			try{
				idGen=(DbIdGen)Class.forName(idGenerator).newInstance();
			}catch(Exception ex){
				throw new Ili2dbException("failed to load/create ID generator",ex);
			}
		  	idGen.init(config.getDbschema());

			// read mapping file
			String mappingConfigFilename=config.getMappingConfigFilename();
			NameMapping mapping=new NameMapping(config);
			if(!(conn instanceof GeodbConnection)){
				  if(DbUtility.tableExists(conn,new DbTableName(config.getDbschema(),DbNames.CLASSNAME_TAB))){
					  // read mapping from db
					  mapping.readTableMappingTable(conn,config.getDbschema());
				  }
				  if(DbUtility.tableExists(conn,new DbTableName(config.getDbschema(),DbNames.ATTRNAME_TAB))){
					  // read mapping from db
					  mapping.readAttrMappingTable(conn,config.getDbschema());
				  }
			}
			if(mappingConfigFilename!=null){
				mapping.readDeprecatedConfig(mappingConfigFilename);
			}
			  TrafoConfig trafoConfig=new TrafoConfig();
			  trafoConfig.readTrafoConfig(conn, config.getDbschema());

			ModelElementSelector ms=new ModelElementSelector();
			ArrayList<String> modelNames=new ArrayList<String>();
			if(models!=null){
				String modelnames[]=models.split(";");
				for(int modeli=0;modeli<modelnames.length;modeli++){
					String m=modelnames[modeli];
					if(m!=null){
						if(m.equals(XTF)){
							// ignore it
						}else{
							modelNames.add(m);				
						}
					}
				}
			}
			java.util.List<Element> eles=ms.getModelElements(modelNames,td, td.getIli1Format()!=null && config.getDoItfLineTables(),Config.CREATE_ENUM_DEFS_MULTI.equals(config.getCreateEnumDefs()));
			optimizeSqlTableNames(config,mapping,eles);
			Viewable2TableMapping class2wrapper=Viewable2TableMapper.getClass2TableMapping(config,trafoConfig,eles);

			SqlColumnConverter geomConverter=null;
			try{
				geomConverter=(SqlColumnConverter)Class.forName(geometryConverter).newInstance();
			}catch(Exception ex){
				throw new Ili2dbException("failed to load/create geometry converter",ex);
			}
			geomConverter.setup(conn, config);

			// create table structure
			EhiLogger.logState("create table structure...");
			try{
				TransferFromIli trsfFromIli=new TransferFromIli();
				
				// map ili-classes to sql-tables
				// TODO move default SRS to config
				DbSchema schema;
				try {
					schema = trsfFromIli.doit(td,eles,mapping,config,idGen,trafoConfig,class2wrapper);
				} catch (Ili2dbException e) {
					throw new Ili2dbException("mapping of ili-classes to sql-tables failed",e);
				}
				if(schema==null){
					return;
				}

				if(!(conn instanceof GeodbConnection)){
					trsfFromIli.addBasketsTable(schema);
					trsfFromIli.addImportsTable(schema);
					TransferFromIli.addInheritanceTable(schema,Integer.parseInt(config.getMaxSqlNameLength()));
					TransferFromIli.addSettingsTable(schema);
					TransferFromIli.addTrafoConfigTable(schema);
					TransferFromIli.addModelsTable(schema);
					trsfFromIli.addEnumTable(schema);
					TransferFromIli.addTableMappingTable(schema);
					TransferFromIli.addAttrMappingTable(schema);
					idGen.addMappingTable(schema);
				}
				
				// TODO create geodb domains
				if(conn instanceof GeodbConnection){
					
				}
							
				GeneratorDriver drv=new GeneratorDriver(gen);
				config.setJdbcConnection(conn);
				idGen.initDb(conn,dbusr);
				idGen.initDbDefs();
				
				drv.visitSchema(config,schema);
				// is a create script requested by user?
				String createscript=config.getCreatescript();
				if(createscript!=null && (gen instanceof GeneratorJdbc)){
					writeScript(createscript,((GeneratorJdbc)gen).iteratorCreateLines());
				}
				// is a drop script requested by user?
				String dropscript=config.getDropscript();
				if(dropscript!=null && (gen instanceof GeneratorJdbc)){
					writeScript(dropscript,((GeneratorJdbc)gen).iteratorDropLines());
				}
				if(!(conn instanceof GeodbConnection)){
					// update mapping table
					mapping.updateTableMappingTable(conn,config.getDbschema());
					mapping.updateAttrMappingTable(conn,config.getDbschema());
					trafoConfig.updateTrafoConfig(conn, config.getDbschema());
					
					// update inheritance table
					trsfFromIli.updateInheritanceTable(conn,config.getDbschema());
					// update enum table
					trsfFromIli.updateEnumTable(conn);
					TransferFromIli.addModels(conn,td,config.getDbschema());
					if(!config.isConfigReadFromDb()){
						TransferFromIli.updateSettings(conn,config,config.getDbschema());
					}
				}
				//if(conn instanceof ch.ehi.ili2geodb.jdbc.GeodbConnection){
				//	String xmlfile=null;
				//	try{
				//		com.esri.arcgis.geodatabasedistributed.GdbExporter exp=new com.esri.arcgis.geodatabasedistributed.GdbExporter();
				//		xmlfile=config.getDbfile()+".xml";
				//		exp.exportWorkspaceSchema(((ch.ehi.ili2geodb.jdbc.GeodbConnection)conn).getGeodbWorkspace(), xmlfile, false,false);
				//	}catch(Throwable ex){
				//		EhiLogger.logError("failed to export gdb to "+xmlfile,ex);
				//	}
				//}
				try {
					conn.commit();
				} catch (SQLException e) {
					throw new Ili2dbException("failed to commit",e);
				}
				
			}catch(java.io.IOException ex){
				throw new Ili2dbException(ex);
			}
			
			try{
				if(conn!=null){
					conn.close();
					conn=null;
				}
				EhiLogger.logState("...done");
			}catch(java.sql.SQLException ex){
				EhiLogger.logError(ex);
			}
				
			}finally{
				ao.end();
			}
			
		}catch(Ili2dbException ex){
			if(logfile!=null){
				logfile.logEvent(new StdLogEvent(LogEvent.ERROR,null,ex,null));
			}
			throw ex;
		}catch(java.lang.RuntimeException ex){
			if(logfile!=null){
				logfile.logEvent(new StdLogEvent(LogEvent.ERROR,null,ex,null));
			}
			throw ex;
		}finally{
			if(logfile!=null){
				EhiLogger.getInstance().removeListener(logfile);
				logfile.close();
				logfile=null;
			}
		}
		
}
	private static void logGeneralInfo(Config config) {
		EhiLogger.logState(config.getSender());
		EhiLogger.logState("ili2c-"+ch.interlis.ili2c.Main.getVersion());
		EhiLogger.logState("java.version "+System.getProperty("java.version"));
		EhiLogger.logState("user.name <"+System.getProperty("user.name")+">");
	}
	private static void setupIli2cPathmap(Config config, String appHome,
			String xtffile,java.sql.Connection conn) throws Ili2dbException {
		config.setValue(ch.interlis.ili2c.gui.UserSettings.ILIDIRS,config.getModeldir());
		java.util.HashMap pathMap=new java.util.HashMap();
		if(xtffile!=null){
			pathMap.put(Ili2db.XTF_DIR,new java.io.File(xtffile).getAbsoluteFile().getParent());
		}else{
			pathMap.put(Ili2db.XTF_DIR,null);
		}
		pathMap.put(Ili2db.JAR_DIR,appHome);
		config.setTransientObject(ch.interlis.ili2c.gui.UserSettings.ILIDIRS_PATHMAP,pathMap);
		
	  	// if ilimodels exists in db
		if(conn!=null){
			IliFiles iliFiles = null;
			String url=null;
			try {
				url=conn.getMetaData().getURL();
				iliFiles=TransferFromIli.readIliFiles(conn,config.getDbschema());
			} catch (SQLException e) {
				throw new Ili2dbException(e);
			}
			if (iliFiles != null) {
				String dbSchema=config.getDbschema();
				if(dbSchema!=null){
					url=url+"/"+dbSchema;
				}
				pathMap.put(Ili2db.ILI_FROM_DB, url);
				config.setTransientValue(
						ch.interlis.ili2c.gui.UserSettings.TEMP_REPOS_URI, url);
				config.setTransientObject(
						ch.interlis.ili2c.gui.UserSettings.TEMP_REPOS_ILIFILES,
						iliFiles);
				config.setTransientObject(
						ch.interlis.ili2c.gui.UserSettings.CUSTOM_ILI_RESOLVER,
						new IliFromDb(url,conn,dbSchema));
			}		  	
		}
	}
	public static void runExport(Config config,String appHome)
	throws Ili2dbException
	{
		ch.ehi.basics.logging.FileListener logfile=null;
		if(config.getLogfile()!=null){
			logfile=new ch.ehi.basics.logging.FileListener(new java.io.File(config.getLogfile()),true);
			EhiLogger.getInstance().addListener(logfile);
		}
		try{
			logGeneralInfo(config);
			
			String xtffile=config.getXtffile();
			if(xtffile==null){
				throw new Ili2dbException("no xtf-file given");
			}
			String modeldir=config.getModeldir();
			if(modeldir==null){
				throw new Ili2dbException("no modeldir given");
			}

			String dburl=config.getDburl();
			String dbusr=config.getDbusr();
			String dbpwd=config.getDbpwd();
			if(dburl==null){
				throw new Ili2dbException("no dburl given");
			}
			if(dbusr==null){
				//EhiLogger.logError("no dbusr given");
				//return;
				dbusr="";
			}
			if(dbpwd==null){
				//EhiLogger.logError("no dbpwd given");
				//return;
				dbpwd="";
			}
			String dbschema=config.getDbschema();
			if(dbschema!=null){
				EhiLogger.logState("dbschema <"+dbschema+">");
			}
			String geometryConverter=config.getGeometryConverter();
			if(geometryConverter==null){
				throw new Ili2dbException("no geoemtry converter given");
			}
			String jdbcDriver=config.getJdbcDriver();
			if(jdbcDriver==null){
				throw new Ili2dbException("no JDBC driver given");
			}
			
			String baskets=config.getBaskets();
			String topics=config.getTopics();
			String models=config.getModels();
			if(models==null && baskets==null && topics==null){
				throw new Ili2dbException("no models, baskets or topics given");
			}
			
			// open db connection
			try{
				Class.forName(jdbcDriver);
			}catch(Exception ex){
				throw new Ili2dbException("failed to load JDBC driver",ex);
			}
			Connection conn=null;
			String url = dburl;
			try{
				EhiLogger.logState("dburl <"+url+">");
				EhiLogger.logState("dbusr <"+dbusr+">");
			  //DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			  try {
				conn = DriverManager.getConnection(url, dbusr, dbpwd);
			} catch (SQLException e) {
				throw new Ili2dbException("failed to get db connection",e);
			}
			  logDBVersion(conn);
			
			ch.interlis.ili2c.config.Configuration modelv=new ch.interlis.ili2c.config.Configuration();
			boolean createBasketCol=config.BASKET_HANDLING_READWRITE.equals(config.getBasketHandling());
			int basketSqlIds[]=null;
			if(baskets!=null){
				if(!createBasketCol){
					throw new Ili2dbException("basket wise export requires column "+DbNames.T_BASKET_COL);
				}
				// BIDs
				String basketids[]=baskets.split(ch.interlis.ili2c.Main.MODELS_SEPARATOR);
				// map BID to sqlBasketId and modelnames
				basketSqlIds=getBasketSqlIdsFromBID(basketids,modelv,conn,config);
			}else if(topics!=null){
				if(!createBasketCol){
					throw new Ili2dbException("topic wise export requires column "+DbNames.T_BASKET_COL);
				}
				// TOPICs
				String topicv[]=topics.split(ch.interlis.ili2c.Main.MODELS_SEPARATOR);
				// map BID to sqlBasketId and modelnames
				basketSqlIds=getBasketSqlIdsFromTopic(topicv,modelv,conn,config);
			}else{
				if(createBasketCol){
					String modelnames[]=models.split(ch.interlis.ili2c.Main.MODELS_SEPARATOR);
					basketSqlIds=getBasketSqlIdsFromModel(modelnames,modelv,conn,config);
				}else{
					String modelnames[]=models.split(ch.interlis.ili2c.Main.MODELS_SEPARATOR);
					for(int modeli=0;modeli<modelnames.length;modeli++){
						String m=modelnames[modeli];
						if(m.equals(XTF)){
							// TODO read modelname from db
						}
						modelv.addFileEntry(new ch.interlis.ili2c.config.FileEntry(m,ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));				
					}
				}
			}
			if(modelv.getSizeFileEntry()==0){
				throw new Ili2dbException("no models given");
			}

			String adapterClassName=config.getGeometryConverter();
			if(adapterClassName==null){
				throw new Ili2dbException("no adapter given");
			}
			

			SqlColumnConverter geomConverter=null;
			try{
				geomConverter=(SqlColumnConverter)Class.forName(geometryConverter).newInstance();
			}catch(Exception ex){
				throw new Ili2dbException("failed to load/create geometry converter",ex);
			}
			

				// compile required ili files
				setupIli2cPathmap(config, appHome, xtffile,conn);
				EhiLogger.logState("compile models...");
				modelv.setAutoCompleteModelList(true);
				modelv.setGenerateWarnings(false);
				TransferDescription td = ch.interlis.ili2c.Main.runCompiler(modelv,
						config);
				if (td == null) {
					throw new Ili2dbException("compiler failed");
				}
			  
			  
			  geomConverter.setup(conn, config);
			  
			  // get mapping definition
			  String mappingConfigFilename=config.getMappingConfigFilename();
			  NameMapping mapping=new NameMapping(config);
			  if(DbUtility.tableExists(conn,new DbTableName(config.getDbschema(),DbNames.CLASSNAME_TAB))){
				  // read mapping from db
				  mapping.readTableMappingTable(conn,config.getDbschema());
			  }else if(mappingConfigFilename!=null){
				  // read mapping from config file if it doesn't exist in the db
				  mapping.readDeprecatedConfig(mappingConfigFilename);
			  }
			  if(DbUtility.tableExists(conn,new DbTableName(config.getDbschema(),DbNames.ATTRNAME_TAB))){
				  // read mapping from db
				  mapping.readAttrMappingTable(conn,config.getDbschema());
			  }
			  TrafoConfig trafoConfig=new TrafoConfig();
			  trafoConfig.readTrafoConfig(conn, config.getDbschema());

				ModelElementSelector ms=new ModelElementSelector();
				ArrayList<String> modelNames=new ArrayList<String>();
				{
					Iterator<ch.interlis.ili2c.config.FileEntry> modi=modelv.iteratorFileEntry();
					while(modi.hasNext()){
						ch.interlis.ili2c.config.FileEntry mod=modi.next();
						if(mod.getKind()==ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE){
							modelNames.add(mod.getFilename());
							EhiLogger.traceState("modelname <"+mod.getFilename()+">");
						}
					}
				}
				
			  java.util.List<Element> eles=ms.getModelElements(modelNames,td, td.getIli1Format()!=null && config.getDoItfLineTables(),Config.CREATE_ENUM_DEFS_MULTI.equals(config.getCreateEnumDefs()));
			  Viewable2TableMapping class2wrapper=Viewable2TableMapper.getClass2TableMapping(config,trafoConfig,eles);

			  // process xtf files
			  EhiLogger.logState("process data...");
			  EhiLogger.logState("data <"+xtffile+">");
				HashSet<BasketStat> stat=new HashSet<BasketStat>();
				ch.ehi.basics.logging.ErrorTracker errs=new ch.ehi.basics.logging.ErrorTracker();
				EhiLogger.getInstance().addListener(errs);
				transferToXtf(conn,xtffile,mapping,td,geomConverter,config.getSender(),config,basketSqlIds,stat,trafoConfig,class2wrapper);
				if (errs.hasSeenErrors()) {
					throw new Ili2dbException("...export failed");
				} else {
					logStatistics(td.getIli1Format() != null, stat);
					EhiLogger.logState("...export done");
				}
			  EhiLogger.getInstance().removeListener(errs);
			//}catch(Exception ex){
				//EhiLogger.logError(ex);
			}finally{
				try{
					conn.close();
				}catch(java.sql.SQLException ex){
					EhiLogger.logError(ex);
				}
			}			
		}catch(Ili2dbException ex){
			if(logfile!=null){
				logfile.logEvent(new StdLogEvent(LogEvent.ERROR,null,ex,null));
			}
			throw ex;
		}catch(java.lang.RuntimeException ex){
			if(logfile!=null){
				logfile.logEvent(new StdLogEvent(LogEvent.ERROR,null,ex,null));
			}
			throw ex;
		}finally{
			if(logfile!=null){
				EhiLogger.getInstance().removeListener(logfile);
				logfile.close();
				logfile=null;
			}
		}
	}
	private static int[] getBasketSqlIdsFromBID(String[] basketids,
			Configuration modelv,Connection conn,Config config) throws Ili2dbException {
		int ret[]=new int[basketids.length];
		int retidx=0;
		String schema=config.getDbschema();
		String colT_ID=config.getColT_ID();
		if(colT_ID==null){
			colT_ID=DbNames.T_ID_COL;
		}

		String sqlName=DbNames.BASKETS_TAB;
		if(schema!=null){
			sqlName=schema+"."+sqlName;
		}
		HashSet<String> models=new HashSet<String>();
		for(String basketid:basketids){
			StringBuilder topicQName=new StringBuilder();
			Integer sqlId=getBasketSqlIdFromBID(basketid, conn, schema,colT_ID, topicQName);
			if(sqlId==null){
				throw new Ili2dbException("no basket with BID "+basketid+" in table "+sqlName);
			}
			String topicName[]=splitIliQName(topicQName.toString());
			if(topicName[0]==null){
				// just a topicname
				throw new Ili2dbException("unexpected unqualified name "+topicQName+" in table "+sqlName);
			}
			String modelName=topicName[0];
			if(!models.contains(modelName)){
				modelv.addFileEntry(new ch.interlis.ili2c.config.FileEntry(modelName,ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));				
				models.add(modelName);
			}
			ret[retidx++]=sqlId;
		}
		return ret;
	}
	public static Integer getBasketSqlIdFromBID(String basketid,Connection conn,String schema, String colT_ID,StringBuilder topicName) throws Ili2dbException {

		String sqlName=DbNames.BASKETS_TAB;
		if(schema!=null){
			sqlName=schema+"."+sqlName;
		}
		int sqlId=0;
		java.sql.PreparedStatement getstmt=null;
		try{
			String stmt="SELECT "+colT_ID+","+DbNames.BASKETS_TAB_TOPIC_COL+" FROM "+sqlName+" WHERE "+DbNames.T_ILI_TID_COL+"= ?";
			EhiLogger.traceBackendCmd(stmt);
			getstmt=conn.prepareStatement(stmt);
			getstmt.setString(1,basketid);
			java.sql.ResultSet res=getstmt.executeQuery();
			if(res.next()){
				sqlId=res.getInt(1);
				topicName.append(res.getString(2));
				return sqlId;
			}
		}catch(java.sql.SQLException ex){
			throw new Ili2dbException("failed to query "+sqlName,ex);
		}finally{
			if(getstmt!=null){
				try{
					getstmt.close();
				}catch(java.sql.SQLException ex){
					EhiLogger.logError(ex);
				}
			}
		}
		return null;
	}
	private static int[] getBasketSqlIdsFromTopic(String[] topics,
			Configuration modelv,Connection conn,Config config) throws Ili2dbException {
		String schema=config.getDbschema();
		String colT_ID=config.getColT_ID();
		if(colT_ID==null){
			colT_ID=DbNames.T_ID_COL;
		}

		String qryTopics[][]=new String[topics.length][];
		int idx=0;
		for(String topic:topics){
			qryTopics[idx++]=splitIliQName(topic);
		}
		String sqlName=DbNames.BASKETS_TAB;
		if(schema!=null){
			sqlName=schema+"."+sqlName;
		}
		HashSet<String> models=new HashSet<String>();
		HashSet<Integer> bids=new HashSet<Integer>();
		String topicQName=null;
		int sqlId=0;
		java.sql.PreparedStatement getstmt=null;
		try{
			String stmt="SELECT "+colT_ID+","+DbNames.BASKETS_TAB_TOPIC_COL+" FROM "+sqlName;
			EhiLogger.traceBackendCmd(stmt);
			getstmt=conn.prepareStatement(stmt);
			java.sql.ResultSet res=getstmt.executeQuery();
			while(res.next()){
				sqlId=res.getInt(1);
				topicQName=res.getString(2);
				String dbTopic[]=splitIliQName(topicQName);
				String modelName=null;
				for(String qryTopic[]:qryTopics){
					if(qryTopic[0]==null && qryTopic[1].equals(dbTopic[1])){
						// found one
						modelName=dbTopic[0];
						break;
					}else if(qryTopic[0]!=null && qryTopic[0].equals(dbTopic[0]) && qryTopic[1].equals(dbTopic[1])){
						// found one
						modelName=dbTopic[0];
						break;
					}
				}
				// found a basket with given topicName?
				if(modelName!=null){
					if(!models.contains(modelName)){
						modelv.addFileEntry(new ch.interlis.ili2c.config.FileEntry(modelName,ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));				
						models.add(modelName);
					}
					if(!bids.contains(sqlId)){
						bids.add(sqlId);
					}
				}
			}
		}catch(java.sql.SQLException ex){
			throw new Ili2dbException("failed to query "+sqlName,ex);
		}finally{
			if(getstmt!=null){
				try{
					getstmt.close();
				}catch(java.sql.SQLException ex){
					EhiLogger.logError(ex);
				}
			}
		}
		if(bids.size()==0){
			throw new Ili2dbException("no baskets with given topic names in table "+sqlName);
		}
		int ret[]=new int[bids.size()];
		idx=0;
		for(int bid:bids){
			ret[idx++]=bid;
		}
		return ret;
	}
	private static int[] getBasketSqlIdsFromModel(String[] qryModels,
			Configuration modelv,Connection conn,Config config) throws Ili2dbException {
		String schema=config.getDbschema();
		String colT_ID=config.getColT_ID();
		if(colT_ID==null){
			colT_ID=DbNames.T_ID_COL;
		}

		int idx=0;
		String sqlName=DbNames.BASKETS_TAB;
		if(schema!=null){
			sqlName=schema+"."+sqlName;
		}
		HashSet<String> models=new HashSet<String>();
		HashSet<Integer> bids=new HashSet<Integer>();
		String topicQName=null;
		int sqlId=0;
		java.sql.PreparedStatement getstmt=null;
		try{
			String stmt="SELECT "+colT_ID+","+DbNames.BASKETS_TAB_TOPIC_COL+" FROM "+sqlName;
			EhiLogger.traceBackendCmd(stmt);
			getstmt=conn.prepareStatement(stmt);
			java.sql.ResultSet res=getstmt.executeQuery();
			while(res.next()){
				sqlId=res.getInt(1);
				topicQName=res.getString(2);
				String dbTopic[]=splitIliQName(topicQName);
				String modelName=null;
				for(String qryModel:qryModels){
					if(qryModel.equals(dbTopic[0])){
						// found one
						modelName=qryModel;
						break;
					}
				}
				// found a basket with given topicName?
				if(modelName!=null){
					if(!models.contains(modelName)){
						modelv.addFileEntry(new ch.interlis.ili2c.config.FileEntry(modelName,ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));				
						models.add(modelName);
					}
					if(!bids.contains(sqlId)){
						bids.add(sqlId);
					}
				}
			}
		}catch(java.sql.SQLException ex){
			throw new Ili2dbException("failed to query "+sqlName,ex);
		}finally{
			if(getstmt!=null){
				try{
					getstmt.close();
				}catch(java.sql.SQLException ex){
					EhiLogger.logError(ex);
				}
			}
		}
		if(bids.size()==0){
			throw new Ili2dbException("no baskets with given topic names in table "+sqlName);
		}
		int ret[]=new int[bids.size()];
		idx=0;
		for(int bid:bids){
			ret[idx++]=bid;
		}
		return ret;
	}
	private static String[] splitIliQName(String topicQName){
		String modelName=null;
		String topicName=null;
		int endModelName=topicQName.indexOf('.');
		if(endModelName<=0){
			// just a topicname
			topicName=topicQName;
		}else{
			// qualified topicname; get model name
			modelName=topicQName.substring(0,endModelName);
			topicName=topicQName.substring(endModelName+1);
		}
		String ret[]=new String[2];
		ret[0]=modelName;
		ret[1]=topicName;
		return ret;
	}
	public static String getModelFromXtf(String filename)
	{
		ch.interlis.iox.StartBasketEvent be=null;
		try{
			IoxReader ioxReader=null;
			if(isItfFilename(filename)){
				ioxReader=new ch.interlis.iom_j.itf.ItfReader(new java.io.File(filename));
			}else{
				ioxReader=new ch.interlis.iom_j.xtf.XtfReader(new java.io.File(filename));
			}
			// get first basket
			ch.interlis.iox.IoxEvent event;
			do{
				event=ioxReader.read();
				if(event instanceof ch.interlis.iox.StartBasketEvent){
					be=(ch.interlis.iox.StartBasketEvent)event;
					break;
				}
			}while(!(event instanceof ch.interlis.iox.EndTransferEvent));
			ioxReader.close();
			ioxReader=null;
		}catch(ch.interlis.iox.IoxException ex){
			EhiLogger.logError("failed to read model from xml file "+filename,ex);
			return null;
		}
		// no baskets?
		if(be==null){
			// no model
			return null;
		}
		String qtopic[]=be.getType().split("\\.");
		String model=qtopic[0];
		//EhiLogger.debug("model from xtf <"+model+">");
		return model;
	}
	public static String getModelFromXtf(java.io.InputStream f,String filename)
	{
		ch.interlis.iox.StartBasketEvent be=null;
		try{
			IoxReader ioxReader=null;
			if(isItfFilename(filename)){
				ioxReader=new ch.interlis.iom_j.itf.ItfReader(f);
			}else{
				ioxReader=new ch.interlis.iom_j.xtf.XtfReader(f);
			}
			// get first basket
			ch.interlis.iox.IoxEvent event;
			do{
				event=ioxReader.read();
				if(event instanceof ch.interlis.iox.StartBasketEvent){
					be=(ch.interlis.iox.StartBasketEvent)event;
					break;
				}
			}while(!(event instanceof ch.interlis.iox.EndTransferEvent));
			ioxReader.close();
			ioxReader=null;
		}catch(ch.interlis.iox.IoxException ex){
			EhiLogger.logError("failed to read model from xml file "+filename,ex);
			return null;
		}
		// no baskets?
		if(be==null){
			// no model
			return null;
		}
		String qtopic[]=be.getType().split("\\.");
		String model=qtopic[0];
		//EhiLogger.debug("model from xtf <"+model+">");
		return model;
	}
	public static ArrayList getModelDirv(String modeldir, String xtffile,String ili2dbHome) {
		ArrayList modeldirv=new ArrayList();
		String modeldirs[]=modeldir.split(ch.interlis.ili2c.Main.ILIDIR_SEPARATOR);
		for(int modeli=0;modeli<modeldirs.length;modeli++){
			String m=modeldirs[modeli];
			if(m.equals(XTF_DIR)){
				if(xtffile!=null){
					m=new java.io.File(xtffile).getAbsoluteFile().getParentFile().getAbsolutePath();
				}else{
					m=null;
				}
			}else if(m.equals(JAR_DIR)){
				if(ili2dbHome!=null){
					m=ili2dbHome;
				}else{
					m=null;
				}
			}
			if(m!=null && m.length()>0){
				modeldirv.add(m);				
			}
		}
		return modeldirv;
	}

	static private void transferFromXtf(Connection conn,IoxReader reader,boolean importOnly,NameMapping ili2sqlName,TransferDescription td,
			String dbusr,
			SqlColumnConverter geomConv,
			DbIdGen idGen,
			Config config,
			HashSet<BasketStat> stat,
			TrafoConfig trafoConfig,Viewable2TableMapping class2wrapper){	
		try{
			TransferFromXtf trsfr=new TransferFromXtf(importOnly,ili2sqlName,td,conn,dbusr,geomConv,idGen,config,trafoConfig,class2wrapper);
			trsfr.doit(reader,config,stat);
		}catch(ch.interlis.iox.IoxException ex){
			EhiLogger.logError("failed to read data file",ex);
		} catch (Ili2dbException ex) {
			EhiLogger.logError("failed to read data file",ex);
		}
	}
	/** transfer data from database to xml file
	*/
	static private void transferToXtf(Connection conn,String xtffile,NameMapping ili2sqlName,TransferDescription td
			,SqlColumnConverter geomConv
			,String sender
			,Config config
			,int basketSqlIds[]
			,HashSet<BasketStat> stat
			,TrafoConfig trafoConfig
			,Viewable2TableMapping class2wrapper){	

		java.io.File outfile=new java.io.File(xtffile);
		IoxWriter ioxWriter=null;
		try{
			String ext=ch.ehi.basics.view.GenericFileFilter.getFileExtension(xtffile).toLowerCase();
			if(config.isItfTransferfile()){
				if(!config.getDoItfLineTables()){
					ioxWriter=new ItfWriter2(outfile,td);
				}else{
					ioxWriter=new ItfWriter(outfile,td);
				}
			}else if(ext!=null && ext.equals("gml")){
				ioxWriter=new IligmlWriter(outfile,td);
			}else{
				ioxWriter=new XtfWriter(outfile,td);
			}
			TransferToXtf trsfr=new TransferToXtf(ili2sqlName,td,conn,geomConv,config,trafoConfig,class2wrapper);
			trsfr.doit(outfile.getName(),ioxWriter,sender,basketSqlIds,stat);
			//trsfr.doitJava();
			ioxWriter.flush();
		}catch(ch.interlis.iox.IoxException ex){
			EhiLogger.logError("failed to write xml output",ex);
		}finally{
			if(ioxWriter!=null){
				try{
					ioxWriter.close();
				}catch(ch.interlis.iox.IoxException ex){
					EhiLogger.logError("failed to close xml output",ex);
				}
			}
			ioxWriter=null;
		}
	}
	static public void optimizeSqlTableNames(Config config,NameMapping mapping,java.util.List<Element> eles)
	{
		if(config.NAME_OPTIMIZATION_DISABLE.equals(config.getNameOptimization())){
			return;
		}
		NameOptimizer optimizer=null;
		if(config.NAME_OPTIMIZATION_TOPIC.equals(config.getNameOptimization())){
			optimizer=new NameOptimizer(){
				@Override
				public String createTableName(Element ele) {
					if(ele instanceof ch.interlis.ili2c.metamodel.Viewable
							|| ele instanceof ch.interlis.ili2c.metamodel.Domain){
						ch.interlis.ili2c.metamodel.Container container=ele.getContainer();
						String optimizedName=container.getName()+"_"+ele.getName();
						return optimizedName;
					}else if(ele instanceof AttributeDef)
					  {
						AttributeDef attr = (AttributeDef) ele;
						ch.interlis.ili2c.metamodel.Viewable v=(ch.interlis.ili2c.metamodel.Viewable)attr.getContainer();
						ch.interlis.ili2c.metamodel.Container container=v.getContainer();
						Type type = Type.findReal (attr.getDomain());
						return container.getName()+"_"+v.getName()+"_"+attr.getName();
					  }
					return null;
				}
				
			};
		}else{
			optimizer=new NameOptimizer(){
				@Override
				public String createTableName(Element ele) {
					if(ele instanceof ch.interlis.ili2c.metamodel.Viewable
							|| ele instanceof ch.interlis.ili2c.metamodel.Domain){
						return ele.getName();
					}else if(ele instanceof AttributeDef)
					  {
						AttributeDef attr = (AttributeDef) ele;
						ch.interlis.ili2c.metamodel.Viewable v=(ch.interlis.ili2c.metamodel.Viewable)attr.getContainer();
						return v.getName()+"_"+attr.getName();
					  }
					return null;
				}
				
			};
		}
		HashSet<String> names=new HashSet<String>();
		
		ArrayList<Element> elev=new ArrayList<Element>(eles);
		//java.util.Collections.reverse(elev);
		for (Element ele : elev) {
			if (ele instanceof ch.interlis.ili2c.metamodel.Viewable
					|| ele instanceof ch.interlis.ili2c.metamodel.Domain) {
				if(ele instanceof ch.interlis.ili2c.metamodel.Table && ((ch.interlis.ili2c.metamodel.Table)ele).isIli1LineAttrStruct()){
					// skip it
				}else{
					String optimizedTableName = optimizer.createTableName(ele);
					if (optimizedTableName != null && !names.contains(optimizedTableName)) {
						mapping.defineTableNameMapping(ele.getScopedName(null),
								optimizedTableName);
						names.add(optimizedTableName);
					}
				}
			} else if (ele instanceof AttributeDef) {
				AttributeDef attr = (AttributeDef) ele;
				Viewable v = (Viewable) attr.getContainer();
				String optimizedTableName = optimizer.createTableName(attr);
				if (optimizedTableName != null
						&& !names.contains(optimizedTableName)) {
					String qualifiedModelElementName = v.getContainer()
					.getScopedName(null) + "." + v.getName() + "."
							+ attr.getName();
					mapping.defineTableNameMapping(qualifiedModelElementName,
							optimizedTableName);
					names.add(optimizedTableName);
				}
			}

		}
	}
	
	static private HashSet getModelNames(ArrayList modelv){
		HashSet ret=new HashSet();
		Iterator modeli=modelv.iterator();
		while(modeli.hasNext()){
			String topic=(String)modeli.next(); // modelname or qualified topicname
			int endModelName=topic.indexOf('.');
			if(endModelName<=0){
				// just a modelname
				ret.add(topic);
			}else{
				// qualified topicname; get model name
				String model=topic.substring(0,endModelName);
				ret.add(model);
			}
		}
		return ret;
	}
	static private void writeScript(String filename,Iterator linei)
	throws java.io.IOException
	{
		java.io.PrintWriter out=new java.io.PrintWriter(new java.io.BufferedOutputStream(new java.io.FileOutputStream(filename)));
		while(linei.hasNext()){
		  GeneratorJdbc.AbstractStmt stmt=(GeneratorJdbc.AbstractStmt)linei.next();
		  String line=stmt.getLine();
		  if(stmt instanceof GeneratorJdbc.Stmt){
			out.println(line+";");
		  }else{
			out.println("-- "+line);
		  }
		}
		out.close();				
	}
	public static boolean isItfFilename(String filename)
	{
		String xtfExt=ch.ehi.basics.view.GenericFileFilter.getFileExtension(new java.io.File(filename)).toLowerCase();
		if("itf".equals(xtfExt)){
			return true;
		}
		return false;
	}
	public static void logDBVersion(Connection conn)
	{
		try {
			EhiLogger.logState("databaseProduct <"
					+ conn.getMetaData().getDatabaseProductName() + ">");
			EhiLogger.logState("databaseVersion <"
					+ conn.getMetaData().getDatabaseProductVersion() + ">");
			EhiLogger.logState("driverVersion <"
					+ conn.getMetaData().getDriverVersion() + ">");
			if(conn.getMetaData().getURL().startsWith("jdbc:postgresql:")){
				try {
					java.sql.Statement stmt=conn.createStatement();
					String sql="SELECT PostGIS_Full_Version()";
					ResultSet rs=stmt.executeQuery(sql);
					if(rs.next()){
						String ver=rs.getString(1);
						EhiLogger.logState("postGISVersion <"+ ver + ">");
					}
				} catch (SQLException e) {
					throw new IllegalStateException("failed to get PostGIS version",e);
				}
			}
		} catch (SQLException e) {
			EhiLogger.logError(e);
		}
		
	}

}
