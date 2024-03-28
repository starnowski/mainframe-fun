CREATE OR REPLACE PROCEDURE DB2_FUN.BINARY_FILE_WITH_CHECKSUM_INSERT (
    IN P_FILE_NAME VARCHAR(255),
    IN P_FILE_CONTENT BLOB(16777216)
)
LANGUAGE SQL
MODIFIES SQL DATA
BEGIN
    DECLARE V_MD5_CHECKSUM VARCHAR(32);
    SET V_MD5_CHECKSUM = HEX( CAST ( HASH(P_FILE_CONTENT, 0) AS VARBINARY(16)) );
    INSERT INTO DB2_FUN.BINARY_FILE_WITH_CHECKSUM (FILE_RECORD_NAME, FILE_RECORD, FILE_RECORD_CHECKSUM) VALUES (P_FILE_NAME, P_FILE_CONTENT, V_MD5_CHECKSUM);
    COMMIT;
END@
