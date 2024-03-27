
DROP TABLE IF EXISTS DB2_FUN.ITEMS;
BEGIN
    IF EXISTS (SELECT SCHEMANAME FROM SYSCAT.SCHEMATA WHERE SCHEMANAME = 'DB2_FUN') THEN
        EXECUTE IMMEDIATE 'DROP SCHEMA DB2_FUN RESTRICT';
    END IF;
END;

CREATE SCHEMA DB2_FUN AUTHORIZATION DB2INST1;

CREATE TABLE DB2_FUN.ITEMS (
    ITEM_NAME   VARCHAR(255) NOT NULL
);