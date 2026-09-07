# AGATE Tosca Migration

TOSCA Module die nicht unterstützt sind aber in SVC TOSCA genutzt
Do-While Loop


Abaluf:
In TOSCA exportieren Template (z.B. dmp_11_getAdminPatientenInformationen.tsu) und dazuhörige TestCaseDesign Sheet (z.B. tcd_dmp_11_getAdminPatientenInformationen.tsu)

agate-tosca-migration-svc>startMigration.bat migrate DMP11 dmp_11_getAdminPatientenInformationen
agate-tosca-migration-svc>startMigration.bat migrate DMP11 tcd_dmp_11_getAdminPatientenInformationen

im folder darf man nur dmp_11_getAdminPatientenInformationen.yaml und  tcd_dmp_11_getAdminPatientenInformationen.csv finden

agate-tosca-migration-svc>startMigration.bat clean DMP11 dmp_11_getAdminPatientenInformationen.yaml tcd_dmp_11_getAdminPatientenInformationen.csv

Kopieren su datai in \agate-tosca-migration-svc\tsu\*.*
agate-tosca-migration-svc>startMigration.bat migrate DMP11 dmp_11_getAdminPatientenInformationen
agate-tosca-migration-svc>startMigration.bat migrate DMP11 tcd_dmp_11_getAdminPatientenInformationen
kopieren agate-tosca-migration-svc\migration\data\DMP11\template\tcd_dmp_11_getAdminPatientenInformationen.csv to 
agate-server\data\DMP11\template\tcd_dmp_11_getAdminPatientenInformationen.csv

kopieren agate-tosca-migration-svc\migration\data\DMP11\template\dmp_11_getAdminPatientenInformationen.yaml to 
agate-server\data\DMP11\template\dmp_11_getAdminPatientenInformationen.yaml

cd agate-server
startTests.bat instantiate DMP11 dmp_11_getAdminPatientenInformationen.yaml dmp_11_getAdminPatientenInformationen.csv

agate-server>startTests.bat Milenko ECS_SYST_AUT1 DMP11 Instance_dmp_11_getAdminPatientenInformationen.yaml

>>> ERROR | Missing configuration file: data/dmp11/modules/soap/ecsrvbe2dmp/services/dmp_ss30_frontendservicesoap/schreibedmppatienteinv2_request/metadata.json


