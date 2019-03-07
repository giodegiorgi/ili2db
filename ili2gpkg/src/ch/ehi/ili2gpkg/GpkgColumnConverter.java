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
package ch.ehi.ili2gpkg;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.ehi.ili2db.converter.AbstractWKBColumnConverter;
import ch.ehi.ili2db.converter.ConverterException;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2db.json.Iox2json;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.vividsolutions.jts.io.ParseException;

import ch.interlis.ili2c.metamodel.AttributeDef;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.Iom_jObject;
import ch.interlis.iom_j.itf.EnumCodeMapper;
import ch.interlis.iox_j.wkb.Iox2wkbException;

public class GpkgColumnConverter extends AbstractWKBColumnConverter {
    private TransferDescription td=null;
	@Override
	public Integer getSrsid(String crsAuthority, String crsCode, Connection conn)
			throws ConverterException {
		int srsid;
		try{
			java.sql.Statement stmt=conn.createStatement();
			java.sql.ResultSet ret=stmt.executeQuery("SELECT srs_id FROM gpkg_spatial_ref_sys WHERE organization=\'"+crsAuthority+"\' AND organization_coordsys_id="+crsCode);
			if(!ret.next()){
				return null;
			}
			srsid=ret.getInt(1);
		}catch(java.sql.SQLException ex){
			throw new ConverterException("failed to query srsid from database",ex);
		}
		return srsid;
	}
	private boolean strokeArcs=true;
	@Override
	public void setup(Connection conn, Settings config) {
		super.setup(conn,config);
		strokeArcs=Config.STROKE_ARCS_ENABLE.equals(Config.getStrokeArcs(config));
	}
	
	@Override
	public String getInsertValueWrapperCoord(String wkfValue,int srid) {
		return wkfValue;
	}
	@Override
	public String getInsertValueWrapperPolyline(String wkfValue,int srid) {
		return wkfValue;
	}
	@Override
	public String getInsertValueWrapperMultiPolyline(String wkfValue,int srid) {
		return wkfValue;
	}
	@Override
	public String getInsertValueWrapperSurface(String wkfValue,int srid) {
		return wkfValue;
	}
	@Override
	public String getInsertValueWrapperMultiSurface(String wkfValue,int srid) {
		return wkfValue;
	}
	@Override
	public String getSelectValueWrapperDate(String sqlColName) {
		 return "strftime('%Y-%m-%d %H:%M:%fZ',"+sqlColName+")";
	}

	@Override
	public String getSelectValueWrapperTime(String sqlColName) {
		 return "strftime('%Y-%m-%d %H:%M:%fZ',"+sqlColName+")";
	}

