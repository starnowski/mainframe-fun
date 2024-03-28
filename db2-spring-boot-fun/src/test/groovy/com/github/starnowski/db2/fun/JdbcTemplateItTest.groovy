package com.github.starnowski.db2.fun

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementCallback
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.jdbc.JdbcTestUtils
import spock.lang.Specification

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = DB2Application)
@ContextConfiguration
class JdbcTemplateItTest extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Sql(value = "clear_tables.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "clear_tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    def "should save item to db2 table with name #name"() {
        given:
            final String itemName = name
            def insertStatement = "INSERT INTO DB2_FUN.ITEMS (ITEM_NAME) VALUES (?)"

        when:
            jdbcTemplate.execute(insertStatement, new PreparedStatementCallback<Void>() {
                @Override
                Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                    ps.setString(1, itemName)
                    ps.execute()
                    return null
                }
            })

        then:
            JdbcTestUtils.countRowsInTable(jdbcTemplate, "DB2_FUN.ITEMS") == 1
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.ITEMS", "ITEM_NAME = '" + name + "'") == 1
            // Same assertion but with usage of groovy string templates
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.ITEMS", "ITEM_NAME = '${name}'") == 1

        where:
            name << ["test1", "simon tools"]
    }

    @Sql(value = "clear_tables.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "clear_tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    def "should save item with number to db2 table with name #name and number #number"() {
        given:
            final String itemName = name
            final int itemNumber = number
            def insertStatement = "INSERT INTO DB2_FUN.ITEMS_WITH_NUMBER (ITEM_NAME, NUM) VALUES (?, ?)"

        when:
            jdbcTemplate.execute(insertStatement, new PreparedStatementCallback<Void>() {
                @Override
                Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                    ps.setString(1, itemName)
                    ps.setInt(2, itemNumber)
                    ps.execute()
                    return null
                }
            })

        then:
            JdbcTestUtils.countRowsInTable(jdbcTemplate, "DB2_FUN.ITEMS_WITH_NUMBER") == 1
            // Same assertion but with usage of groovy string templates
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.ITEMS_WITH_NUMBER", "ITEM_NAME = '${name}' AND NUM = ${itemNumber}") == 1

        where:
            name | number
            "xxx"   | 13
    }

    @Sql(value = ["clear_tables.sql", "items_with_number_add_upsert_procedure.sql"],
            config = @SqlConfig(separator = "@"),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = ["clear_tables.sql", "items_with_number_drop_upsert_procedure.sql"],
            config = @SqlConfig(separator = "@"),
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    def "should insert item with number to db2 table with name #name and number #number with procedure"() {
        given:
            final String itemName = name
            final int itemNumber = number
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withSchemaName("DB2_FUN").withProcedureName("ITEMS_WITH_NUMBER_UPSERT")
            SqlParameterSource parameterSource = new MapSqlParameterSource().addValue("P_ITEM_NAME", name).addValue("P_NUM", number)

        when:
            simpleJdbcCall.execute(parameterSource)

        then:
            JdbcTestUtils.countRowsInTable(jdbcTemplate, "DB2_FUN.ITEMS_WITH_NUMBER") == 1
            // Same assertion but with usage of groovy string templates
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.ITEMS_WITH_NUMBER", "ITEM_NAME = '${name}' AND NUM = ${itemNumber}") == 1

        where:
            name                | number
            "vvcc"              | 13
            "some item name"    | 463
    }

    @Sql(value = ["clear_tables.sql", "items_with_number_add_upsert_procedure.sql", "items_with_number.sql"],
            config = @SqlConfig(separator = "@"),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = ["clear_tables.sql", "items_with_number_drop_upsert_procedure.sql"],
            config = @SqlConfig(separator = "@"),
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    def "should update item with number to db2 table with name #name and number #number with procedure when current number is #currentNumber"() {
        given:
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withSchemaName("DB2_FUN").withProcedureName("ITEMS_WITH_NUMBER_UPSERT")
            SqlParameterSource parameterSource = new MapSqlParameterSource().addValue("P_ITEM_NAME", name).addValue("P_NUM", number)
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.ITEMS_WITH_NUMBER", "ITEM_NAME = '${name}' AND NUM = ${currentNumber}") == 1

        when:
            simpleJdbcCall.execute(parameterSource)

        then:
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.ITEMS_WITH_NUMBER", "ITEM_NAME = '${name}' AND NUM = ${number}") == 1
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.ITEMS_WITH_NUMBER", "ITEM_NAME = '${name}' AND NUM = ${currentNumber}") == 0

        where:
            name                | currentNumber |   number
            "item1"             | 13            |   3213
            "item2"             | 463           |   -43
    }

    @Sql(value = ["clear_tables.sql", "file_record_add_upsert_procedure.sql"],
            config = @SqlConfig(separator = "@"),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = ["clear_tables.sql", "file_record_drop_upsert_procedure.sql"],
            config = @SqlConfig(separator = "@"),
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    def "should save file #file and calculate correct md5 checksum"() {
        given:
            def content = getClass().getResourceAsStream(file).readAllBytes()
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withSchemaName("DB2_FUN").withProcedureName("BINARY_FILE_WITH_CHECKSUM_INSERT")
            SqlParameterSource parameterSource = new MapSqlParameterSource().addValue("P_FILE_NAME", file).addValue("P_FILE_CONTENT", content)
            JdbcTestUtils.countRowsInTable(jdbcTemplate, "DB2_FUN.BINARY_FILE_WITH_CHECKSUM") == 0
            String checksumGenerateByFirstStrategy = ChecksumMD5Utils.calculateMD5ChecksumForByteArrayWithFirstStrategy(content)
            String checksumGenerateBySecondStrategy = ChecksumMD5Utils.calculateMD5ChecksumForByteArrayWithSecondStrategy(content)

        when:
            simpleJdbcCall.execute(parameterSource)

        then:
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.BINARY_FILE_WITH_CHECKSUM", "FILE_RECORD_NAME = '${file}'") == 1
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.BINARY_FILE_WITH_CHECKSUM", "FILE_RECORD_NAME = '${file}' AND FILE_RECORD IS NOT NULL") == 1
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.BINARY_FILE_WITH_CHECKSUM", "FILE_RECORD_NAME = '${file}' AND FILE_RECORD_CHECKSUM = '${checksumGenerateByFirstStrategy}'") == 1
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.BINARY_FILE_WITH_CHECKSUM", "FILE_RECORD_NAME = '${file}' AND FILE_RECORD_CHECKSUM = '${checksumGenerateBySecondStrategy}'") == 1

        where:
            file << ["test1.txt", "test2.txt"]
    }

    @Sql(value = ["clear_tables.sql", "file_record_add_upsert_procedure.sql", "file_record_add_read_procedure.sql"],
            config = @SqlConfig(separator = "@"),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = ["clear_tables.sql", "file_record_drop_upsert_procedure.sql", "file_record_drop_read_procedure.sql"],
            config = @SqlConfig(separator = "@"),
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    def "should read saved file #file and its md5 checksum"() {
        given:
            def content = getClass().getResourceAsStream(file).readAllBytes()
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withSchemaName("DB2_FUN").withProcedureName("BINARY_FILE_WITH_CHECKSUM_INSERT")
            SqlParameterSource parameterSource = new MapSqlParameterSource().addValue("P_FILE_NAME", file).addValue("P_FILE_CONTENT", content)
            JdbcTestUtils.countRowsInTable(jdbcTemplate, "DB2_FUN.BINARY_FILE_WITH_CHECKSUM") == 0
            String checksumGenerateByFirstStrategy = ChecksumMD5Utils.calculateMD5ChecksumForByteArrayWithFirstStrategy(content)
            simpleJdbcCall.execute(parameterSource)
            JdbcTestUtils.countRowsInTable(jdbcTemplate, "DB2_FUN.BINARY_FILE_WITH_CHECKSUM") == 1
            SimpleJdbcCall tested = new SimpleJdbcCall(jdbcTemplate).withSchemaName("DB2_FUN").withProcedureName("BINARY_FILE_WITH_CHECKSUM_READ")
                    .returningResultSet("mapObjRefrence", new RowMapper<BinaryFileWithChecksum>() {

                        @Override
                        BinaryFileWithChecksum mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return new BinaryFileWithChecksum(rs.getString("R_FILE_NAME"), rs.getString("R_FILE_CHECKSUM"), rs.getBytes("R_FILE_CONTENT"))
                        }
                    });
            SqlParameterSource testedParameterSource = new MapSqlParameterSource().addValue("P_FILE_NAME", file)


        when:
            def result = tested.execute(testedParameterSource)
            System.out.println("result is " + result)

        then:
            result.get("mapObjRefrence")
            List<BinaryFileWithChecksum> objects = result.get("mapObjRefrence")
            !objects.isEmpty()
            with(objects.get(0)) {
                getName() == file
                getContent() == content
                getChecksum() == checksumGenerateByFirstStrategy
            }

        where:
        file << ["test1.txt", "test2.txt"]
    }

    private static class BinaryFileWithChecksum {
        private final String checksum;
        private final byte[] content;
        private final String name;

        String getName() {
            return name
        }

        String getChecksum() {
            return checksum
        }
        byte[] getContent() {
            return content
        }

        BinaryFileWithChecksum(String name, String checksum, byte[] content) {
            this.name = name
            this.checksum = checksum
            this.content = content
        }

    }

}