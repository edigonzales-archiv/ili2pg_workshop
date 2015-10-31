CREATE TABLE buildings_v1.Buildings_Roof_types (
  itfCode integer PRIMARY KEY
  ,iliCode varchar(1024) NOT NULL
  ,seq integer NULL
  ,dispName varchar(250) NOT NULL
)
;
-- Buildings_V1.Buildings.Building
CREATE TABLE buildings_v1.Buildings_Building (
  T_Id integer PRIMARY KEY
  ,T_Type varchar(60) NOT NULL
  ,Storeys integer NOT NULL
  ,Roof varchar(255) NULL
)
;
SELECT AddGeometryColumn('buildings_v1','buildings_building','geometry',(SELECT srid FROM SPATIAL_REF_SYS WHERE AUTH_NAME='EPSG' AND AUTH_SRID=21781),'POLYGON',2);
COMMENT ON TABLE buildings_v1.Buildings_Building IS '@iliname Buildings_V1.Buildings.Building';
CREATE TABLE buildings_v1.T_ILI2DB_IMPORT_BASKET (
  T_Id integer PRIMARY KEY
  ,import integer NOT NULL
  ,basket integer NOT NULL
  ,objectCount integer NULL
  ,start_t_id integer NULL
  ,end_t_id integer NULL
)
;
CREATE TABLE buildings_v1.T_ILI2DB_ATTRNAME (
  IliName varchar(1024) PRIMARY KEY
  ,SqlName varchar(1024) NOT NULL
)
;
CREATE TABLE buildings_v1.T_ILI2DB_BASKET (
  T_Id integer PRIMARY KEY
  ,dataset integer NULL
  ,topic varchar(200) NOT NULL
  ,T_Ili_Tid varchar(200) NULL
  ,attachmentKey varchar(200) NOT NULL
)
;
CREATE TABLE buildings_v1.T_ILI2DB_IMPORT_OBJECT (
  T_Id integer PRIMARY KEY
  ,import_basket integer NOT NULL
  ,class varchar(200) NOT NULL
  ,objectCount integer NULL
  ,start_t_id integer NULL
  ,end_t_id integer NULL
)
;
CREATE TABLE buildings_v1.T_ILI2DB_DATASET (
  T_Id integer PRIMARY KEY
)
;
-- Buildings_V1.Buildings.ApartmentBuilding
CREATE TABLE buildings_v1.Buildings_ApartmentBuilding (
  T_Id integer PRIMARY KEY
  ,Apartments integer NOT NULL
)
;
COMMENT ON TABLE buildings_v1.Buildings_ApartmentBuilding IS '@iliname Buildings_V1.Buildings.ApartmentBuilding';
CREATE TABLE buildings_v1.T_KEY_OBJECT (
  T_Key varchar(30) PRIMARY KEY
  ,T_LastUniqueId integer NOT NULL
  ,T_LastChange timestamp NOT NULL
  ,T_CreateDate timestamp NOT NULL
  ,T_User varchar(40) NOT NULL
)
;
CREATE TABLE buildings_v1.T_ILI2DB_SETTINGS (
  tag varchar(60) PRIMARY KEY
  ,setting varchar(60) NULL
)
;
CREATE TABLE buildings_v1.T_ILI2DB_CLASSNAME (
  IliName varchar(1024) PRIMARY KEY
  ,SqlName varchar(1024) NOT NULL
)
;
CREATE TABLE buildings_v1.T_ILI2DB_MODEL (
  file varchar(250) NOT NULL
  ,iliversion varchar(3) NOT NULL
  ,modelName text NOT NULL
  ,content text NOT NULL
  ,importDate timestamp NOT NULL
  ,PRIMARY KEY (iliversion,modelName)
)
;
CREATE TABLE buildings_v1.T_ILI2DB_INHERITANCE (
  thisClass varchar(60) PRIMARY KEY
  ,baseClass varchar(60) NULL
)
;
CREATE TABLE buildings_v1.T_ILI2DB_IMPORT (
  T_Id integer PRIMARY KEY
  ,dataset integer NOT NULL
  ,importDate timestamp NOT NULL
  ,importUser varchar(40) NOT NULL
  ,importFile varchar(200) NOT NULL
)
;
-- Buildings_V1.Buildings.Address
CREATE TABLE buildings_v1.Buildings_Address (
  T_Id integer PRIMARY KEY
  ,T_Seq integer NOT NULL
  ,House_number varchar(20) NOT NULL
  ,Street_name varchar(255) NOT NULL
  ,RegBL_EGID integer NULL
  ,Buildings_V1Buildings_Building_Addresses integer NULL
)
;
COMMENT ON TABLE buildings_v1.Buildings_Address IS '@iliname Buildings_V1.Buildings.Address';
COMMENT ON COLUMN buildings_v1.Buildings_Address.Buildings_V1Buildings_Building_Addresses IS '@iliname Buildings_V1.Buildings.Building.Addresses';
-- Buildings_V1.Buildings.AdministrativeBuilding
CREATE TABLE buildings_v1.Buildings_AdministrativeBuilding (
  T_Id integer PRIMARY KEY
  ,Department varchar(255) NULL
)
;
COMMENT ON TABLE buildings_v1.Buildings_AdministrativeBuilding IS '@iliname Buildings_V1.Buildings.AdministrativeBuilding';
