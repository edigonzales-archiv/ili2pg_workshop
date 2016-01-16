sudo -u postgres psql -d rosebud2 -c "CREATE EXTENSION fineltra;"
sudo -u postgres psql -d rosebud2 -c "CREATE SCHEMA av_chenyx06 AUTHORIZATION stefan;"

cd ~
wget https://www.dropbox.com/s/63lm992uypbol3m/chenyx06.sqlite?dl=0 -O chenyx06.sqlite
/usr/local/gdal_master/bin/ogr2ogr -f "PostgreSQL" PG:"dbname='rosebud2' host='localhost' port='5432' user='stefan' password='ziegler12'" chenyx06.sqlite chenyx06 -lco SCHEMA=av_chenyx06 -nln chenyx06_triangles

sudo -u postgres psql -d rosebud2 -c "GRANT USAGE ON SCHEMA av_chenyx06 TO mspublic;"
sudo -u postgres psql -d rosebud2 -c "GRANT SELECT ON av_chenyx06.chenyx06_triangles TO mspublic;"


