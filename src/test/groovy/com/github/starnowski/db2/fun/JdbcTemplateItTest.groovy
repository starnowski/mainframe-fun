package com.github.starnowski.db2.fun

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementCallback
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
            JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "DB2_FUN.ITEMS", " ITEM_NAME == " + name) == 1

        where:
            name << ["test1", "simon tools"]
    }
}