/**
 * Database initialization
 * Prerequisite: Created Oracle Database with Characterset UTF8
 */
/**
 * create eSciDoc DB users and schemas
 */  
CREATE USER ${escidoc.database.user} IDENTIFIED BY ${escidoc.database.password}; 

GRANT CONNECT, create any table, create any index, select any table, insert any table, update any table, create any procedure, UNLIMITED TABLESPACE TO ${escidoc.database.user};

CREATE USER aa IDENTIFIED BY aa;

GRANT UNLIMITED TABLESPACE TO aa;

CREATE USER adm IDENTIFIED BY adm; 

GRANT UNLIMITED TABLESPACE TO adm;

CREATE USER oai IDENTIFIED BY oai; 

GRANT UNLIMITED TABLESPACE TO oai;

CREATE USER om IDENTIFIED BY om; 

GRANT UNLIMITED TABLESPACE TO om;

CREATE USER sm IDENTIFIED BY sm; 

GRANT UNLIMITED TABLESPACE TO sm;

CREATE USER st IDENTIFIED BY st;

GRANT UNLIMITED TABLESPACE TO st;

    