package ch.ehi.ili2db;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.DbNames;
import ch.ehi.ili2db.base.DbUrlConverter;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2db.mapping.NameMapping;
import ch.ehi.sqlgen.DbUtility;
import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.xtf.Xtf24Reader;
import ch.interlis.iom_j.xtf.XtfReader;
import ch.interlis.iox.EndBasketEvent;
import ch.interlis.iox.EndTransferEvent;
import ch.interlis.iox.IoxEvent;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox.ObjectEvent;
import ch.interlis.iox.StartBasketEvent;
import ch.interlis.iox.StartTransferEvent;

//-Ddburl=jdbc:postgresql:dbname -Ddbusr=usrname -Ddbpwd=1234
@Ignore
public class MultiCrs24Test {
	private static final String DBSCHEMA = "MultiCrs24";
	String dburl=System.getProperty("dburl"); 
	String dbuser=System.getProperty("dbusr");
	String dbpwd=System.getProperty("dbpwd"); 

	public Config initConfig(String xtfFilename,String dbschema,String logfile) {
		Config config=new Config();
		new ch.ehi.ili2pg.PgMain().initConfig(config);
		config.setDburl(dburl);
		config.setDbusr(dbuser);
		config.setDbpwd(dbpwd);
		if(dbschema!=null){
			config.setDbschema(dbschema);
		}
		if(logfile!=null){
			config.setLogfile(logfile);
		}
		config.setXtffile(xtfFilename);
		if(xtfFilename!=null && Ili2db.isItfFilename(xtfFilename)){
			config.setItfTransferfile(true);
		}
		return config;
	}
	
    @Test
    public void importIli() throws Exception
    {
        //EhiLogger.getInstance().setTraceFilter(false);
        Connection jdbcConnection=null;
        Statement stmt=null;
        try{
            Class driverClass = Class.forName("org.postgresql.Driver");
            jdbcConnection = DriverManager.getConnection(dburl, dbuser, dbpwd);
            stmt=jdbcConnection.createStatement();
            stmt.execute("DROP SCHEMA IF EXISTS "+DBSCHEMA+" CASCADE");        

            File data=new File("test/data/Crs/MultiCrs24.ili");
            Config config=initConfig(data.getPath(),DBSCHEMA,data.getPath()+".log");
            config.setFunction(Config.FC_SCHEMAIMPORT);
            config.setCreateFk(config.CREATE_FK_YES);
            config.setTidHandling(Config.TID_HANDLING_PROPERTY);
            config.setBasketHandling(config.BASKET_HANDLING_READWRITE);
            config.setCatalogueRefTrafo(null);
            config.setMultiSurfaceTrafo(null);
            config.setMultiLineTrafo(null);
            config.setMultiPointTrafo(null);
            config.setMultilingualTrafo(null);
            config.setInheritanceTrafo(null);
            config.setUseEpsgInNames(true);
            config.setValidation(false);
            Ili2db.readSettingsFromDb(config);
            Ili2db.run(config,null);
            
            final String ili2db_attrname_table=DBSCHEMA+"."+DbNames.ATTRNAME_TAB;
            // verify attr name mapping
            {
                String stmtTxt="SELECT "+DbNames.ATTRNAME_TAB_SQLNAME_COL+","+DbNames.ATTRNAME_TAB_OWNER_COL+" FROM "+ili2db_attrname_table+" WHERE "+DbNames.ATTRNAME_TAB_ILINAME_COL+"='MultiCrs24.TestA.ClassA1.attr2:2056'";
                Assert.assertTrue(stmt.execute(stmtTxt));
                ResultSet rs=stmt.getResultSet();
                Assert.assertTrue(rs.next());
                Assert.assertEquals("attr2_2056",rs.getString(1));
                Assert.assertEquals("classa1",rs.getString(2));
            }
            {
                String stmtTxt="SELECT "+DbNames.ATTRNAME_TAB_SQLNAME_COL+","+DbNames.ATTRNAME_TAB_OWNER_COL+" FROM "+ili2db_attrname_table+" WHERE "+DbNames.ATTRNAME_TAB_ILINAME_COL+"='MultiCrs24.TestA.ClassA1.attr2:21781'";
                Assert.assertTrue(stmt.execute(stmtTxt));
                ResultSet rs=stmt.getResultSet();
                Assert.assertTrue(rs.next());
                Assert.assertEquals("attr2_21781",rs.getString(1));
                Assert.assertEquals("classa1",rs.getString(2));
            }
            // verify columns exist
            {
                String stmtTxt="SELECT attr2_21781,attr2_2056 FROM "+DBSCHEMA+".classa1";
                Assert.assertTrue(stmt.execute(stmtTxt));
                ResultSet rs=stmt.getResultSet();
                Assert.assertFalse(rs.next());
            }
            {
                // t_ili2db_attrname
                String [][] expectedValues=new String[][] {
                    {"MultiCrs24.TestA.ClassA1.attr2:2056", "attr2_2056", "classa1", null},   
                    {"MultiCrs24.TestA.ClassA1.attr2:21781", "attr2_21781", "classa1", null},
                    {"MultiCrs24.TestA.ClassA1.attr1", "attr1", "classa1", null}   
                };
                Ili2dbAssert.assertAttrNameTable(jdbcConnection, expectedValues, DBSCHEMA);
            }
            {
                // t_ili2db_trafo
                String [][] expectedValues=new String[][] {
                    {"MultiCrs24.TestA.ClassA1", "ch.ehi.ili2db.inheritance", "newClass"},
                };
                Ili2dbAssert.assertTrafoTable(jdbcConnection,expectedValues, DBSCHEMA);
            }
        }catch(Exception e) {
            throw new IoxException(e);
        }finally{
            if(stmt!=null) {
                stmt.close();
                stmt=null;
            }
            if(jdbcConnection!=null){
                jdbcConnection.close();
            }
        }
    }
	
