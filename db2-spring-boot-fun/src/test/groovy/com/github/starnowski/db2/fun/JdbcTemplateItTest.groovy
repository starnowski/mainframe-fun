package com.github.starnowski.db2.fun

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementCallback
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.jdbc.JdbcTestUtils
import spock.lang.Specification

import java.sql.PreparedStatement
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
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = ["clear_tables.sql", "items_with_number_drop_upsert_procedure.sql"],
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    def "should insert item with number to db2 table with name #name and number #number with procedure"() {
        given:
            final String itemName = name
            final int itemNumber = number
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withSchemaName("DB2_FUN").withProcedureName("ITEMS_WITH_NUMBER_UPSERT")
            SqlParameterSource parameterSource = new MapSqlParameterSource().addValue("P_ITEM_NAME", name).addValue("P_NUM", number)

        when:
            simpleJdbcCall.execute()

        then:
            JdbcTestUtils.countRowsInTable(jdbcTemplate, "DB2_FUN.ITEMS_WITH_NUMBER") == 1
            // Same assertion but with usage of groovy string templates
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.ITEMS_WITH_NUMBER", "ITEM_NAME = '${name}' AND NUM = ${itemNumber}") == 1

        where:
            name                | number
            "vvcc"              | 13
            "some item name"    | 463
    }


    //items_with_number_add_upsert_procedure.sql
    //items_with_number_drop_upsert_procedure.sql
}