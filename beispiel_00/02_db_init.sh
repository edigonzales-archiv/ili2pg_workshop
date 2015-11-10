#!/bin/bash

SUPERUSER="postgres"

ADMIN="stefan"
ADMINPWD="ziegler12"
USER="mspublic"
USERPWD="mspublic"

DB_NAME="rosebud2"

echo "Delete database: $DB_NAME"
sudo -u $SUPERUSER dropdb $DB_NAME

echo "Create database: $DB_NAME"
sudo -u $SUPERUSER createdb --owner $ADMIN $DB_NAME

sudo -u $SUPERUSER psql -d $DB_NAME -c "CREATE EXTENSION postgis;"


echo "Grant tables to..."
sudo -u $SUPERUSER psql -d $DB_NAME -c "GRANT ALL ON SCHEMA public TO $ADMIN;"
sudo -u $SUPERUSER psql -d $DB_NAME -c "ALTER TABLE geometry_columns OWNER TO $ADMIN;"
sudo -u $SUPERUSER psql -d $DB_NAME -c "GRANT ALL ON geometry_columns TO $ADMIN;"
sudo -u $SUPERUSER psql -d $DB_NAME -c "GRANT ALL ON spatial_ref_sys TO $ADMIN;"
sudo -u $SUPERUSER psql -d $DB_NAME -c "GRANT ALL ON geography_columns TO $ADMIN;"
sudo -u $SUPERUSER psql -d $DB_NAME -c "GRANT ALL ON raster_columns TO $ADMIN;"
sudo -u $SUPERUSER psql -d $DB_NAME -c "GRANT ALL ON raster_overviews TO $ADMIN;"
