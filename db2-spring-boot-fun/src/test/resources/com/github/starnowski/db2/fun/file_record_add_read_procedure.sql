CREATE OR REPLACE PROCEDURE DB2_FUN.BINARY_FILE_WITH_CHECKSUM_READ (
    IN P_FILE_NAME VARCHAR(255)
)
LANGUAGE SQL
DYNAMIC RESULT SETS 1
BEGIN
    DECLARE CURSOR1 CURSOR WITH RETURN FOR
        SELECT FILE_RECORD AS R_FILE_CONTENT, FILE_RECORD_NAME AS R_FILE_NAME , FILE_RECORD_CHECKSUM AS R_FILE_CHECKSUM FROM DB2_FUN.BINARY_FILE_WITH_CHECKSUM
        WHERE FILE_RECORD_NAME = P_FILE_NAME
        ;
    OPEN CURSOR1;
END@