	@Override
	public String getSelectValueWrapperDateTime(String sqlColName) {
		 return "strftime('%Y-%m-%d %H:%M:%fZ',"+sqlColName+")";
	}
	@Override
	public String getSelectValueWrapperCoord(String dbNativeValue) {
		return dbNativeValue;
	}
	@Override
	public String getSelectValueWrapperMultiCoord(String dbNativeValue) {
		return dbNativeValue;
	}
	@Override
	public String getSelectValueWrapperPolyline(String dbNativeValue) {
		return dbNativeValue;
	}
	@Override
	public String getSelectValueWrapperMultiPolyline(String dbNativeValue) {
		return dbNativeValue;
	}
	@Override
	public String getSelectValueWrapperSurface(String dbNativeValue) {
		return dbNativeValue;
	}
	@Override
	public String getSelectValueWrapperMultiSurface(String dbNativeValue) {
		return dbNativeValue;
	}
	@Override
	public Object fromIomUuid(String uuid) 
			throws java.sql.SQLException, ConverterException
	{
		return uuid;
	}
	@Override
	public Object fromIomXml(String xml) 
			throws java.sql.SQLException, ConverterException
	{
		return xml;
	}
	@Override
	public Object fromIomBlob(String blob) 
			throws java.sql.SQLException, ConverterException
	{

	    byte[] bytearray;
		try {
			bytearray = new sun.misc.BASE64Decoder().decodeBuffer(blob);
		} catch (IOException e) {
			throw new ConverterException(e);
		}
		return bytearray;
	}
	@Override
	public java.lang.Object fromIomSurface(
			IomObject value,
			int srsid,
			boolean hasLineAttr,
			boolean is3D,double p)
			throws SQLException, ConverterException {
				if(value!=null){
					Iox2gpkg conv=new Iox2gpkg(is3D?3:2);
					//EhiLogger.debug("conv "+conv); // select st_asewkt(form) from tablea
					try {
						return conv.surface2wkb(value,!strokeArcs,p,srsid);
					} catch (Iox2wkbException ex) {
						throw new ConverterException(ex);
					}
				}
				return null;
		}
	@Override
	public java.lang.Object fromIomMultiSurface(
			IomObject value,
			int srsid,
			boolean hasLineAttr,
			boolean is3D,double p)
			throws SQLException, ConverterException {
				if(value!=null){
					Iox2gpkg conv=new Iox2gpkg(is3D?3:2);
					//EhiLogger.debug("conv "+conv); // select st_asewkt(form) from tablea
					try {
						return conv.multisurface2wkb(value,!strokeArcs,p,srsid);
					} catch (Iox2wkbException ex) {
						throw new ConverterException(ex);
					}
				}
				return null;
		}
		@Override
		public java.lang.Object fromIomCoord(IomObject value, int srsid,boolean is3D)
			throws SQLException, ConverterException {
			if(value!=null){
				Iox2gpkg conv=new Iox2gpkg(is3D?3:2);
				try {
					return conv.coord2wkb(value,srsid);
				} catch (Iox2wkbException ex) {
					throw new ConverterException(ex);
				}
			}
			return null;
		}
		@Override
		public java.lang.Object fromIomMultiCoord(IomObject value, int srsid,boolean is3D)
			throws SQLException, ConverterException {
			if(value!=null){
				Iox2gpkg conv=new Iox2gpkg(is3D?3:2);
				try {
					return conv.multicoord2wkb(value,srsid);
				} catch (Iox2wkbException ex) {
					throw new ConverterException(ex);
				}
			}
			return null;
		}
		@Override
		public java.lang.Object fromIomPolyline(IomObject value, int srsid,boolean is3D,double p)
			throws SQLException, ConverterException {
			if(value!=null){
				Iox2gpkg conv=new Iox2gpkg(is3D?3:2);
				try {
					return conv.polyline2wkb(value,false,!strokeArcs,p,srsid);
				} catch (Iox2wkbException ex) {
					throw new ConverterException(ex);
				}
			}
			return null;
		}
		@Override
		public java.lang.Object fromIomMultiPolyline(IomObject value, int srsid,boolean is3D,double p)
			throws SQLException, ConverterException {
			if(value!=null){
				Iox2gpkg conv=new Iox2gpkg(is3D?3:2);
				try {
					return conv.multiline2wkb(value,!strokeArcs,p,srsid);
				} catch (Iox2wkbException ex) {
					throw new ConverterException(ex);
				}
			}
			return null;
		}
		@Override
		public IomObject toIomCoord(
				Object geomobj,
				String sqlAttrName,
				boolean is3D)
				throws SQLException, ConverterException {
				byte bv[]=(byte [])geomobj;
				Gpkg2iox conv=new Gpkg2iox();
				try {
					return conv.read(bv);
				} catch (ParseException e) {
					throw new ConverterException(e);
				}
			}
		@Override
		public IomObject toIomMultiCoord(
				Object geomobj,
				String sqlAttrName,
				boolean is3D)
				throws SQLException, ConverterException {
				byte bv[]=(byte [])geomobj;
				Gpkg2iox conv=new Gpkg2iox();
				try {
					return conv.read(bv);
				} catch (ParseException e) {
					throw new ConverterException(e);
				}
			}
		@Override
			public IomObject toIomSurface(
				Object geomobj,
				String sqlAttrName,
				boolean is3D)
				throws SQLException, ConverterException {
				byte bv[]=(byte [])geomobj;
				Gpkg2iox conv=new Gpkg2iox();
				try {
					return conv.read(bv);
				} catch (ParseException e) {
					throw new ConverterException(e);
				}
			}
		@Override
		public IomObject toIomMultiSurface(
			Object geomobj,
			String sqlAttrName,
			boolean is3D)
			throws SQLException, ConverterException {
			byte bv[]=(byte [])geomobj;
			Gpkg2iox conv=new Gpkg2iox();
			try {
				return conv.read(bv);
			} catch (ParseException e) {
				throw new ConverterException(e);
			}
		}
		@Override
			public IomObject toIomPolyline(
				Object geomobj,
				String sqlAttrName,
				boolean is3D)
				throws SQLException, ConverterException {
				byte bv[]=(byte [])geomobj;
				Gpkg2iox conv=new Gpkg2iox();
				try {
					return conv.read(bv);
				} catch (ParseException e) {
					throw new ConverterException(e);
				}
			}
		@Override
		public IomObject toIomMultiPolyline(
			Object geomobj,
			String sqlAttrName,
			boolean is3D)
			throws SQLException, ConverterException {
			byte bv[]=(byte [])geomobj;
			Gpkg2iox conv=new Gpkg2iox();
			try {
				return conv.read(bv);
			} catch (ParseException e) {
				throw new ConverterException(e);
			}
		}
		@Override
		public String toIomXml(Object obj) throws java.sql.SQLException,
				ConverterException {
			return (String)obj;
		}

