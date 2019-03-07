PRAGMA application_id=1196437808;

CREATE TABLE gpkg_spatial_ref_sys ( srs_name TEXT NOT NULL, srs_id INTEGER NOT NULL PRIMARY KEY, organization TEXT NOT NULL, organization_coordsys_id INTEGER NOT NULL, definition TEXT NOT NULL, description TEXT);

INSERT INTO gpkg_spatial_ref_sys ( srs_name, srs_id, organization, organization_coordsys_id, definition) VALUES ('WGS-84', 4326, 'EPSG', 4326, 'GEOGCS["WGS 84",DATUM["WGS_1984",SPHEROID["WGS 84",6378137,298.257223563,AUTHORITY["EPSG","7030"]],AUTHORITY["EPSG","6326"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.0174532925199433,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4326"]]');

INSERT INTO gpkg_spatial_ref_sys ( srs_name, srs_id, organization, organization_coordsys_id, definition) VALUES ('Undefined', -1, 'NONE', -1, 'Undefined');

INSERT INTO gpkg_spatial_ref_sys ( srs_name, srs_id, organization, organization_coordsys_id, definition) VALUES ('CH1903 / LV03', 21781, 'EPSG', 21781, 'PROJCS["CH1903 / LV03",GEOGCS["CH1903",DATUM["CH1903",SPHEROID["Bessel 1841",6377397.155,299.1528128,AUTHORITY["EPSG","7004"]],TOWGS84[674.374,15.056,405.346,0,0,0,0],AUTHORITY["EPSG","6149"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.0174532925199433,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4149"]],UNIT["metre",1,AUTHORITY["EPSG","9001"]],PROJECTION["Hotine_Oblique_Mercator_Azimuth_Center"],PARAMETER["latitude_of_center",46.95240555555556],PARAMETER["longitude_of_center",7.439583333333333],PARAMETER["azimuth",90],PARAMETER["rectified_grid_angle",90],PARAMETER["scale_factor",1],PARAMETER["false_easting",600000],PARAMETER["false_northing",200000],AUTHORITY["EPSG","21781"],AXIS["Y",EAST],AXIS["X",NORTH]]');

INSERT INTO gpkg_spatial_ref_sys ( srs_name, srs_id, organization, organization_coordsys_id, definition) VALUES ('CH1903+ / LV95', 2056, 'EPSG', 2056, 'PROJCS["CH1903+ / LV95",GEOGCS["CH1903+",DATUM["CH1903+",SPHEROID["Bessel 1841",6377397.155,299.1528128,AUTHORITY["EPSG","7004"]],TOWGS84[674.374,15.056,405.346,0,0,0,0],AUTHORITY["EPSG","6150"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.0174532925199433,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4150"]],PROJECTION["Hotine_Oblique_Mercator_Azimuth_Center"],PARAMETER["latitude_of_center",46.95240555555556],PARAMETER["longitude_of_center",7.439583333333333],PARAMETER["azimuth",90],PARAMETER["rectified_grid_angle",90],PARAMETER["scale_factor",1],PARAMETER["false_easting",2600000],PARAMETER["false_northing",1200000],UNIT["metre",1,AUTHORITY["EPSG","9001"]],AXIS["Y",EAST],AXIS["X",NORTH],AUTHORITY["EPSG","2056"]]');

INSERT INTO gpkg_spatial_ref_sys ( srs_name, srs_id, organization, organization_coordsys_id, definition) VALUES ('MAGNA-SIRGAS / Colombia Bogota zone', 3116, 'EPSG', 3116, 'PROJCS["MAGNA-SIRGAS / Colombia Bogota zone",GEOGCS["MAGNA-SIRGAS",DATUM["Marco_Geocentrico_Nacional_de_Referencia",SPHEROID["GRS 1980",6378137,298.257222101,AUTHORITY["EPSG","7019"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY["EPSG","6686"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.0174532925199433,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4686"]],PROJECTION["Transverse_Mercator"],PARAMETER["latitude_of_origin",4.596200416666666],PARAMETER["central_meridian",-74.07750791666666],PARAMETER["scale_factor",1],PARAMETER["false_easting",1000000],PARAMETER["false_northing",1000000],UNIT["metre",1,AUTHORITY["EPSG","9001"]],AUTHORITY["EPSG","3116"]]');

INSERT INTO gpkg_spatial_ref_sys ( srs_name, srs_id, organization, organization_coordsys_id, definition) VALUES ('Undefined', 0, 'NONE', 0, 'Undefined');

CREATE TABLE gpkg_contents ( table_name TEXT NOT NULL PRIMARY KEY, data_type TEXT NOT NULL, identifier TEXT UNIQUE, description TEXT DEFAULT '', last_change DATETIME NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', ' now' )), min_x DOUBLE, min_y DOUBLE, max_x DOUBLE, max_y DOUBLE, srs_id INTEGER, CONSTRAINT fk_gc_r_srs_id FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys(srs_id));

CREATE TABLE gpkg_geometry_columns ( table_name TEXT NOT NULL, column_name TEXT NOT NULL, geometry_type_name TEXT NOT NULL, srs_id INTEGER NOT NULL, z TINYINT NOT NULL, m TINYINT NOT NULL, CONSTRAINT pk_geom_cols PRIMARY KEY (table_name, column_name), CONSTRAINT uk_gc_table_name UNIQUE (table_name), CONSTRAINT fk_gc_tn FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name), CONSTRAINT fk_gc_srs FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys (srs_id));

CREATE TABLE gpkg_extensions (table_name TEXT,column_name TEXT,extension_name TEXT NOT NULL,definition TEXT NOT NULL,scope TEXT NOT NULL,CONSTRAINT ge_tce UNIQUE (table_name, column_name, extension_name))

CREATE TABLE gpkg_data_columns (table_name TEXT NOT NULL, column_name TEXT NOT NULL,  name TEXT UNIQUE,  title TEXT,  description TEXT,  mime_type TEXT,  constraint_name TEXT,  CONSTRAINT pk_gdc PRIMARY KEY (table_name, column_name),  CONSTRAINT fk_gdc_tn FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name));

CREATE TABLE gpkg_data_column_constraints (  constraint_name TEXT NOT NULL,  constraint_type TEXT NOT NULL,  value TEXT,  min NUMERIC,  min_is_inclusive BOOLEAN,  max NUMERIC,  max_is_inclusive BOOLEAN,  description TEXT,  CONSTRAINT gdcc_ntv UNIQUE (constraint_name, constraint_type, value));

INSERT INTO gpkg_extensions (table_name,column_name,extension_name,definition,scope) VALUES ('gpkg_data_columns','','gpkg_schema','http://www.geopackage.org/spec120/#extension_schema','read-write');

INSERT INTO gpkg_extensions (table_name,column_name,extension_name,definition,scope) VALUES ('gpkg_data_column_constraints','','gpkg_schema','http://www.geopackage.org/spec120/#extension_schema','read-write');