	@Test
	public void importXtf() throws Exception
	{
		//EhiLogger.getInstance().setTraceFilter(false);
		Connection jdbcConnection=null;
        Statement stmt=null;
		try{
		    Class driverClass = Class.forName("org.postgresql.Driver");
	        jdbcConnection = DriverManager.getConnection(dburl, dbuser, dbpwd);
	        stmt=jdbcConnection.createStatement();
			stmt.execute("DROP SCHEMA IF EXISTS "+DBSCHEMA+" CASCADE");        

			File data=new File("test/data/Crs/MultiCrs24.xtf");
			Config config=initConfig(data.getPath(),DBSCHEMA,data.getPath()+".log");
            config.setDatasetName("Data");
			config.setFunction(Config.FC_IMPORT);
			config.setCreateFk(config.CREATE_FK_YES);
			config.setTidHandling(Config.TID_HANDLING_PROPERTY);
			config.setBasketHandling(config.BASKET_HANDLING_READWRITE);
			config.setCatalogueRefTrafo(null);
			config.setMultiSurfaceTrafo(null);
			config.setMultiLineTrafo(null);
			config.setMultiPointTrafo(null);
			config.setMultilingualTrafo(null);
			config.setInheritanceTrafo(null);
			config.setUseEpsgInNames(true);
			config.setValidation(false);
			Ili2db.readSettingsFromDb(config);
			Ili2db.run(config,null);
			// assertions
			ResultSet rs = stmt.executeQuery("SELECT st_asewkt(attr2_2056),st_asewkt(attr2_21781) FROM "+DBSCHEMA+".classa1 ORDER BY t_id ASC;");
			ResultSetMetaData rsmd=rs.getMetaData();
			assertTrue(rs.next());
			assertEquals("SRID=2056;POINT(2460001 1045001)", rs.getObject(1));
            assertEquals(null, rs.getObject(2));
            assertTrue(rs.next());
            assertEquals(null, rs.getObject(1));
            assertEquals("SRID=21781;POINT(460002 45002)", rs.getObject(2));
		}catch(Exception e) {
			throw new IoxException(e);
		}finally{
		    if(stmt!=null) {
		        stmt.close();
		        stmt=null;
		    }
			if(jdbcConnection!=null){
				jdbcConnection.close();
			}
		}
	}
	
	@Test
	public void exportXtf() throws Exception
	{
	    importXtf();
		try{
		    Class driverClass = Class.forName("org.postgresql.Driver");
	        
	        File data=new File("test/data/Crs/MultiCrs24-out.xtf");
			Config config=initConfig(data.getPath(),DBSCHEMA,data.getPath()+".log");
			config.setDatasetName("Data");
			config.setFunction(Config.FC_EXPORT);
			config.setValidation(false);
			Ili2db.readSettingsFromDb(config);
			Ili2db.run(config,null);
			
            TransferDescription td=null;
            Configuration ili2cConfig=new Configuration();
            FileEntry fileEntry=new FileEntry("test/data/Crs/MultiCrs24.ili", FileEntryKind.ILIMODELFILE);
            ili2cConfig.addFileEntry(fileEntry);
            td=ch.interlis.ili2c.Ili2c.runCompiler(ili2cConfig);
            assertNotNull(td);

			HashMap<String,IomObject> objs=new HashMap<String,IomObject>();
			IoxReader reader=Xtf24Reader.createReader(data);
			((Xtf24Reader) reader).setModel(td);
			IoxEvent event=null;
			 do{
		        event=reader.read();
		        if(event instanceof StartTransferEvent){
		        }else if(event instanceof StartBasketEvent){
		        }else if(event instanceof ObjectEvent){
		        	IomObject iomObj=((ObjectEvent)event).getIomObject();
		        	if(iomObj.getobjectoid()!=null){
			        	objs.put(iomObj.getobjectoid(), iomObj);
		        	}
		        }else if(event instanceof EndBasketEvent){
		        }else if(event instanceof EndTransferEvent){
		        }
			 }while(!(event instanceof EndTransferEvent));
			 {
				 IomObject obj0 = objs.get("4");
				 Assert.assertNotNull(obj0);
                 Assert.assertEquals("COORD {C1 2460001.0, C2 1045001.0}", obj0.getattrobj("attr2", 0).toString());
			 }
			 {
				 IomObject obj0 = objs.get("8");
				 Assert.assertNotNull(obj0);
				 Assert.assertEquals("COORD {C1 460002.0, C2 45002.0}", obj0.getattrobj("attr2", 0).toString());
			 }
		}catch(Exception e) {
			throw new IoxException(e);
		}finally{
		}
	}
}