		@Override
		public String toIomBlob(Object obj) throws java.sql.SQLException,
				ConverterException {
		    String s = new sun.misc.BASE64Encoder().encode((byte[])obj);
		    return s;
		}

		@Override
		public void setTimestamp(PreparedStatement ps, int valuei,
				Timestamp datetime) throws SQLException {
			java.text.DateFormat dfm = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			dfm.setTimeZone(java.util.TimeZone.getTimeZone("GMT+0"));
			String stmp=dfm.format(datetime)+"Z"; // add timesone indicator (Z)
			ps.setString(valuei, stmp);
		}

		@Override
		public void setDate(PreparedStatement ps, int valuei, Date date)
				throws SQLException {
			java.text.DateFormat dfm = new java.text.SimpleDateFormat("yyyy-MM-dd");
			String stmp=dfm.format(date);
			ps.setString(valuei, stmp);
		}

		@Override
		public void setTime(PreparedStatement ps, int valuei, Time time)
				throws SQLException {
			java.text.DateFormat dfm = new java.text.SimpleDateFormat("HH:mm:ss.SSS");
			String stmp=dfm.format(time);
			ps.setString(valuei, stmp);
		}
		@Override
		public void setXmlNull(PreparedStatement stmt, int parameterIndex)
				throws SQLException {
			 stmt.setNull(parameterIndex, Types.VARCHAR);
		}
		@Override
		public void setBlobNull(PreparedStatement stmt, int parameterIndex)
				throws SQLException {
			 stmt.setNull(parameterIndex, Types.VARBINARY);
		}
	    @Override
	    public String getSelectValueWrapperArray(String sqlColName) {
            return sqlColName;
	    }
	    @Override
	    public String getInsertValueWrapperArray(String sqlColName) {
            return sqlColName;
	    }
	    @Override
	    public void setArrayNull(PreparedStatement stmt, int parameterIndex) throws SQLException {
            stmt.setNull(parameterIndex, Types.VARCHAR);
	    }
	    @Override
	    public Object fromIomArray(AttributeDef iliEleAttr, String[] iomValues, boolean isEnumInt)
	            throws SQLException, ConverterException {
            JsonFactory jsonF = new JsonFactory();
            JsonGenerator jg=null;
            java.io.StringWriter out=new java.io.StringWriter();
            
            try {
                jg = jsonF.createJsonGenerator(out);
                Iox2json.writeArray(jg, iomValues,iliEleAttr,isEnumInt);
                jg.flush();
                jg.close();
                jg=null;
            } catch (IOException e) {
                throw new ConverterException(e);
            }
            return out.toString();
	    }
	    @Override
	    public String[] toIomArray(AttributeDef iliEleAttr, Object sqlArray, boolean isEnumInt)
	            throws SQLException, ConverterException {
            JsonFactory jsonF = new JsonFactory();
            java.io.StringReader in=new java.io.StringReader((String)sqlArray);
            String values[]=null;
            try {
                JsonParser jg = jsonF.createJsonParser(in);
                
                values=Iox2json.readArray(jg);
            }catch(IOException ex) {
                throw new ConverterException(ex);
            }
            return values;
	    }
		
	    @Override
	    public String getSelectValueWrapperJson(String sqlColName) {
	        return sqlColName;
	    }
	    @Override
	    public String getInsertValueWrapperJson(String sqlColName) {
	        return sqlColName;
	    }
	    @Override
	    public void setJsonNull(PreparedStatement stmt, int parameterIndex) throws SQLException {
            stmt.setNull(parameterIndex, Types.VARCHAR);
	    }
	    @Override
	    public Object fromIomStructureToJson(AttributeDef iliEleAttr, IomObject[] iomObjects)
	            throws SQLException, ConverterException {
	        JsonFactory jsonF = new JsonFactory();
	        java.io.StringWriter out=new java.io.StringWriter();
            JsonGenerator jg=null;
	        
	        try {
	            if(td==null) {
	                td=(TransferDescription) iliEleAttr.getContainer(TransferDescription.class);
	            }
                jg = jsonF.createJsonGenerator(out);
                Iox2json.write(jg, iomObjects,td);
                jg.flush();
                jg.close();
                jg=null;
            } catch (IOException e) {
                throw new ConverterException(e);
            }
	        return out.toString();
	    }
	    @Override
	    public IomObject[] toIomStructureFromJson(AttributeDef iliEleAttr, Object sqlArray)
	            throws SQLException, ConverterException {
	        JsonFactory jsonF = new JsonFactory();
	        java.io.StringReader in=new java.io.StringReader((String)sqlArray);
	        IomObject iomObj[]=null;
	        try {
	            JsonParser jg = jsonF.createJsonParser(in);
	            
	            iomObj=Iox2json.read(jg);
	        }catch(IOException ex) {
                throw new ConverterException(ex);
	        }
	        return iomObj;
	    }

}
